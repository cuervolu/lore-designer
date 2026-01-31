use super::EditorError;
use serde::{Deserialize, Serialize};
use std::path::Path;

/// State of the editor for a specific workspace
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct EditorState {
    /// Workspace path
    pub workspace_path: String,

    /// List of open tabs
    pub open_tabs: Vec<TabInfo>,

    /// Currently active tab
    pub active_tab: Option<String>,

    /// State of UI panels
    pub panels: PanelState,

    /// Last opened/edited files
    pub recently_opened: Vec<String>,
}

/// Information about an open tab
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct TabInfo {
    /// Unique ID for the tab
    pub id: String,

    /// File path (relative to workspace)
    pub path: String,

    /// Tab title
    pub title: String,

    /// Icon for the tab
    pub icon: Option<String>,

    /// Whether the tab has unsaved changes
    pub has_unsaved_changes: bool,
}

/// State of UI panels
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct PanelState {
    /// Whether the console is visible
    pub console_visible: bool,

    /// Whether the inspector is visible
    pub inspector_visible: bool,

    /// Width of the file explorer
    pub explorer_width: u32,

    /// Width of the inspector
    pub inspector_width: u32,

    /// Height of the console
    pub console_height: u32,
}

impl Default for PanelState {
    fn default() -> Self {
        Self {
            console_visible: false,
            inspector_visible: true,
            explorer_width: 250,
            inspector_width: 300,
            console_height: 200,
        }
    }
}

impl EditorState {
    /// Load editor state for a workspace
    pub fn load_or_create(workspace_path: &Path) -> Result<Self, EditorError> {
        let state_file = workspace_path.join(".lore").join("editor_state.json");

        if state_file.exists() {
            let state_json = std::fs::read_to_string(&state_file)?;
            let state: EditorState = serde_json::from_str(&state_json)?;
            return Ok(state);
        }

        // Create default state
        let default_state = EditorState {
            workspace_path: workspace_path.display().to_string(),
            open_tabs: Vec::new(),
            active_tab: None,
            panels: PanelState::default(),
            recently_opened: Vec::new(),
        };

        // Save the default state
        default_state.save()?;

        Ok(default_state)
    }

    /// Save editor state to disk
    pub fn save(&self) -> Result<(), EditorError> {
        let workspace_path = Path::new(&self.workspace_path);
        let lore_dir = workspace_path.join(".lore");

        // Ensure directory exists
        std::fs::create_dir_all(&lore_dir)?;

        let state_file = lore_dir.join("editor_state.json");
        let state_json = serde_json::to_string_pretty(&self)?;

        std::fs::write(state_file, state_json)?;

        Ok(())
    }

    /// Add a recently opened file
    pub fn add_recently_opened(&mut self, file_path: &str) {
        // Remove if already exists
        self.recently_opened.retain(|p| p != file_path);

        // Add to front
        self.recently_opened.insert(0, file_path.to_string());

        // Limit to 10 entries
        if self.recently_opened.len() > 10 {
            self.recently_opened.truncate(10);
        }
    }

    /// Add or update a tab
    pub fn add_or_update_tab(&mut self, tab: TabInfo) {
        // Check if tab already exists
        if let Some(existing) = self.open_tabs.iter_mut().find(|t| t.id == tab.id) {
            *existing = tab;
        } else {
            self.open_tabs.push(tab);
        }
    }

    /// Close a tab
    pub fn close_tab(&mut self, tab_id: &str) {
        self.open_tabs.retain(|t| t.id != tab_id);

        // Update active tab if needed
        if self.active_tab.as_deref() == Some(tab_id) {
            self.active_tab = self.open_tabs.first().map(|t| t.id.clone());
        }
    }

    /// Set active tab
    pub fn set_active_tab(&mut self, tab_id: &str) {
        // Only set if tab exists
        if self.open_tabs.iter().any(|t| t.id == tab_id) {
            self.active_tab = Some(tab_id.to_string());
        }
    }
}
