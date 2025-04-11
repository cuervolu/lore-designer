use thiserror::Error;

#[derive(Error, Debug)]
pub enum AppError {
    #[error("Store not initialized")]
    StoreNotInitialized,

    #[error("Failed to lock mutex: {0}")]
    MutexError(String),

    #[error("Store error: {0}")]
    StoreError(#[from] tauri_plugin_store::Error),

    #[error("Serialization error: {0}")]
    SerializationError(#[from] serde_json::Error),
}

impl serde::Serialize for AppError {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: serde::ser::Serializer,
    {
        serializer.serialize_str(self.to_string().as_ref())
    }
}
