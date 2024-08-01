use thiserror::Error;

#[derive(Error, Debug)]
pub enum AppError {
    #[error("File selection was cancelled or failed")]
    FileSelectionFailed,
    #[error("Tauri error: {0}")]
    TauriError(#[from] tauri::Error),
    #[error("Failed to get app data directory")]
    AppDataDirError,
    #[error("Database error: {0}")]
    DatabaseError(#[from] sqlx::Error),
    #[error("File system error: {0}")]
    FileSystemError(#[from] std::io::Error),
    // #[error("Failed to execute database query")]
    // QueryExecutionError,
    #[error("Failed to fetch GitHub release notes: {0}")]
    GithubError(String),
    #[error("Failed to open browser")]
    ShellError(#[from] tauri_plugin_shell::Error),
}

impl serde::Serialize for AppError {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::ser::Serializer,
    {
        serializer.serialize_str(self.to_string().as_ref())
    }
}
