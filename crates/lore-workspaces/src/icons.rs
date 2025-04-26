use crate::model::WorkspaceError;
use anyhow::Context;
use log::debug;
use std::fs;
use std::path::{Path, PathBuf};
use tauri::{AppHandle, Manager};
use tauri::path::BaseDirectory;

/// Name of the workspace icon file
pub const WORKSPACE_ICON_FILENAME: &str = "icon.webp";

/// Copy the default icon from Tauri resources to a newly created workspace
pub fn copy_default_icon_to_workspace(
    app: &AppHandle,
    workspace_path: &Path,
) -> Result<PathBuf, WorkspaceError> {
    // Resolve the resource path to the default icon
    let icon_resource_path = app
        .path()
        .resolve("../../../icon.webp", BaseDirectory::Resource)
        .context("Failed to resolve icon resource path")?;

    debug!("Default icon resource path: {}", icon_resource_path.display());

    // Create the target path for the icon in the workspace
    let target_icon_path = workspace_path.join(WORKSPACE_ICON_FILENAME);

    fs::copy(&icon_resource_path, &target_icon_path)
        .context(format!("Failed to copy icon to workspace: {}", target_icon_path.display()))?;

    debug!("Created workspace icon at {}", target_icon_path.display());

    Ok(target_icon_path)
}

/// Check if a workspace has an icon
/// Returns the path to the icon if it exists, or None if it doesn't
pub fn get_workspace_icon_path(workspace_path: &Path) -> Option<PathBuf> {
    let icon_path = workspace_path.join(WORKSPACE_ICON_FILENAME);
    if icon_path.exists() {
        Some(icon_path)
    } else {
        None
    }
}