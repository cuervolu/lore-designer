use thiserror::Error;

#[derive(Error, Debug)]
pub enum AppError {
    #[error("File selection was cancelled or failed")]
    FileSelectionFailed,
    #[error("Failed to create directory: {0}")]
    CreateDirFailed(#[from] std::io::Error),
    #[error("Tauri error: {0}")]
    TauriError(#[from] tauri::Error),
}

impl serde::Serialize for AppError {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::ser::Serializer,
    {
        serializer.serialize_str(self.to_string().as_ref())
    }
}
