use super::{WorkspaceError, WorkspaceManager, WorkspaceResponse, create_default_settings, INTERNAL_DIR, form_config};
use crate::icons::get_workspace_icon_path;
use crate::recent::{
    RecentWorkspace, add_recent_workspace, check_workspace_exists, get_recent_workspaces,
    remove_recent_workspace,
};
use tracing::{error, info};
use std::path::{Path, PathBuf};
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

    let sanitized_name = WorkspaceManager::sanitize_workspace_name(&name);

    Ok(WorkspaceResponse {
        path: result.display().to_string(),
        message: format!(
            "Workspace '{}' created successfully as folder '{}'.",
            name, sanitized_name
        ),
    })
}

#[tauri::command]
pub fn get_workspace_icon(workspace_path: String) -> Result<String, String> {
    let path = Path::new(&workspace_path);

    if let Some(icon_path) = get_workspace_icon_path(path) {
        return Ok(icon_path.to_string_lossy().to_string());
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


#[tauri::command]
pub async fn open_existing_workspace(
    app: AppHandle,
    workspace_path: String,
) -> Result<WorkspaceResponse, WorkspaceError> {
    info!("Opening existing workspace at path: '{}'", workspace_path);

    let path = Path::new(&workspace_path);
    
    if !path.exists() {
        return Err(WorkspaceError::InvalidPath(format!(
            "Directory does not exist: {}", workspace_path
        )));
    }

    if !path.is_dir() {
        return Err(WorkspaceError::InvalidPath(format!(
            "Selected path is not a directory: {}", workspace_path
        )));
    }
    
    let dir_name = path.file_name()
        .and_then(|n| n.to_str())
        .ok_or_else(|| WorkspaceError::InvalidPath(format!(
            "Cannot determine directory name: {}", workspace_path
        )))?;

    let manifest_path = path.join(format!("{}.lore", dir_name));

    if !manifest_path.exists() {
        return Err(WorkspaceError::InvalidPath(format!(
            "Not a valid Lore Designer workspace: Missing manifest file {}.lore", dir_name
        )));
    }

    // Verify .lore directory exists (internal data directory)
    let lore_dir = path.join(INTERNAL_DIR);
    if !lore_dir.exists() || !lore_dir.is_dir() {
        return Err(WorkspaceError::InvalidPath("Not a valid Lore Designer workspace: Missing .lore directory".to_string()));
    }

    // If we've made it here, it's a valid workspace
    // Add to recent workspaces
    if let Err(err) = add_recent_workspace(&app, dir_name.to_string(), workspace_path.clone()) {
        return Err(WorkspaceError::InvalidPath(format!(
            "Failed to add workspace to recent list: {}", err
        )));
    }

    Ok(WorkspaceResponse {
        path: workspace_path.clone(),
        message: format!("Workspace '{}' opened successfully.", dir_name),
    })
}

#[tauri::command]
pub async fn get_character_form_config(workspace_path: PathBuf) -> Result<form_config::FormConfig, String> {
    form_config::get_config(&workspace_path).await
}

#[tauri::command]
pub async fn save_character_form_config(
    workspace_path: PathBuf,
    config: form_config::FormConfig,
) -> Result<(), String> {
    form_config::save_config(&workspace_path, &config).await
}