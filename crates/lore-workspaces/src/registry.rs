use std::{
    fs, io,
    path::{Path, PathBuf},
};

use chrono::Utc;
use serde::Serialize;
use thiserror::Error;

use crate::models::{
    CreateWorkspaceRequest, CreateWorkspaceResult, WorkspaceTemplateSummary, WorkspaceVersion,
};

const INTERNAL_DIR: &str = ".lore";
const SETTINGS_FILE: &str = "settings.toml";
const STATE_FILE: &str = "state.json";

pub fn list_workspace_templates() -> Vec<WorkspaceTemplateSummary> {
    builtin_templates()
        .iter()
        .map(|definition| definition.summary())
        .collect()
}

pub fn create_workspace(
    request: CreateWorkspaceRequest,
) -> Result<CreateWorkspaceResult, WorkspaceError> {
    let definition = builtin_templates()
        .into_iter()
        .find(|definition| definition.id == request.template_id)
        .ok_or_else(|| WorkspaceError::UnknownTemplate(request.template_id.clone()))?;

    if !definition.supports_creation {
        return Err(WorkspaceError::TemplateUnavailable(
            definition.id.to_string(),
        ));
    }

    let validated_name = validate_name(&request.name)?;
    let root_path = validate_root_path(&request.path)?;

    ensure_destination_ready(&root_path)?;

    definition.create(validated_name, &root_path, &request.app_version)
}

#[derive(Debug, Error)]
pub enum WorkspaceError {
    #[error("Workspace name cannot be empty.")]
    EmptyName,

    #[error("Workspace name must be at least 2 characters.")]
    NameTooShort,

    #[error("Workspace path cannot be empty.")]
    EmptyPath,

    #[error("Template '{0}' does not exist.")]
    UnknownTemplate(String),

    #[error("Template '{0}' is not available for creation yet.")]
    TemplateUnavailable(String),

    #[error("A file already exists at '{0}'.")]
    DestinationIsFile(String),

    #[error("The destination folder '{0}' already exists and is not empty.")]
    DestinationNotEmpty(String),

    #[error("Unable to create workspace: {0}")]
    Io(#[from] io::Error),
}

#[derive(Debug, Clone, Copy)]
struct TemplateDefinition {
    id: &'static str,
    display_name: &'static str,
    description: &'static str,
    supports_creation: bool,
    sort_order: u16,
    create_fn: fn(&str, &Path, &str) -> Result<CreateWorkspaceResult, WorkspaceError>,
}

impl TemplateDefinition {
    fn summary(self) -> WorkspaceTemplateSummary {
        WorkspaceTemplateSummary {
            id: self.id.to_string(),
            display_name: self.display_name.to_string(),
            description: self.description.to_string(),
            supports_creation: self.supports_creation,
            is_builtin: true,
            sort_order: self.sort_order,
        }
    }

    fn create(
        self,
        name: &str,
        root_path: &Path,
        app_version: &str,
    ) -> Result<CreateWorkspaceResult, WorkspaceError> {
        (self.create_fn)(name, root_path, app_version)
    }
}

fn builtin_templates() -> [TemplateDefinition; 4] {
    [
        TemplateDefinition {
            id: "blank",
            display_name: "Blank project",
            description: "An empty workspace. Add characters, places, and drafts as you go.",
            supports_creation: true,
            sort_order: 0,
            create_fn: create_blank_workspace,
        },
        TemplateDefinition {
            id: "novel",
            display_name: "Long-form novel",
            description: "Pre-organized for chapters, character files, and a places index.",
            supports_creation: false,
            sort_order: 1,
            create_fn: unsupported_template,
        },
        TemplateDefinition {
            id: "game-bible",
            display_name: "Game / narrative bible",
            description: "Factions, regions, and quest-style threads for ongoing development.",
            supports_creation: false,
            sort_order: 2,
            create_fn: unsupported_template,
        },
        TemplateDefinition {
            id: "sample",
            display_name: "Sample — Saltreach Cycle",
            description: "A small worked example showing how characters, places, and drafts link together.",
            supports_creation: false,
            sort_order: 3,
            create_fn: unsupported_template,
        },
    ]
}

fn unsupported_template(
    _name: &str,
    _root_path: &Path,
    app_version: &str,
) -> Result<CreateWorkspaceResult, WorkspaceError> {
    Err(WorkspaceError::TemplateUnavailable(app_version.to_string()))
}

fn create_blank_workspace(
    name: &str,
    root_path: &Path,
    app_version: &str,
) -> Result<CreateWorkspaceResult, WorkspaceError> {
    fs::create_dir_all(root_path)?;

    let internal_dir = root_path.join(INTERNAL_DIR);
    fs::create_dir_all(&internal_dir)?;

    let manifest_name = format!("{}.lore", slugify_project_name(name));
    let manifest_path = root_path.join(manifest_name);
    let created_at = Utc::now();

    let manifest = WorkspaceManifest {
        name,
        workspace_version: WorkspaceVersion::default().major,
        created_with: app_version,
        created_at: created_at.to_rfc3339(),
        template_id: "blank",
    };

    fs::write(&manifest_path, toml::to_string_pretty(&manifest)?)?;
    fs::write(internal_dir.join(SETTINGS_FILE), "")?;
    fs::write(internal_dir.join(STATE_FILE), "{}\n")?;

    Ok(CreateWorkspaceResult {
        name: name.to_string(),
        root_path: root_path.display().to_string(),
        manifest_path: manifest_path.display().to_string(),
        created_at: created_at.to_rfc3339(),
        template_id: "blank".to_string(),
        workspace_version: WorkspaceVersion::default(),
    })
}

fn validate_name(name: &str) -> Result<&str, WorkspaceError> {
    let trimmed = name.trim();
    if trimmed.is_empty() {
        return Err(WorkspaceError::EmptyName);
    }

    if trimmed.chars().count() < 2 {
        return Err(WorkspaceError::NameTooShort);
    }

    Ok(trimmed)
}

fn validate_root_path(path: &str) -> Result<PathBuf, WorkspaceError> {
    let trimmed = path.trim();
    if trimmed.is_empty() {
        return Err(WorkspaceError::EmptyPath);
    }

    Ok(PathBuf::from(trimmed))
}

fn ensure_destination_ready(path: &Path) -> Result<(), WorkspaceError> {
    match fs::metadata(path) {
        Ok(metadata) if metadata.is_file() => Err(WorkspaceError::DestinationIsFile(
            path.display().to_string(),
        )),
        Ok(_) => {
            let mut entries = fs::read_dir(path)?;
            if entries.next().is_some() {
                return Err(WorkspaceError::DestinationNotEmpty(
                    path.display().to_string(),
                ));
            }

            Ok(())
        }
        Err(error) if error.kind() == io::ErrorKind::NotFound => Ok(()),
        Err(error) => Err(WorkspaceError::Io(error)),
    }
}

fn slugify_project_name(name: &str) -> String {
    let mut slug = String::new();
    let mut last_was_separator = false;

    for ch in name.trim().chars() {
        if ch.is_ascii_alphanumeric() {
            slug.push(ch.to_ascii_lowercase());
            last_was_separator = false;
            continue;
        }

        if !last_was_separator {
            slug.push('-');
            last_was_separator = true;
        }
    }

    let slug = slug.trim_matches('-');
    if slug.is_empty() {
        "workspace".to_string()
    } else {
        slug.to_string()
    }
}

#[derive(Serialize)]
#[serde(rename_all = "snake_case")]
struct WorkspaceManifest<'a> {
    name: &'a str,
    workspace_version: u32,
    created_with: &'a str,
    created_at: String,
    template_id: &'a str,
}

impl From<toml::ser::Error> for WorkspaceError {
    fn from(error: toml::ser::Error) -> Self {
        WorkspaceError::Io(io::Error::other(error))
    }
}

#[cfg(test)]
mod tests {
    use std::fs;

    use tempfile::tempdir;

    use super::{create_workspace, list_workspace_templates};
    use crate::CreateWorkspaceRequest;

    #[test]
    fn lists_builtin_templates_with_blank_enabled() {
        let templates = list_workspace_templates();

        assert_eq!(templates.len(), 4);
        assert_eq!(templates[0].id, "blank");
        assert!(templates[0].supports_creation);
        assert!(!templates[1].supports_creation);
    }

    #[test]
    fn creates_blank_workspace_scaffold() {
        let temp = tempdir().expect("tempdir");
        let root_path = temp.path().join("ashen-coast");

        let result = create_workspace(CreateWorkspaceRequest {
            name: "The Ashen Coast".to_string(),
            path: root_path.display().to_string(),
            template_id: "blank".to_string(),
            app_version: "0.1.0".to_string(),
        })
        .expect("workspace created");

        assert!(root_path.join(".lore").is_dir());
        assert!(root_path.join(".lore/settings.toml").is_file());
        assert!(root_path.join(".lore/state.json").is_file());
        assert!(root_path.join("the-ashen-coast.lore").is_file());
        assert_eq!(result.template_id, "blank");
        assert_eq!(result.workspace_version.major, 1);
    }

    #[test]
    fn rejects_existing_non_empty_directory() {
        let temp = tempdir().expect("tempdir");
        let root_path = temp.path().join("occupied");
        fs::create_dir_all(&root_path).expect("create root");
        fs::write(root_path.join("README.md"), "occupied").expect("seed file");

        let error = create_workspace(CreateWorkspaceRequest {
            name: "Occupied".to_string(),
            path: root_path.display().to_string(),
            template_id: "blank".to_string(),
            app_version: "0.1.0".to_string(),
        })
        .expect_err("must fail");

        assert!(
            error
                .to_string()
                .contains("already exists and is not empty")
        );
    }

    #[test]
    fn rejects_unknown_template() {
        let temp = tempdir().expect("tempdir");
        let root_path = temp.path().join("unknown");

        let error = create_workspace(CreateWorkspaceRequest {
            name: "Unknown".to_string(),
            path: root_path.display().to_string(),
            template_id: "mystery".to_string(),
            app_version: "0.1.0".to_string(),
        })
        .expect_err("must fail");

        assert!(error.to_string().contains("does not exist"));
    }
}
