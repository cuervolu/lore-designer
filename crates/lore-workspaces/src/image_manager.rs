use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use std::collections::HashMap;
use std::fs;
use std::path::{Path, PathBuf};
use std::sync::Mutex;
use tokio::io::AsyncReadExt;
use tracing::{debug, error, info, warn};

const IMAGE_INDEX_FILE: &str = "image_references.json";
const IMAGE_INDEX_VERSION: u32 = 1;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ImageReference {
    pub hash: String,
    pub original_name: String,
    pub added_at: String,
    pub used_by: Vec<String>,
    pub size_bytes: u64,
    pub last_verified: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ImageIndex {
    pub version: u32,
    pub last_indexed: String,
    pub images: HashMap<String, ImageReference>,
}

impl Default for ImageIndex {
    fn default() -> Self {
        Self {
            version: IMAGE_INDEX_VERSION,
            last_indexed: chrono::Utc::now().to_rfc3339(),
            images: HashMap::new(),
        }
    }
}

#[derive(Debug, Serialize)]
pub struct ValidationReport {
    pub total_images: usize,
    pub missing_images: Vec<MissingImageInfo>,
    pub orphaned_images: Vec<String>,
}

#[derive(Debug, Serialize)]
pub struct MissingImageInfo {
    pub path: String,
    pub original_name: String,
    pub used_by: Vec<String>,
}

#[derive(Debug, Serialize)]
pub struct BulkUpdateResult {
    pub updated_files: Vec<String>,
    pub failed_files: Vec<String>,
}

pub struct ImageRegistry {
    index: Mutex<ImageIndex>,
    workspace_path: PathBuf,
}

impl ImageRegistry {
    /// Create a new ImageRegistry for a workspace
    pub fn new(workspace_path: impl AsRef<Path>) -> Result<Self> {
        let workspace_path = workspace_path.as_ref().to_path_buf();
        let index = Self::load_or_create_index(&workspace_path)?;

        Ok(Self {
            index: Mutex::new(index),
            workspace_path,
        })
    }

    /// Load existing index or create a new one
    fn load_or_create_index(workspace_path: &Path) -> Result<ImageIndex> {
        let index_path = workspace_path.join(crate::INTERNAL_DIR).join(IMAGE_INDEX_FILE);

        if index_path.exists() {
            debug!("Loading existing image index from {}", index_path.display());
            let content = fs::read_to_string(&index_path)
                .context("Failed to read image index file")?;
            
            let index: ImageIndex = serde_json::from_str(&content)
                .context("Failed to parse image index JSON")?;
            
            info!("Loaded image index with {} entries", index.images.len());
            Ok(index)
        } else {
            debug!("No existing image index found, creating new one");
            Ok(ImageIndex::default())
        }
    }

    /// Save the index to disk
    fn save_index(&self) -> Result<()> {
        let index = self.index.lock().unwrap();
        let index_path = self.workspace_path.join(crate::INTERNAL_DIR).join(IMAGE_INDEX_FILE);

        let json = serde_json::to_string_pretty(&*index)
            .context("Failed to serialize image index")?;

        fs::write(&index_path, json)
            .context(format!("Failed to write image index to {}", index_path.display()))?;

        debug!("Image index saved to {}", index_path.display());
        Ok(())
    }

    /// Calculate SHA-256 hash of a file
    pub async fn calculate_file_hash(file_path: impl AsRef<Path>) -> Result<String> {
        let file_path = file_path.as_ref();
        let mut file = tokio::fs::File::open(file_path).await
            .context(format!("Failed to open file for hashing: {}", file_path.display()))?;

        let mut hasher = Sha256::new();
        let mut buffer = vec![0u8; 8192]; // 8KB buffer

        loop {
            let n = file.read(&mut buffer).await
                .context("Failed to read file during hashing")?;
            
            if n == 0 {
                break;
            }
            
            hasher.update(&buffer[..n]);
        }

        Ok(format!("{:x}", hasher.finalize()))
    }

    /// Register a new image or update existing entry
    pub async fn register_image(
        &self,
        relative_path: String,
        used_by_file: String,
    ) -> Result<()> {
        let absolute_path = self.workspace_path.join(&relative_path);

        if !absolute_path.exists() {
            return Err(anyhow::anyhow!(
                "Image file does not exist: {}",
                absolute_path.display()
            ));
        }

        let hash = Self::calculate_file_hash(&absolute_path).await?;
        
        let metadata = tokio::fs::metadata(&absolute_path).await
            .context("Failed to read image metadata")?;
        let size_bytes = metadata.len();

        let original_name = absolute_path
            .file_name()
            .and_then(|n| n.to_str())
            .unwrap_or("unknown")
            .to_string();

        let now = chrono::Utc::now().to_rfc3339();

        let mut index = self.index.lock().unwrap();

        if let Some(existing) = index.images.get_mut(&relative_path) {
            // Update existing entry
            if !existing.used_by.contains(&used_by_file) {
                existing.used_by.push(used_by_file.clone());
                debug!("Added '{}' to existing image reference '{}'", used_by_file, relative_path);
            }
            existing.last_verified = now;
        } else {
            // Create new entry
            let reference = ImageReference {
                hash,
                original_name,
                added_at: now.clone(),
                used_by: vec![used_by_file.clone()],
                size_bytes,
                last_verified: now,
            };

            index.images.insert(relative_path.clone(), reference);
            debug!("Registered new image '{}' used by '{}'", relative_path, used_by_file);
        }

        drop(index);
        self.save_index()?;

        Ok(())
    }

    /// Update image references for a specific file
    pub fn update_image_references(
        &self,
        file_path: String,
        image_paths: Vec<String>,
    ) -> Result<()> {
        let mut index = self.index.lock().unwrap();
        let now = chrono::Utc::now().to_rfc3339();

        // Remove file_path from all images that no longer reference it
        for (_, image_ref) in index.images.iter_mut() {
            if image_ref.used_by.contains(&file_path) && !image_paths.contains(&image_ref.hash) {
                image_ref.used_by.retain(|f| f != &file_path);
                debug!("Removed '{}' from image references", file_path);
            }
        }

        // Add file_path to images that now reference it
        for img_path in image_paths.iter() {
            if let Some(image_ref) = index.images.get_mut(img_path)
                && !image_ref.used_by.contains(&file_path) {
                    image_ref.used_by.push(file_path.clone());
                    image_ref.last_verified = now.clone();
                    debug!("Added '{}' to image '{}' references", file_path, img_path);
                }
        }

        drop(index);
        self.save_index()?;

        Ok(())
    }

    /// Validate the entire image index
    pub fn validate_index(&self) -> Result<ValidationReport> {
        let mut index = self.index.lock().unwrap();
        let now = chrono::Utc::now().to_rfc3339();

        let mut missing_images = Vec::new();
        let total_images = index.images.len();

        // Check each registered image
        for (relative_path, image_ref) in index.images.iter_mut() {
            let absolute_path = self.workspace_path.join(relative_path);

            if !absolute_path.exists() {
                missing_images.push(MissingImageInfo {
                    path: relative_path.clone(),
                    original_name: image_ref.original_name.clone(),
                    used_by: image_ref.used_by.clone(),
                });
                warn!("Missing image detected: {}", relative_path);
            } else {
                image_ref.last_verified = now.clone();
            }
        }

        // Find orphaned images (in assets but not in index)
        let orphaned_images = self.find_orphaned_images(&index)?;

        if !missing_images.is_empty() {
            warn!("Validation found {} missing images", missing_images.len());
        }

        if !orphaned_images.is_empty() {
            info!("Validation found {} orphaned images", orphaned_images.len());
        }

        drop(index);
        if missing_images.is_empty() && orphaned_images.is_empty() {
            self.save_index()?;
        }

        Ok(ValidationReport {
            total_images,
            missing_images,
            orphaned_images,
        })
    }

    /// Find images in assets directory that are not in the index
    fn find_orphaned_images(&self, index: &ImageIndex) -> Result<Vec<String>> {
        let assets_dir = self.workspace_path.join(".lore/assets/images");
        
        if !assets_dir.exists() {
            return Ok(Vec::new());
        }

        let mut orphaned = Vec::new();

        let entries = fs::read_dir(&assets_dir)
            .context("Failed to read assets directory")?;

        for entry in entries {
            let entry = entry.context("Failed to read directory entry")?;
            let path = entry.path();

            if path.is_file()
                && let Ok(relative) = path.strip_prefix(&self.workspace_path) {
                    let relative_str = relative.to_string_lossy().replace('\\', "/");
                    
                    if !index.images.contains_key(&relative_str) {
                        orphaned.push(relative_str);
                    }
                }
        }

        Ok(orphaned)
    }

    /// Find candidate images by hash (for relocated images)
    pub async fn find_image_candidates(
        &self,
        missing_hash: String,
    ) -> Result<Vec<String>> {
        let assets_dir = self.workspace_path.join(".lore/assets/images");
        
        if !assets_dir.exists() {
            return Ok(Vec::new());
        }

        let mut candidates = Vec::new();

        let mut entries = tokio::fs::read_dir(&assets_dir).await
            .context("Failed to read assets directory")?;

        while let Some(entry) = entries.next_entry().await
            .context("Failed to read directory entry")? {
            
            let path = entry.path();

            if path.is_file() {
                let hash = Self::calculate_file_hash(&path).await?;
                
                if hash == missing_hash
                    && let Ok(relative) = path.strip_prefix(&self.workspace_path) {
                        let relative_str = relative.to_string_lossy().replace('\\', "/");
                        candidates.push(relative_str);
                    }
            }
        }

        Ok(candidates)
    }

    /// Bulk update image path (after move/rename operation)
    pub fn bulk_update_image_path(
        &self,
        old_path: String,
        new_path: String,
    ) -> Result<BulkUpdateResult> {
        let mut index = self.index.lock().unwrap();

        let used_by = if let Some(image_ref) = index.images.remove(&old_path) {
            let new_ref = ImageReference {
                last_verified: chrono::Utc::now().to_rfc3339(),
                ..image_ref.clone()
            };
            
            index.images.insert(new_path.clone(), new_ref);
            image_ref.used_by
        } else {
            return Err(anyhow::anyhow!("Image not found in index: {}", old_path));
        };

        drop(index);

        let mut updated_files = Vec::new();
        let mut failed_files = Vec::new();

        // Update frontmatter in each file that uses this image
        for file_path in used_by {
            let absolute_file_path = self.workspace_path.join(&file_path);
            
            match self.update_file_frontmatter(&absolute_file_path, &old_path, &new_path) {
                Ok(_) => {
                    updated_files.push(file_path);
                }
                Err(e) => {
                    error!("Failed to update {}: {}", file_path, e);
                    failed_files.push(file_path);
                }
            }
        }

        self.save_index()?;

        info!("Bulk update: {} files updated, {} failed", updated_files.len(), failed_files.len());

        Ok(BulkUpdateResult {
            updated_files,
            failed_files,
        })
    }

    /// Update frontmatter in a file to replace old image path with new one
    fn update_file_frontmatter(
        &self,
        file_path: &Path,
        old_path: &str,
        new_path: &str,
    ) -> Result<()> {
        let content = fs::read_to_string(file_path)
            .context(format!("Failed to read file: {}", file_path.display()))?;

        let updated_content = content.replace(old_path, new_path);

        fs::write(file_path, updated_content)
            .context(format!("Failed to write file: {}", file_path.display()))?;

        debug!("Updated image path in {}", file_path.display());
        Ok(())
    }
}