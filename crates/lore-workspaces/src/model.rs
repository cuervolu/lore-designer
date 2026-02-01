use serde::Serialize;
use thiserror::Error;

/// Core version for workspace format 
pub const WORKSPACE_VERSION: u32 = 1;

#[derive(Error, Debug)]
pub enum WorkspaceError {
    #[error("Filesystem error: {0}")]
    Io(#[from] std::io::Error),

    #[error("TOML serialization error: {0}")]
    Toml(#[from] toml::ser::Error),

    #[error("Invalid path provided: {0}")]
    InvalidPath(String),

    #[error("Workspace directory already exists at: {0}")]
    DirectoryExists(String),

    #[error("Error creating workspace: {0}")]
    Creation(#[from] anyhow::Error),
}

impl Serialize for WorkspaceError {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::ser::Serializer,
    {
        serializer.serialize_str(self.to_string().as_ref())
    }
}

/// Response structure for workspace operations
#[derive(Debug, Serialize)]
pub struct WorkspaceResponse {
    /// Path where the workspace was created
    pub path: String,

    /// Status message
    pub message: String,
}

#[derive(Debug, Serialize)]
pub struct WorkspacePreview {
    pub folders: Vec<FolderInfo>,
}

#[derive(Debug, Serialize)]
pub struct FolderInfo {
    pub name: String,
    pub file_count: usize,
}