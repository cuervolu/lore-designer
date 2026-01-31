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

    #[error("Failed to parse frontmatter: {0}")]
    FrontmatterError(#[from] gray_matter::Error),
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
    Location,
    Image,
    Lore,
    Dialogue,
    Unknown,
}

impl FileType {
    /// Determine file type from path
    pub fn from_path(path: &Path) -> Result<Self, EditorError> {
        if let Some(file_name) = path.file_name().and_then(|n| n.to_str()) {
            if file_name.ends_with(".character.md") {
                return Ok(FileType::Character);
            } else if file_name.ends_with(".location.md") {
                return Ok(FileType::Location);
            } else if file_name.ends_with(".lore.md") {
                return Ok(FileType::Lore);
            } else if file_name.ends_with(".dialogue.md") || file_name.ends_with(".dialogue.json") {
                return Ok(FileType::Dialogue);
            } else if file_name.ends_with(".canvas.json") {
                return Ok(FileType::Canvas);
            }
        }
        if let Some(ext) = path.extension().and_then(|e| e.to_str()) {
            match ext.to_lowercase().as_str() {
                // Generic Markdown is now a fallback after specific types failed
                "md" => return Ok(FileType::Markdown),
                "canvas" => return Ok(FileType::Canvas), // Keep if you allow just .canvas
                "dialogue" => return Ok(FileType::Dialogue), // Keep if you allow just .dialogue
                "json" => {
                    // Maybe check if filename contains .canvas? Or rely on specific check above.
                    // For now, could be Unknown or maybe a specific JSON type if context allows.
                    return Ok(FileType::Unknown); // Or handle generic JSON differently
                }
                "png" | "jpg" | "jpeg" | "webp" | "svg" => return Ok(FileType::Image),
                _ => {} // Fall through if extension doesn't match known simple types
            }
        }

        // Default if no specific filename or known extension matched
        Ok(FileType::Unknown)
    }
}

/// Content of a file
#[derive(Debug, Serialize, Deserialize, Clone)]
#[serde(tag = "type", content = "data")]
pub enum FileContent {
    Markdown {
        content: String,
    },
    Canvas {
        data: String,
    }, // JSON string
    Character {
        frontmatter: Option<String>,
        content: String,
    },
    Location {
        frontmatter: Option<String>,
        content: String,
    },
    Lore {
        frontmatter: Option<String>,
        content: String,
    },
    Dialogue {
        data: String,
    }, // Could be Markdown or JSON
    Image {
        path: String,
    },
    PlainText {
        content: String,
    },
}

/// Request to save a file
#[derive(Debug, Serialize, Deserialize)]
#[serde(tag = "type", content = "data")]
pub enum SaveFileRequest {
    Text {
        content: String,
    },
    Json {
        content: String,
    },
    MarkdownWithFrontmatter {
        frontmatter: String,
        content: String,
    },
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

#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct FileSearchResult {
    pub path: String,              // Relative path from workspace root
    pub name: String,              // Display name (from frontmatter or filename)
    pub file_type: FileType,       // Character, Location, Lore, etc.
    pub extension: Option<String>, // .md, .json, etc.
    pub last_modified: u64,        // Timestamp for sorting
}

#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct FrontmatterResult {
    pub path: String,
    pub frontmatter: Option<String>, // YAML string or null
    pub has_frontmatter: bool,
}
