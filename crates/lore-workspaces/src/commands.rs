use super::{WorkspaceError, WorkspaceManager, WorkspaceResponse, create_default_settings};
use crate::icons::get_workspace_icon_path;
use crate::recent::{
    RecentWorkspace, add_recent_workspace, check_workspace_exists, get_recent_workspaces,
    remove_recent_workspace,
};
use base64::Engine;
use base64::engine::general_purpose;
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

    // This is a workaround for the issue where the asset protocol does not allow the path
    // The problem is that the user can have a workspace wherever they want, how could I predict that?
    // The Tauri docs aren't clear on this, but it seems that the asset protocol is not meant to be
    // used for arbitrary file paths. See <https://v2.tauri.app/reference/config/#assetprotocolconfig>
    // and <https://v2.tauri.app/reference/config/#fsscope>
    // So, I ask Claude how to fix this thing, and he came up with this solution, it's not perfect,
    // but it works for now. Maybe in the future I can find a better solution.
    // One potential drawback is that for very large images, the base64 encoding will increase the
    // size by about 33%, but for small UI icons this shouldn't be an issue. Unless the user has
    // a big ass image as an icon, but I don't think that's a common use case.
    if let Some(icon_path) = get_workspace_icon_path(path) {
        return match std::fs::read(&icon_path) {
            Ok(bytes) => {
                // Determine MIME type based on file extension
                let mime_type = match icon_path.extension().and_then(|e| e.to_str()) {
                    Some("webp") => "image/webp",
                    Some("png") => "image/png",
                    Some("jpg") | Some("jpeg") => "image/jpeg",
                    Some("svg") => "image/svg+xml",
                    _ => "application/octet-stream",
                };

                let base64 = general_purpose::STANDARD.encode(&bytes);

                let data_url = format!("data:{};base64,{}", mime_type, base64);

                Ok(data_url)
            }
            Err(e) => Err(format!("Failed to read icon file: {}", e)),
        };
    }
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
