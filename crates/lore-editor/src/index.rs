use super::{EditorError, FileTreeItem, FileType, IndexedDirectory, IndexedFile};
use ignore::Walk;
use log::{debug, error, info, warn};
use std::collections::HashMap;
use std::path::Path;
use std::sync::{Arc, LazyLock, Mutex};
use std::time::SystemTime;

/// Special folder names that get custom icons
pub const SPECIAL_FOLDERS: [&str; 5] = ["characters", "lore", "locations", "story", "notes"];

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

pub struct IndexManager;

static INDEXING_STATE: LazyLock<Mutex<HashMap<String, Arc<Mutex<IndexingProgress>>>>> =
    LazyLock::new(|| Mutex::new(HashMap::new()));

impl IndexManager {
    pub fn start_indexing(workspace_path: &Path) -> Result<(), EditorError> {
        let path_str = workspace_path.display().to_string();

        let progress = Arc::new(Mutex::new(IndexingProgress {
            total: 0,
            processed: 0,
            current_file: None,
            completed: false,
        }));

        {
            let mut state = INDEXING_STATE.lock().unwrap();
            state.insert(path_str.clone(), progress.clone());
        }

        let workspace_path = workspace_path.to_path_buf();
        std::thread::spawn(
            move || match Self::perform_indexing(&workspace_path, progress) {
                Ok(_) => info!("Indexing completed for: {}", path_str),
                Err(e) => error!("Indexing failed for {}: {}", path_str, e),
            },
        );

        Ok(())
    }

    pub fn trigger_incremental_update(
        workspace_path: &Path,
        changed_path: &Path,
    ) -> Result<(), EditorError> {
        debug!(
            "Performing incremental update for: {}",
            changed_path.display()
        );

        let (mut files, mut directories) = Self::load_index(workspace_path)?;

        let rel_path = changed_path
            .strip_prefix(workspace_path)
            .map_err(|_| EditorError::InvalidPath(changed_path.display().to_string()))?;

        let rel_path_str = rel_path.display().to_string();

        // If the file exists, it was created or modified?
        if changed_path.exists() {
            if changed_path.is_file() {
                files.retain(|f| f.path != rel_path_str);

                match Self::index_file(workspace_path, changed_path) {
                    Ok(indexed_file) => {
                        debug!("Incrementally indexed file: {}", rel_path_str);
                        files.push(indexed_file);
                    }
                    Err(e) => {
                        error!("Failed to index file {}: {}", rel_path_str, e);
                    }
                }
            } else if changed_path.is_dir() {
                directories.retain(|d| d.path != rel_path_str);

                match Self::index_directory(workspace_path, changed_path) {
                    Ok(indexed_dir) => {
                        debug!("Incrementally indexed directory: {}", rel_path_str);
                        directories.push(indexed_dir);
                    }
                    Err(e) => {
                        error!("Failed to index directory {}: {}", rel_path_str, e);
                    }
                }
            }
        } else {
            files.retain(|f| !f.path.starts_with(&rel_path_str));
            directories.retain(|d| !d.path.starts_with(&rel_path_str));
            debug!("Removed deleted path from index: {}", rel_path_str);
        }

        Self::save_index(workspace_path, &files, &directories)?;
        debug!("Incremental index update completed");

        Ok(())
    }

    pub fn get_indexing_progress(workspace_path: &Path) -> Option<IndexingProgress> {
        let path_str = workspace_path.display().to_string();
        let state = INDEXING_STATE.lock().unwrap();

        state.get(&path_str).map(|progress| {
            let guard = progress.lock().unwrap();
            guard.clone()
        })
    }

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

        {
            let mut guard = progress.lock().unwrap();
            guard.total = total_files;
        }

        let mut files = Vec::new();
        let mut directories = Vec::new();

        for result in Walk::new(workspace_path) {
            match result {
                Ok(entry) => {
                    let path = entry.path();

                    {
                        let mut guard = progress.lock().unwrap();
                        guard.processed += 1;
                        guard.current_file = path
                            .strip_prefix(workspace_path)
                            .ok()
                            .and_then(|p| p.to_str())
                            .map(|s| s.to_string());
                    }
                    if let Ok(rel_path) = path.strip_prefix(workspace_path) {
                        let path_str = rel_path.to_string_lossy();
                        if path_str.starts_with(".lore/") {
                            continue;
                        }
                    }

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

        {
            let mut guard = progress.lock().unwrap();
            guard.completed = true;
            guard.current_file = None;
        }
        Self::save_index(workspace_path, &files, &directories)?;

        Ok(())
    }

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

    pub fn build_file_tree(workspace_path: &Path) -> Result<Vec<FileTreeItem>, EditorError> {
        debug!(
            "Building file tree for workspace: {}",
            workspace_path.display()
        );

        let (files, directories) = Self::load_index(workspace_path)?;
        debug!(
            "Loaded index: {} files, {} directories",
            files.len(),
            directories.len()
        );

        let mut tree_map: HashMap<String, FileTreeItem> = HashMap::new();

        for dir in &directories {
            debug!(
                "Creating directory node: {} at path: {}",
                dir.name, dir.path
            );
            let dir_item = FileTreeItem {
                name: dir.name.clone(),
                path: dir.path.clone(),
                is_directory: true,
                icon: dir.icon.clone(),
                children: Vec::new(), // Initially empty, will be filled later
            };
            tree_map.insert(dir.path.clone(), dir_item);
        }

        for file in &files {
            debug!("Creating file node: {} at path: {}", file.name, file.path);
            let icon = match file.file_type {
                FileType::Markdown => Some("markdown".to_string()),
                FileType::Canvas => Some("canvas".to_string()),
                FileType::Character => Some("character".to_string()),
                FileType::Location => Some("location".to_string()),
                FileType::Lore => Some("book".to_string()),
                FileType::Dialogue => Some("message-square".to_string()),
                FileType::Image => Some("image".to_string()),
                _ => None,
            };
            let file_item = FileTreeItem {
                name: file.name.clone(),
                path: file.path.clone(),
                is_directory: false,
                icon,
                children: Vec::new(), // Files don't have children
            };
            tree_map.insert(file.path.clone(), file_item);
        }

        let paths: Vec<String> = tree_map.keys().cloned().collect();
        let mut root_children_paths: Vec<String> = Vec::new();

        for path in paths {
            let parts: Vec<&str> = path.split('/').collect();

            let parent_path = if parts.len() <= 1 {
                root_children_paths.push(path.clone());
                continue;
            } else {
                parts[..parts.len() - 1].join("/")
            };

            if let Some(child_item) = tree_map.remove(&path) {
                debug!(
                    "Linking item: {} to parent: {}",
                    child_item.path, parent_path
                );
                if let Some(parent) = tree_map.get_mut(&parent_path) {
                    parent.children.push(child_item);
                    debug!(
                        "Added item {} to parent {}, parent now has {} children",
                        path,
                        parent_path,
                        parent.children.len()
                    );
                } else {
                    warn!(
                        "Parent directory not found in tree_map for item: {} (parent: {}) - Re-inserting item.",
                        path, parent_path
                    );
                    tree_map.insert(path, child_item);
                    if !parent_path.is_empty() {
                        warn!("Parent path not found in tree_map: {}", parent_path);
                    }
                }
            } else {
                warn!("Item path not found during linking phase: {}", path);
            }
        }

        let mut result: Vec<FileTreeItem> = Vec::new();
        for root_child_path in root_children_paths {
            if let Some(item) = tree_map.remove(&root_child_path) {
                result.push(item);
            } else {
                warn!(
                    "Root child path not found in final map: {}",
                    root_child_path
                );
            }
        }
        result.sort_by(|a, b| match (a.is_directory, b.is_directory) {
            (true, false) => std::cmp::Ordering::Less,
            (false, true) => std::cmp::Ordering::Greater,
            _ => a.name.cmp(&b.name),
        });

        debug!(
            "Returning final tree structure with {} root items.",
            result.len()
        );
        Ok(result)
    }
}
