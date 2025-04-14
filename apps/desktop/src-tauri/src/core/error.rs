use thiserror::Error;

#[derive(Error, Debug)]
pub enum AppError {
    #[error("Store error: {0}")]
    StoreError(#[from] tauri_plugin_store::Error),

    #[error("Serialization error: {0}")]
    SerializationError(#[from] serde_json::Error),

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

impl serde::Serialize for AppError {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::ser::Serializer,
    {
        serializer.serialize_str(self.to_string().as_ref())
    }
}
