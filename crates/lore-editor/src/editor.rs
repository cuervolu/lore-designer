use super::{EditorError, EditorState, TabInfo};
use std::path::{Path, PathBuf};

/// Editor actions
pub struct Editor;

impl Editor {
    /// Get welcome text for a new workspace
    pub fn get_welcome_text(workspace_name: &str) -> String {
        format!(
            "# Welcome to {}\n\nThis is your new Lore Designer workspace.\n\n## Getting Started\n\n- Create characters, locations, and lore in their respective folders\n- Use the file explorer to navigate your content\n- Everything is automatically saved in your workspace\n\nLet your creativity flow!",
            workspace_name
        )
    }

    /// Open a file in the editor
    pub fn open_file(
        workspace_path: &Path,
        file_path: &Path,
        editor_state: &mut EditorState,
    ) -> Result<TabInfo, EditorError> {
        let rel_path = file_path
            .strip_prefix(workspace_path)
            .map_err(|_| EditorError::InvalidPath(file_path.display().to_string()))?;

        let rel_path_str = rel_path.display().to_string();

        // Generate a unique ID
        let tab_id = format!("tab-{}", uuid::Uuid::new_v4());

        let file_name = file_path
            .file_name()
            .and_then(|n| n.to_str())
            .unwrap_or("unnamed")
            .to_string();

        // Determine icon based on file type or directory
        let icon = determine_file_icon(file_path);

        // Create tab info
        let tab = TabInfo {
            id: tab_id,
            path: rel_path_str.clone(),
            title: file_name,
            icon,
            has_unsaved_changes: false,
        };

        // Add to editor state
        editor_state.add_or_update_tab(tab.clone());
        editor_state.set_active_tab(&tab.id);
        editor_state.add_recently_opened(&rel_path_str);

        // Save state
        editor_state.save()?;

        Ok(tab)
    }

    /// Create a new file in the workspace
    pub fn create_file(
        workspace_path: &Path,
        parent_dir: &Path,
        file_name: &str,
        content: &str,
    ) -> Result<PathBuf, EditorError> {
        let parent_path = workspace_path.join(parent_dir);

        // Ensure parent directory exists
        std::fs::create_dir_all(&parent_path)?;

        let file_path = parent_path.join(file_name);

        // Write content to file
        std::fs::write(&file_path, content)?;

        Ok(file_path)
    }
}

/// Determine icon for a file
fn determine_file_icon(file_path: &Path) -> Option<String> {
    // Check extension
    if let Some(ext) = file_path.extension().and_then(|e| e.to_str()) {
        match ext.to_lowercase().as_str() {
            "md" => return Some("markdown".to_string()),
            "canvas" => return Some("canvas".to_string()),
            "character" => return Some("character".to_string()),
            "png" | "jpg" | "jpeg" | "webp" | "svg" => return Some("image".to_string()),
            _ => {}
        }
    }

    // Check filename patterns
    if let Some(file_name) = file_path.file_name().and_then(|n| n.to_str()) {
        if file_name.ends_with(".character.json") {
            return Some("character".to_string());
        } else if file_name.ends_with(".canvas.json") {
            return Some("canvas".to_string());
        }
    }

    // Check parent directory
    if let Some(parent) = file_path
        .parent()
        .and_then(|p| p.file_name())
        .and_then(|n| n.to_str())
    {
        match parent.to_lowercase().as_str() {
            "characters" => return Some("character".to_string()),
            "locations" => return Some("location".to_string()),
            "lore" => return Some("book".to_string()),
            "story" | "stories" => return Some("fileText".to_string()),
            "notes" => return Some("note".to_string()),
            _ => {}
        }
    }

    None
}
