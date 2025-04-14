use crate::core::config::preferences::{
    get_current_preferences, AppPreferences, PREFERENCES_FILE, PREFERENCES_KEY,
};
use serde_json::json;
use tauri::{AppHandle, Runtime};
use tauri_plugin_store::StoreExt;

#[tauri::command]
pub fn get_preferences<R: Runtime>(app: AppHandle<R>) -> Result<AppPreferences, String> {
    get_current_preferences(&app)
}

#[tauri::command]
pub fn save_preferences<R: Runtime>(
    app: AppHandle<R>,
    preferences: AppPreferences,
) -> Result<(), String> {
    let store = app.store(PREFERENCES_FILE).map_err(|e| e.to_string())?;
    store.set(PREFERENCES_KEY, json!(preferences));
    store.save().map_err(|e| e.to_string())?;
    Ok(())
}

#[tauri::command]
pub fn set_theme<R: Runtime>(app: AppHandle<R>, theme: String) -> Result<(), String> {
    let mut preferences = get_current_preferences(&app)?;
    preferences.theme = theme;

    let store = app.store(PREFERENCES_FILE).map_err(|e| e.to_string())?;
    store.set(PREFERENCES_KEY, json!(preferences));
    store.save().map_err(|e| e.to_string())?;
    Ok(())
}

#[tauri::command]
pub fn set_language<R: Runtime>(app: AppHandle<R>, language: String) -> Result<(), String> {
    let mut preferences = get_current_preferences(&app)?;
    preferences.language = language;

    let store = app.store(PREFERENCES_FILE).map_err(|e| e.to_string())?;
    store.set(PREFERENCES_KEY, json!(preferences));
    store.save().map_err(|e| e.to_string())?;
    Ok(())
}

#[tauri::command]
pub fn update_last_project<R: Runtime>(app: AppHandle<R>, path: String) -> Result<(), String> {
    let mut preferences = get_current_preferences(&app)?;
    preferences.last_project_path = Some(path);

    let store = app.store(PREFERENCES_FILE).map_err(|e| e.to_string())?;
    store.set(PREFERENCES_KEY, json!(preferences));
    store.save().map_err(|e| e.to_string())?;
    Ok(())
}
