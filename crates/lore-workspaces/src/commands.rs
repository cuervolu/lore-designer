use super::{WorkspaceError, WorkspaceManager, WorkspaceResponse, create_default_settings};
use crate::icons::get_workspace_icon_path;
use crate::recent::{
    RecentWorkspace, add_recent_workspace, check_workspace_exists, get_recent_workspaces,
    remove_recent_workspace,
};
use log::{error, info};
use std::path::Path;
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

    let result = WorkspaceManager::create_workspace(&app, &name, location_path, &app_version)?;

    if let Err(e) = create_default_settings(&result) {
        error!("Failed to create default settings: {}", e);
    }

    Ok(WorkspaceResponse {
        path: result.display().to_string(),
        message: format!("Workspace '{}' created successfully.", name),
    })
}

#[tauri::command]
pub fn get_workspace_icon(workspace_path: String) -> Result<String, String> {
    let path = Path::new(&workspace_path);

    if let Some(icon_path) = get_workspace_icon_path(path) {
        return Ok(icon_path.to_string_lossy().to_string());
    }

    // If the icon is not found, return an error. In the frontend we will
    // use the default application icon.
    Err("Workspace icon not found, use application default icon".to_string())
}

#[tauri::command]
pub async fn get_recent_workspaces_command(app: AppHandle) -> Result<Vec<RecentWorkspace>, String> {
    get_recent_workspaces(&app)
}

#[tauri::command]
pub async fn add_recent_workspace_command(
    app: AppHandle,
    name: String,
    path: String,
) -> Result<(), String> {
    add_recent_workspace(&app, name, path)
}

#[tauri::command]
pub async fn remove_recent_workspace_command(app: AppHandle, path: String) -> Result<(), String> {
    remove_recent_workspace(&app, path)
}

#[tauri::command]
pub fn check_workspace_exists_command(path: String) -> bool {
    check_workspace_exists(&path)
}
