mod commands;
mod editor;
mod index;
mod model;
mod state;

pub use commands::*;
pub use editor::*;
pub use index::*;
pub use model::*;
pub use state::*;

use anyhow::Context;
use log::info;
use std::path::Path;

/// Main structure for editor functionality
pub struct EditorManager;

impl EditorManager {
    /// Open a workspace in the editor
    pub fn open_workspace(workspace_path: impl AsRef<Path>) -> Result<WorkspaceInfo, EditorError> {
        let path = workspace_path.as_ref();

        if !path.exists() {
            return Err(EditorError::WorkspaceNotFound(path.display().to_string()));
        }

        if !path.is_dir() {
            return Err(EditorError::InvalidPath(path.display().to_string()));
        }

        // Get basic workspace info
        let workspace_info = WorkspaceInfo::from_path(path)?;

        // Start indexing process (in background)
        IndexManager::start_indexing(path)?;

        // Load editor state for this workspace
        EditorState::load_or_create(path)?;

        info!("Opened workspace: {}", path.display());
        Ok(workspace_info)
    }

    /// Get file content from workspace
    pub fn get_file_content(
        workspace_path: impl AsRef<Path>,
        file_path: impl AsRef<Path>,
    ) -> Result<FileContent, EditorError> {
        let workspace = workspace_path.as_ref();
        let file = file_path.as_ref();

        // Validate paths
        if !workspace.exists() || !workspace.is_dir() {
            return Err(EditorError::WorkspaceNotFound(
                workspace.display().to_string(),
            ));
        }

        let full_path = workspace.join(file);
        if !full_path.exists() || !full_path.is_file() {
            return Err(EditorError::FileNotFound(full_path.display().to_string()));
        }

        // Determine file type and load content accordingly
        let file_type = FileType::from_path(&full_path)?;
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
            FileType::Character => {
                // Read character file (JSON format)
                let json = std::fs::read_to_string(&full_path).context(format!(
                    "Failed to read character file: {}",
                    full_path.display()
                ))?;

                FileContent::Character { data: json }
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
        };

        Ok(content)
    }

    /// Save file content to workspace
    pub fn save_file_content(
        workspace_path: impl AsRef<Path>,
        file_path: impl AsRef<Path>,
        content: SaveFileRequest,
    ) -> Result<(), EditorError> {
        let workspace = workspace_path.as_ref();
        let file = file_path.as_ref();

        // Validate paths
        if !workspace.exists() || !workspace.is_dir() {
            return Err(EditorError::WorkspaceNotFound(
                workspace.display().to_string(),
            ));
        }

        let full_path = workspace.join(file);

        // Ensure parent directories exist
        if let Some(parent) = full_path.parent() {
            std::fs::create_dir_all(parent).context(format!(
                "Failed to create parent directories for: {}",
                full_path.display()
            ))?;
        }

        // Write content based on the request type
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
        }

        info!("Saved file: {}", full_path.display());
        Ok(())
    }
}
