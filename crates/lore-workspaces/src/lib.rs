mod commands;
mod manifest;
mod model;

use anyhow::Context;
use log::{debug, info, warn};
use std::fs;
use std::path::{Path, PathBuf};

pub use commands::*;
pub use manifest::*;
pub use model::*;

pub use crate::model::{WorkspaceError, WORKSPACE_VERSION};

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
        name: &str,
        base_path: impl AsRef<Path>,
        app_version: &str,
    ) -> Result<PathBuf, WorkspaceError> {
        let base_path = base_path.as_ref();
        if !base_path.is_dir() {
            return Err(WorkspaceError::InvalidPath(base_path.display().to_string()));
        }

        let workspace_path = base_path.join(name);
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

        // Create the manifest file
        create_manifest(&workspace_path, name, app_version)?;

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
}