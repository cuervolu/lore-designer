use font_kit::{error::SelectionError, source::SystemSource};
use log::warn;

#[tauri::command]
pub async fn get_fonts() -> Vec<String> {
    let source = SystemSource::new();

    let fonts: Result<Vec<String>, SelectionError> = source.all_families();

    if let Ok(font) = fonts {
        font
    } else {
        warn!("Could not get fonts, returning empty list");
        vec![]
    }
}
