mod commands;
mod editor;
mod frontmatter;
mod index;
mod model;
mod state;
mod templates;
mod watcher;

pub use commands::*;
pub use editor::*;
pub use index::*;
pub use model::*;
pub use state::*;
pub use watcher::*;

use crate::frontmatter::combine_frontmatter_and_content;
use anyhow::Context;
use gray_matter::{Matter, engine::YAML};
use tracing::{debug, info};
use std::path::Path;

/// Main structure for editor functionality
pub struct EditorManager;

impl EditorManager {
    pub fn open_workspace(
        app: &tauri::AppHandle,
        workspace_path: impl AsRef<Path>,
    ) -> Result<WorkspaceInfo, EditorError> {
        let path = workspace_path.as_ref();

        if !path.exists() {
            return Err(EditorError::WorkspaceNotFound(path.display().to_string()));
        }

        if !path.is_dir() {
            return Err(EditorError::InvalidPath(path.display().to_string()));
        }

        let workspace_info = WorkspaceInfo::from_path(path)?;

        // Start indexing process (in background)
        IndexManager::start_indexing(path)?;

        // Start watching the workspace for changes
        FileSystemWatcher::new(path, Some(app.clone()))?;

        // Load editor state for this workspace
        EditorState::load_or_create(path)?;

        info!("Opened workspace: {}", path.display());
        Ok(workspace_info)
    }

    pub fn get_file_content(
        workspace_path: impl AsRef<Path>,
        file_path: impl AsRef<Path>,
    ) -> Result<FileContent, EditorError> {
        let workspace = workspace_path.as_ref();
        let file = file_path.as_ref();

        if !workspace.exists() || !workspace.is_dir() {
            return Err(EditorError::WorkspaceNotFound(
                workspace.display().to_string(),
            ));
        }

        let full_path = workspace.join(file);
        if !full_path.exists() || !full_path.is_file() {
            return Err(EditorError::FileNotFound(full_path.display().to_string()));
        }

        let file_type = FileType::from_path(&full_path)?;

        let matter = Matter::<YAML>::new();

        let content = match file_type {
            FileType::Markdown => {
                // Read markdown content
                let text = std::fs::read_to_string(&full_path).context(format!(
                    "Failed to read markdown file: {}",
                    full_path.display()
                ))?;

                FileContent::Markdown { content: text }
            }
            FileType::Canvas => {
                // Read canvas content (JSON format)
                let json = std::fs::read_to_string(&full_path).context(format!(
                    "Failed to read canvas file: {}",
                    full_path.display()
                ))?;

                FileContent::Canvas { data: json }
            }
            FileType::Character | FileType::Location | FileType::Lore => {
                let text = std::fs::read_to_string(&full_path).context(format!(
                    "Failed to read file with frontmatter: {}",
                    full_path.display()
                ))?;

                let result = matter.parse(&text);

                // Extract the raw frontmatter string and content
                // We check result.data.is_some() to see if frontmatter was actually parsed.
                // If not, we treat the whole file as content.
                // `result.matter` contains the raw frontmatter string.
                // `result.content` contains the content after frontmatter.
                let (frontmatter_opt, content_str) = if result.data.is_some() {
                    (Some(result.matter.to_string()), result.content.to_string())
                } else {
                    (None, result.orig.to_string()) // Use original string if no frontmatter found
                };

                debug!(
                    "Parsed File Type: {:?}, Frontmatter Found: {}, Frontmatter Content: {:?}",
                    file_type,
                    frontmatter_opt.is_some(),
                    frontmatter_opt
                );

                match file_type {
                    FileType::Character => FileContent::Character {
                        frontmatter: frontmatter_opt,
                        content: content_str,
                    },
                    FileType::Location => FileContent::Location {
                        frontmatter: frontmatter_opt,
                        content: content_str,
                    },
                    FileType::Lore => FileContent::Lore {
                        frontmatter: frontmatter_opt,
                        content: content_str,
                    },
                    _ => unreachable!(), // Should not happen due to outer match
                }
            }
            FileType::Image => {
                // For images, we return the path for frontend to handle
                FileContent::Image {
                    path: full_path.display().to_string(),
                }
            }
            FileType::Unknown => {
                // Read as plain text for unknown formats
                let text = std::fs::read_to_string(&full_path)
                    .context(format!("Failed to read file: {}", full_path.display()))?;

                FileContent::PlainText { content: text }
            }
            FileType::Dialogue => {
                let text = std::fs::read_to_string(&full_path).context(format!(
                    "Failed to read dialogue file: {}",
                    full_path.display()
                ))?;

                FileContent::Dialogue { data: text }
            }
        };

        Ok(content)
    }

    pub fn save_file_content(
        workspace_path: impl AsRef<Path>,
        file_path: impl AsRef<Path>,
        content: SaveFileRequest,
    ) -> Result<(), EditorError> {
        let workspace = workspace_path.as_ref();
        let file = file_path.as_ref();

        if !workspace.exists() || !workspace.is_dir() {
            return Err(EditorError::WorkspaceNotFound(
                workspace.display().to_string(),
            ));
        }

        let full_path = workspace.join(file);

        if let Some(parent) = full_path.parent() {
            std::fs::create_dir_all(parent).context(format!(
                "Failed to create parent directories for: {}",
                full_path.display()
            ))?;
        }

        match content {
            SaveFileRequest::Text { content } => {
                std::fs::write(&full_path, content)
                    .context(format!("Failed to write to file: {}", full_path.display()))?;
            }
            SaveFileRequest::Json { content } => {
                std::fs::write(&full_path, content).context(format!(
                    "Failed to write JSON to file: {}",
                    full_path.display()
                ))?;
            }
            SaveFileRequest::MarkdownWithFrontmatter {
                frontmatter,
                content,
            } => {
                let combined = combine_frontmatter_and_content(&frontmatter, &content);

                std::fs::write(&full_path, combined).context(format!(
                    "Failed to write file with frontmatter: {}",
                    full_path.display()
                ))?;
            }
        }

        debug!("Saved file: {}", full_path.display());
        Ok(())
    }
    pub fn get_file_frontmatter(
        workspace_path: impl AsRef<Path>,
        file_path: impl AsRef<Path>,
    ) -> Result<FrontmatterResult, EditorError> {
        let workspace = workspace_path.as_ref();
        let file = file_path.as_ref();

        if !workspace.exists() || !workspace.is_dir() {
            return Err(EditorError::WorkspaceNotFound(
                workspace.display().to_string(),
            ));
        }

        let full_path = workspace.join(file);
        if !full_path.exists() || !full_path.is_file() {
            return Err(EditorError::FileNotFound(full_path.display().to_string()));
        }

        let file_type = FileType::from_path(&full_path)?;

        match file_type {
            FileType::Character | FileType::Location | FileType::Lore => {
                let content = std::fs::read_to_string(&full_path)
                    .context(format!("Failed to read file: {}", full_path.display()))?;

                let matter = Matter::<YAML>::new();
                let parsed = matter.parse(&content);

                let frontmatter_yaml = if parsed.data.is_some() {
                    Some(parsed.orig.clone())
                } else {
                    None
                };

                Ok(FrontmatterResult {
                    path: file.display().to_string(),
                    frontmatter: frontmatter_yaml,
                    has_frontmatter: parsed.data.is_some(),
                })
            }
            _ => {
                // File types that don't support frontmatter
                Ok(FrontmatterResult {
                    path: file.display().to_string(),
                    frontmatter: None,
                    has_frontmatter: false,
                })
            }
        }
    }
}
