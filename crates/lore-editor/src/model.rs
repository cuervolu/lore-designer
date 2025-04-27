use lore_workspaces::get_workspace_icon_path;
use serde::{Deserialize, Serialize};
use std::path::Path;
use std::time::SystemTime;
use thiserror::Error;

/// Error type for editor operations
#[derive(Error, Debug)]
pub enum EditorError {
    #[error("Workspace not found: {0}")]
    WorkspaceNotFound(String),

    #[error("File not found: {0}")]
    FileNotFound(String),

    #[error("Invalid path: {0}")]
    InvalidPath(String),

    #[error("IO error: {0}")]
    Io(#[from] std::io::Error),

    #[error("Serialization error: {0}")]
    Serialization(#[from] serde_json::Error),

    #[error("Error in editor operation: {0}")]
    Operation(#[from] anyhow::Error),

    #[error("Workspace error: {0}")]
    Workspace(#[from] lore_workspaces::WorkspaceError),
}

impl Serialize for EditorError {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::ser::Serializer,
    {
        serializer.serialize_str(self.to_string().as_ref())
    }
}

/// Basic workspace information
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct WorkspaceInfo {
    /// Path to the workspace
    pub path: String,

    /// Name of the workspace
    pub name: String,

    /// Icon path (if available)
    pub icon_path: Option<String>,

    /// Last modification time
    pub last_modified: u64,
}

impl WorkspaceInfo {
    pub fn from_path(path: &Path) -> Result<Self, EditorError> {
        let name = path
            .file_name()
            .and_then(|n| n.to_str())
            .unwrap_or("Unnamed Workspace")
            .to_string();

        let icon_path = get_workspace_icon_path(path).map(|p| p.display().to_string());

        let metadata = std::fs::metadata(path)?;
        let last_modified = metadata
            .modified()
            .map(|time| {
                time.duration_since(SystemTime::UNIX_EPOCH)
                    .unwrap_or_default()
                    .as_secs()
            })
            .unwrap_or(0);

        Ok(Self {
            path: path.display().to_string(),
            name,
            icon_path,
            last_modified,
        })
    }
}

/// File type enumeration
#[derive(Debug, Serialize, Deserialize, Clone, PartialEq)]
pub enum FileType {
    Markdown,
    Canvas,
    Character,
    Image,
    Unknown,
}

impl FileType {
    /// Determine file type from path
    pub fn from_path(path: &Path) -> Result<Self, EditorError> {
        if let Some(ext) = path.extension().and_then(|e| e.to_str()) {
            match ext.to_lowercase().as_str() {
                "md" => return Ok(FileType::Markdown),
                "canvas" => return Ok(FileType::Canvas),
                "character" => return Ok(FileType::Character),
                "png" | "jpg" | "jpeg" | "webp" | "svg" => return Ok(FileType::Image),
                _ => {}
            }
        }

        // Also check if the filename contains the extension as part of its name
        if let Some(file_name) = path.file_name().and_then(|n| n.to_str()) {
            if file_name.ends_with(".character.json") {
                return Ok(FileType::Character);
            } else if file_name.ends_with(".canvas.json") {
                return Ok(FileType::Canvas);
            }
        }

        Ok(FileType::Unknown)
    }
}

/// Content of a file
#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(tag = "type", content = "data")]
pub enum FileContent {
    Markdown { content: String },
    Canvas { data: String },    // JSON string
    Character { data: String }, // JSON string
    Image { path: String },
    PlainText { content: String },
}

/// Request to save a file
#[derive(Debug, Serialize, Deserialize)]
#[serde(tag = "type", content = "data")]
pub enum SaveFileRequest {
    Text { content: String },
    Json { content: String },
}

/// Indexed file information
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct IndexedFile {
    /// Path relative to workspace root
    pub path: String,

    /// File name
    pub name: String,

    /// File extension (if any)
    pub extension: Option<String>,

    /// File type
    pub file_type: FileType,

    /// Last modification time
    pub last_modified: u64,

    /// File size in bytes
    pub size: u64,
}

/// Directory in the workspace
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct IndexedDirectory {
    /// Path relative to workspace root
    pub path: String,

    /// Directory name
    pub name: String,

    /// Directory icon (based on special folders)
    pub icon: Option<String>,
}

/// Workspace file tree structure
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct FileTreeItem {
    /// File or directory name
    pub name: String,

    /// Path relative to workspace
    pub path: String,

    /// Whether this is a directory
    pub is_directory: bool,

    /// Icon to use (for special folders or file types)
    pub icon: Option<String>,

    /// Children (only for directories)
    pub children: Vec<FileTreeItem>,
}
