use serde::{Deserialize, Serialize};
use serde_json::json;
use tauri::{AppHandle, Runtime};
use tauri_plugin_store::StoreExt;

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct AppPreferences {
    // Appearance
    pub theme: String,    // "system", "dark", "light"
    pub language: String, // "en", "es", etc.
    // Last project
    pub last_project_path: Option<String>,
    pub last_file_path: Option<String>,
    // User interface
    pub sidebar_width: Option<u32>,
    pub sidebar_expanded: bool,
    pub font_size: u32,
    // Application behavior
    pub auto_save: bool,
    pub auto_save_interval_seconds: u32,
    // Lore Designer specific settings
    pub default_character_template: Option<String>,
    pub default_location_template: Option<String>,
    // Space for additional configurations
    pub custom_settings: serde_json::Value,
}

impl Default for AppPreferences {
    fn default() -> Self {
        Self {
            theme: "system".to_string(),
            language: "en".to_string(),
            last_project_path: None,
            last_file_path: None,
            sidebar_width: Some(250),
            sidebar_expanded: true,
            font_size: 14,
            auto_save: true,
            auto_save_interval_seconds: 60,
            default_character_template: None,
            default_location_template: None,
            custom_settings: json!({}),
        }
    }
}

pub const PREFERENCES_KEY: &str = "preferences";
pub const PREFERENCES_FILE: &str = "preferences.dat";

pub fn init_preferences<R: Runtime>(app: &AppHandle<R>) -> Result<(), String> {
    let store = app.store(PREFERENCES_FILE).map_err(|e| e.to_string())?;

    // Initialize with defaults if no preferences exist
    if !store.has(PREFERENCES_KEY) {
        let defaults = AppPreferences::default();
        store.set(PREFERENCES_KEY, json!(defaults));
        store.save().map_err(|e| e.to_string())?;
    }

    Ok(())
}

pub fn get_current_preferences<R: Runtime>(app: &AppHandle<R>) -> Result<AppPreferences, String> {
    let store = app.store(PREFERENCES_FILE).map_err(|e| e.to_string())?;

    match store.get(PREFERENCES_KEY) {
        Some(prefs_value) => {
            let preferences: AppPreferences = serde_json::from_value(prefs_value.clone())
                .map_err(|e| e.to_string())?;
            Ok(preferences)
        }
        None => Ok(AppPreferences::default())
    }
}