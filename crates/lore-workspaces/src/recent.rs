use serde::{Deserialize, Serialize};
use std::path::Path;
use tauri::AppHandle;
use tauri_plugin_store::StoreExt;

const RECENT_WORKSPACES_KEY: &str = "recent_workspaces";
const RECENT_WORKSPACES_FILE: &str = "recent_workspaces.dat";
const MAX_RECENT_WORKSPACES: usize = 10;

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct RecentWorkspace {
    /// Name of the workspace
    pub name: String,

    /// Full path to the workspace directory
    pub path: String,

    /// Last accessed timestamp
    pub last_accessed: u64,
}

/// Get list of recent workspaces
pub fn get_recent_workspaces(app: &AppHandle) -> Result<Vec<RecentWorkspace>, String> {
    let store = match app.get_store(RECENT_WORKSPACES_FILE) {
        Some(store) => store,
        None => app
            .store(RECENT_WORKSPACES_FILE)
            .map_err(|e| e.to_string())?,
    };

    if !store.has(RECENT_WORKSPACES_KEY) {
        store.set(
            RECENT_WORKSPACES_KEY,
            serde_json::json!(Vec::<RecentWorkspace>::new()),
        );
        store.save().map_err(|e| e.to_string())?;
        return Ok(Vec::new());
    }

    match store.get(RECENT_WORKSPACES_KEY) {
        Some(workspaces_value) => {
            let workspaces: Vec<RecentWorkspace> =
                serde_json::from_value(workspaces_value.clone()).map_err(|e| e.to_string())?;
            Ok(workspaces)
        }
        None => Ok(Vec::new()),
    }
}

pub fn add_recent_workspace(app: &AppHandle, name: String, path: String) -> Result<(), String> {
    let mut workspaces = get_recent_workspaces(app)?;

    let store = match app.get_store(RECENT_WORKSPACES_FILE) {
        Some(store) => store,
        None => app
            .store(RECENT_WORKSPACES_FILE)
            .map_err(|e| e.to_string())?,
    };

    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs();

    // Remove if it exists (we'll add it back at the top)
    workspaces.retain(|w| w.path != path);

    workspaces.insert(
        0,
        RecentWorkspace {
            name,
            path,
            last_accessed: now,
        },
    );

    if workspaces.len() > MAX_RECENT_WORKSPACES {
        workspaces.truncate(MAX_RECENT_WORKSPACES);
    }

    store.set(RECENT_WORKSPACES_KEY, serde_json::json!(workspaces));
    store.save().map_err(|e| e.to_string())?;

    Ok(())
}

pub fn remove_recent_workspace(app: &AppHandle, path: String) -> Result<(), String> {
    let mut workspaces = get_recent_workspaces(app)?;

    // Get the store (should exist at this point, but just in case)
    let store = app
        .get_store(RECENT_WORKSPACES_FILE)
        .ok_or_else(|| "Failed to get store".to_string())?;

    workspaces.retain(|w| w.path != path);

    store.set(RECENT_WORKSPACES_KEY, serde_json::json!(workspaces));
    store.save().map_err(|e| e.to_string())?;

    Ok(())
}

pub fn check_workspace_exists(path: &str) -> bool {
    Path::new(path).exists()
}
