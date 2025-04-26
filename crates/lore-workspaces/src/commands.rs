use super::{create_default_settings, WorkspaceManager, WorkspaceError, WorkspaceResponse};
use log::{error, info};
use tauri::AppHandle;


#[tauri::command]
pub async fn create_workspace(
    app: AppHandle,
    name: String,
    location_path: String,
) -> Result<WorkspaceResponse, WorkspaceError> {
    info!(
        "Attempting to create workspace '{}' at location '{}'",
        name, location_path
    );

    let app_version = app.package_info().version.to_string();

    let result = WorkspaceManager::create_workspace(&name, location_path, &app_version)?;

    if let Err(e) = create_default_settings(&result) {
        error!("Failed to create default settings: {}", e);
    }

    Ok(WorkspaceResponse {
        path: result.display().to_string(),
        message: format!("Workspace '{}' created successfully.", name),
    })
}