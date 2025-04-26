use crate::model::WorkspaceError;
use anyhow::Context;
use log::debug;
use std::fs;
use std::path::{Path, PathBuf};
use tauri::{AppHandle, Manager};
use tauri::path::BaseDirectory;

/// Name of the workspace icon file
pub const DEFAULT_ICON_FILENAME: &str = "icon.webp";

/// Supported icon formats (in order of preference)
pub const SUPPORTED_ICON_FORMATS: [&str; 4] = ["icon.webp", "icon.png", "icon.svg", "icon.jpg"];

/// Check if a file extension is a supported icon format
pub fn is_supported_icon_extension(ext: &str) -> bool {
    matches!(ext.to_lowercase().as_str(), "webp" | "png" | "svg" | "jpg" | "jpeg")
}

/// Copy the default icon from Tauri resources to a newly created workspace
pub fn copy_default_icon_to_workspace(
    app: &AppHandle,
    workspace_path: &Path,
) -> Result<PathBuf, WorkspaceError> {
    let icon_resource_path = app
        .path()
        .resolve("../../../icon.webp", BaseDirectory::Resource)
        .context("Failed to resolve icon resource path")?;

    debug!("Default icon resource path: {}", icon_resource_path.display());

    // Create the target path for the icon in the workspace
    let target_icon_path = workspace_path.join(DEFAULT_ICON_FILENAME);

    fs::copy(&icon_resource_path, &target_icon_path)
        .context(format!("Failed to copy icon to workspace: {}", target_icon_path.display()))?;

    debug!("Created workspace icon at {}", target_icon_path.display());

    Ok(target_icon_path)
}

/// Check if a workspace has an icon
/// Returns the path to the icon if it exists, or None if it doesn't
pub fn get_workspace_icon_path(workspace_path: &Path) -> Option<PathBuf> {
    // Check for each supported format
    for format in SUPPORTED_ICON_FORMATS.iter() {
        let icon_path = workspace_path.join(format);
        if icon_path.exists() {
            return Some(icon_path);
        }
    }

    // No icon found
    None
}