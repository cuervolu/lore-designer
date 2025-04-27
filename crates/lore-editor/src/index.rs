use super::{EditorError, FileTreeItem, FileType, IndexedDirectory, IndexedFile};
use ignore::Walk;
use log::{error, info, warn};
use std::collections::HashMap;
use std::path::Path;
use std::sync::{Arc, LazyLock, Mutex};
use std::time::SystemTime;

/// Special folder names that get custom icons
pub const SPECIAL_FOLDERS: [&str; 5] = ["characters", "lore", "locations", "story", "notes"];

/// Progress of indexing operation
#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct IndexingProgress {
    /// Total files to process
    pub total: usize,

    /// Files processed so far
    pub processed: usize,

    /// Current file being processed
    pub current_file: Option<String>,

    /// Whether indexing is complete
    pub completed: bool,
}

/// Manages workspace indexing
pub struct IndexManager;

// Global state for indexing using LazyLock instead of lazy_static
static INDEXING_STATE: LazyLock<Mutex<HashMap<String, Arc<Mutex<IndexingProgress>>>>> =
    LazyLock::new(|| Mutex::new(HashMap::new()));

impl IndexManager {
    /// Start indexing a workspace
    pub fn start_indexing(workspace_path: &Path) -> Result<(), EditorError> {
        let path_str = workspace_path.display().to_string();

        // Create initial progress state
        let progress = Arc::new(Mutex::new(IndexingProgress {
            total: 0,
            processed: 0,
            current_file: None,
            completed: false,
        }));

        // Store in global state
        {
            let mut state = INDEXING_STATE.lock().unwrap();
            state.insert(path_str.clone(), progress.clone());
        }

        // Start background indexing thread
        let workspace_path = workspace_path.to_path_buf();
        std::thread::spawn(
            move || match Self::perform_indexing(&workspace_path, progress) {
                Ok(_) => info!("Indexing completed for: {}", path_str),
                Err(e) => error!("Indexing failed for {}: {}", path_str, e),
            },
        );

        Ok(())
    }

    /// Get current indexing progress
    pub fn get_indexing_progress(workspace_path: &Path) -> Option<IndexingProgress> {
        let path_str = workspace_path.display().to_string();
        let state = INDEXING_STATE.lock().unwrap();

        state.get(&path_str).map(|progress| {
            let guard = progress.lock().unwrap();
            guard.clone()
        })
    }

    /// Perform the actual indexing (internal method)
    fn perform_indexing(
        workspace_path: &Path,
        progress: Arc<Mutex<IndexingProgress>>,
    ) -> Result<(), EditorError> {
        // First, count the total files (for progress reporting)
        let mut total_files = 0;
        for result in Walk::new(workspace_path) {
            match result {
                Ok(_) => total_files += 1,
                Err(err) => warn!("Error walking directory: {}", err),
            }
        }

        // Update progress with total
        {
            let mut guard = progress.lock().unwrap();
            guard.total = total_files;
        }

        // Create index storage
        let mut files = Vec::new();
        let mut directories = Vec::new();

        // Process each file
        for result in Walk::new(workspace_path) {
            match result {
                Ok(entry) => {
                    let path = entry.path();

                    // Update progress
                    {
                        let mut guard = progress.lock().unwrap();
                        guard.processed += 1;
                        guard.current_file = path
                            .strip_prefix(workspace_path)
                            .ok()
                            .and_then(|p| p.to_str())
                            .map(|s| s.to_string());
                    }

                    // Skip .lore directory
                    if let Ok(rel_path) = path.strip_prefix(workspace_path) {
                        let path_str = rel_path.to_string_lossy();
                        if path_str.starts_with(".lore/") {
                            continue;
                        }
                    }

                    // Process the entry
                    if path.is_file() {
                        if let Ok(indexed_file) = Self::index_file(workspace_path, path) {
                            files.push(indexed_file);
                        }
                    } else if path.is_dir() && path != workspace_path {
                        if let Ok(indexed_dir) = Self::index_directory(workspace_path, path) {
                            directories.push(indexed_dir);
                        }
                    }
                }
                Err(err) => {
                    warn!("Error processing entry: {}", err);
                }
            }

            // Small delay to not hog CPU
            std::thread::sleep(std::time::Duration::from_millis(5));
        }

        // Mark as completed
        {
            let mut guard = progress.lock().unwrap();
            guard.completed = true;
            guard.current_file = None;
        }

        // Save the index to workspace
        Self::save_index(workspace_path, &files, &directories)?;

        Ok(())
    }

    /// Index a single file
    fn index_file(workspace_path: &Path, file_path: &Path) -> Result<IndexedFile, EditorError> {
        let rel_path = file_path
            .strip_prefix(workspace_path)
            .map_err(|_| EditorError::InvalidPath(file_path.display().to_string()))?;

        let metadata = std::fs::metadata(file_path)?;
        let last_modified = metadata
            .modified()
            .map(|time| {
                time.duration_since(SystemTime::UNIX_EPOCH)
                    .unwrap_or_default()
                    .as_secs()
            })
            .unwrap_or(0);

        let name = file_path
            .file_name()
            .and_then(|n| n.to_str())
            .unwrap_or("unnamed")
            .to_string();

        let extension = file_path
            .extension()
            .and_then(|e| e.to_str())
            .map(|s| s.to_string());

        let file_type = FileType::from_path(file_path)?;

        Ok(IndexedFile {
            path: rel_path.display().to_string(),
            name,
            extension,
            file_type,
            last_modified,
            size: metadata.len(),
        })
    }

    /// Index a directory
    fn index_directory(
        workspace_path: &Path,
        dir_path: &Path,
    ) -> Result<IndexedDirectory, EditorError> {
        let rel_path = dir_path
            .strip_prefix(workspace_path)
            .map_err(|_| EditorError::InvalidPath(dir_path.display().to_string()))?;

        let name = dir_path
            .file_name()
            .and_then(|n| n.to_str())
            .unwrap_or("unnamed")
            .to_string();

        // Determine if this is a special folder
        let icon = SPECIAL_FOLDERS
            .iter()
            .find(|&&folder| name.to_lowercase() == folder)
            .map(|&folder| folder.to_string());

        Ok(IndexedDirectory {
            path: rel_path.display().to_string(),
            name,
            icon,
        })
    }

    /// Save index to workspace
    fn save_index(
        workspace_path: &Path,
        files: &[IndexedFile],
        directories: &[IndexedDirectory],
    ) -> Result<(), EditorError> {
        let index_dir = workspace_path.join(".lore");
        std::fs::create_dir_all(&index_dir)?;

        let index_file = index_dir.join("index.json");
        let index_data = serde_json::json!({
            "files": files,
            "directories": directories,
            "timestamp": SystemTime::now()
                .duration_since(SystemTime::UNIX_EPOCH)
                .unwrap_or_default()
                .as_secs()
        });

        let index_json = serde_json::to_string_pretty(&index_data)?;
        std::fs::write(index_file, index_json)?;

        Ok(())
    }

    /// Load index from workspace
    pub fn load_index(
        workspace_path: &Path,
    ) -> Result<(Vec<IndexedFile>, Vec<IndexedDirectory>), EditorError> {
        let index_file = workspace_path.join(".lore").join("index.json");

        if !index_file.exists() {
            return Ok((Vec::new(), Vec::new()));
        }

        let index_json = std::fs::read_to_string(index_file)?;
        let index_data: serde_json::Value = serde_json::from_str(&index_json)?;

        let files: Vec<IndexedFile> = serde_json::from_value(index_data["files"].clone())?;
        let directories: Vec<IndexedDirectory> =
            serde_json::from_value(index_data["directories"].clone())?;

        Ok((files, directories))
    }

    /// Build file tree from indexed data
    pub fn build_file_tree(workspace_path: &Path) -> Result<Vec<FileTreeItem>, EditorError> {
        let (files, directories) = Self::load_index(workspace_path)?;

        // Create a map to store the tree structure
        let mut tree_map: HashMap<String, FileTreeItem> = HashMap::new();

        // Add root
        tree_map.insert(
            "".to_string(),
            FileTreeItem {
                name: workspace_path
                    .file_name()
                    .and_then(|n| n.to_str())
                    .unwrap_or("Workspace")
                    .to_string(),
                path: "".to_string(),
                is_directory: true,
                icon: None,
                children: Vec::new(),
            },
        );

        // Add all directories
        for dir in &directories {
            let parts: Vec<&str> = dir.path.split('/').collect();
            let parent_path = parts[..parts.len() - 1].join("/");

            // Create directory item
            let dir_item = FileTreeItem {
                name: dir.name.clone(),
                path: dir.path.clone(),
                is_directory: true,
                icon: dir.icon.clone(),
                children: Vec::new(),
            };

            // Add to tree map
            tree_map.insert(dir.path.clone(), dir_item);

            // Add to parent's children
            if let Some(parent) = tree_map.get_mut(&parent_path) {
                parent.children.push(FileTreeItem {
                    name: dir.name.clone(),
                    path: dir.path.clone(),
                    is_directory: true,
                    icon: dir.icon.clone(),
                    children: Vec::new(),
                });
            }
        }

        // Add all files
        for file in &files {
            let parts: Vec<&str> = file.path.split('/').collect();
            let parent_path = if parts.len() > 1 {
                parts[..parts.len() - 1].join("/")
            } else {
                "".to_string()
            };

            // Determine file icon based on type
            let icon = match file.file_type {
                FileType::Markdown => Some("markdown".to_string()),
                FileType::Canvas => Some("canvas".to_string()),
                FileType::Character => Some("character".to_string()),
                FileType::Image => Some("image".to_string()),
                FileType::Unknown => None,
            };

            // Add to parent's children
            if let Some(parent) = tree_map.get_mut(&parent_path) {
                parent.children.push(FileTreeItem {
                    name: file.name.clone(),
                    path: file.path.clone(),
                    is_directory: false,
                    icon,
                    children: Vec::new(),
                });
            }
        }

        // Return the root's children
        if let Some(root) = tree_map.get("") {
            return Ok(root.children.clone());
        }

        Ok(Vec::new())
    }
}