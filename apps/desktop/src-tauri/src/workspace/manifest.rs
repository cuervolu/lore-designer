use crate::core::error::AppError;
use crate::workspace::{LORE_DESIGNER_EXT, SETTINGS_FILE_NAME, WORKSPACE_VERSION};
use anyhow::Context;
use log::info;
use serde::{Deserialize, Serialize};
use std::fs;
use std::path::{Path, PathBuf};
use toml::toml;
use uuid::Uuid;

/// Structure representing the project manifest file
#[derive(Debug, Serialize, Deserialize)]
pub struct ProjectManifest {
    /// Version of the Lore Designer format (for migration/compatibility)
    pub lore_version: u32,

    /// Unique identifier for the project
    pub project_id: String,

    /// User-defined name of the project
    pub project_name: String,

    /// Version of Lore Designer that created the project
    pub created_with_version: String,

    /// Version of Lore Designer that last modified the project
    pub last_opened_with_version: String,

    /// Template used for this project (if any)
    pub template_id: Option<String>,

    /// User-provided description of the project
    pub description: Option<String>,

    /// List of authors/contributors
    pub authors: Option<Vec<String>>,
}

/// Create a new manifest file for a workspace
///
/// # Arguments
/// * `workspace_path` - Path to the workspace directory
/// * `name` - Name of the workspace/project
/// * `app_version` - Current application version
pub fn create_manifest(
    workspace_path: &Path,
    name: &str,
    app_version: &str,
) -> Result<PathBuf, AppError> {
    let manifest_file_name = format!("{}.lore", name);
    let manifest_path = workspace_path.join(&manifest_file_name);

    let manifest_content = ProjectManifest {
        lore_version: WORKSPACE_VERSION,
        project_id: Uuid::new_v4().to_string(),
        project_name: name.to_string(),
        created_with_version: app_version.to_string(),
        last_opened_with_version: app_version.to_string(),
        template_id: None,
        description: None,
        authors: None,
    };

    let toml_string = toml::to_string_pretty(&manifest_content).map_err(AppError::Toml)?;

    fs::write(&manifest_path, toml_string).context(format!(
        "Failed to write manifest file: {}",
        manifest_path.display()
    ))?;

    info!("Created manifest file: {}", manifest_path.display());
    Ok(manifest_path)
}

/// Create a default settings.toml file in the .lore directory
pub fn create_default_settings(workspace_path: &Path) -> Result<PathBuf, AppError> {
    let settings_path = workspace_path
        .join(LORE_DESIGNER_EXT)
        .join(SETTINGS_FILE_NAME);

    // Basic settings structure - can be expanded later
    let settings = toml! {
        [editor]
        theme = "system"
        font_size = 14

        [workspace]
        auto_save = true
        auto_save_interval_seconds = 60
    };

    let toml_string = toml::to_string_pretty(&settings).map_err(AppError::Toml)?;

    fs::write(&settings_path, toml_string).context(format!(
        "Failed to write settings file: {}",
        settings_path.display()
    ))?;

    info!("Created default settings file: {}", settings_path.display());
    Ok(settings_path)
}
