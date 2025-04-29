mod commands;
mod icons;
mod manifest;
mod model;
mod recent;

pub use crate::model::{WORKSPACE_VERSION, WorkspaceError};
use anyhow::Context;
pub use commands::*;
pub use icons::*;
use log::{debug, info, warn};
pub use manifest::*;
pub use model::*;
pub use recent::*;
use std::fs;
use std::path::{Path, PathBuf};
use tauri::AppHandle;

pub const DEFAULT_FOLDERS: [&str; 4] = ["Characters", "Lore", "Story", "Notes"];
pub const INTERNAL_DIR: &str = ".lore";
pub const INTERNAL_SUBDIRS: [&str; 2] = ["trash", "logs"];

pub const SETTINGS_FILE_NAME: &str = "settings.toml";
pub const LORE_DESIGNER_EXT: &str = ".lore";

pub struct WorkspaceManager;

impl WorkspaceManager {
    /// Create a new workspace at the specified location
    ///
    /// # Arguments
    /// * `name` - The name of the workspace
    /// * `base_path` - Base directory where the workspace should be created
    /// * `app_version` - Current application version
    pub fn create_workspace(
        app: &AppHandle,
        name: &str,
        base_path: impl AsRef<Path>,
        app_version: &str,
    ) -> Result<PathBuf, WorkspaceError> {
        let base_path = base_path.as_ref();
        if !base_path.is_dir() {
            return Err(WorkspaceError::InvalidPath(base_path.display().to_string()));
        }
        let sanitized_name = Self::sanitize_workspace_name(name);
        let workspace_path = base_path.join(&sanitized_name);
        debug!("Full workspace path: {}", workspace_path.display());

        // Check if the target directory already exists
        if workspace_path.exists() {
            warn!(
                "Workspace directory already exists: {}",
                workspace_path.display()
            );
            return Err(WorkspaceError::DirectoryExists(
                workspace_path.display().to_string(),
            ));
        }

        // Create the main workspace directory
        Self::create_directory_structure(&workspace_path)?;

        // Create the manifest file -- pass the original name for display purposes, but use
        // the sanitized name for the path
        create_manifest(&workspace_path, name, &sanitized_name, app_version)?;

        copy_default_icon_to_workspace(app, &workspace_path)?;

        info!(
            "Workspace '{}' created successfully at {}",
            name,
            workspace_path.display()
        );
        Ok(workspace_path)
    }

    /// Create the directory structure for a new workspace
    fn create_directory_structure(workspace_path: &Path) -> Result<(), WorkspaceError> {
        // Create main workspace directory
        fs::create_dir_all(workspace_path).context(format!(
            "Failed to create workspace directory: {}",
            workspace_path.display()
        ))?;
        debug!("Created workspace directory: {}", workspace_path.display());

        let dot_lore_path = workspace_path.join(INTERNAL_DIR);
        fs::create_dir(&dot_lore_path).context(format!(
            "Failed to create .lore directory: {}",
            dot_lore_path.display()
        ))?;
        debug!("Created internal '.lore' directory");

        for subdir in INTERNAL_SUBDIRS.iter() {
            let subdir_path = dot_lore_path.join(subdir);
            fs::create_dir(&subdir_path).context(format!(
                "Failed to create subdir: {}",
                subdir_path.display()
            ))?;
            debug!("Created internal subdir: {}", subdir_path.display());
        }

        for folder in DEFAULT_FOLDERS.iter() {
            let folder_path = workspace_path.join(folder);
            fs::create_dir(&folder_path).context(format!(
                "Failed to create content folder: {}",
                folder_path.display()
            ))?;
            debug!("Created default folder: {}", folder_path.display());
        }

        info!("Created default user content folders");
        Ok(())
    }

    /// Sanitize a workspace name for filesystem use
    ///
    /// Converts to lowercase, replaces spaces with underscores, and removes special characters
    pub fn sanitize_workspace_name(name: &str) -> String {
        let lowercase = name.to_lowercase();

        let with_underscores = lowercase.replace(' ', "_");

        let sanitized: String = with_underscores
            .chars()
            .filter(|c| c.is_alphanumeric() || *c == '_' || *c == '-')
            .collect();

        sanitized
    }
}
