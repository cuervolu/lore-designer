use super::{manifest, WorkspaceManager};
use crate::core::error::AppError;
use log::{error, info};
use serde::Serialize;
use tauri::{command, AppHandle};

/// Response structure for workspace operations
#[derive(Debug, Serialize)]
pub struct WorkspaceResponse {
    /// Path where the workspace was created
    pub path: String,

    /// Status message
    pub message: String,
}

/// Create a new workspace
///
/// # Arguments
/// * `app` - Tauri AppHandle for accessing app info
/// * `name` - Name of the workspace to create
/// * `location_path` - Base path where to create the workspace
#[command]
pub async fn create_workspace(
    app: AppHandle,
    name: String,
    location_path: String,
) -> Result<WorkspaceResponse, AppError> {
    info!(
        "Attempting to create workspace '{}' at location '{}'",
        name, location_path
    );

    let app_version = app.package_info().version.to_string();

    let result = WorkspaceManager::create_workspace(&name, location_path, &app_version)?;

    if let Err(e) = manifest::create_default_settings(&result) {
        error!("Failed to create default settings: {}", e);
    }

    Ok(WorkspaceResponse {
        path: result.display().to_string(),
        message: format!("Workspace '{}' created successfully.", name),
    })
}
