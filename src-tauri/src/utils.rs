use crate::error::AppError;
use font_kit::{error::SelectionError, source::SystemSource};
use log::{info, warn};
use std::fs;
use tauri::Manager;
use tauri_plugin_dialog::DialogExt;
use uuid::Uuid;

#[derive(serde::Serialize)]
pub struct ImageInfo {
    id: String,
    path: String,
}

/// Retrieves a list of font family names available on the system.
///
/// This function queries the system's font source to compile a list of all available font family names.
/// If the query is successful, a vector containing the names of the font families is returned.
/// In case of failure, a warning is logged, and an empty vector is returned to indicate the absence of available fonts.
///
/// # Returns
///
/// A `Vec<String>` containing the names of all font families available on the system.
/// If no fonts are found or an error occurs during the query, an empty vector is returned.
#[tauri::command]
pub async fn get_fonts() -> Vec<String> {
    info!("Getting fonts from system source");
    let source = SystemSource::new();

    let fonts: Result<Vec<String>, SelectionError> = source.all_families();

    if let Ok(font) = fonts {
        info!("Got {:?} fonts", font.len());
        font
    } else {
        warn!("Could not get fonts, returning empty list");
        vec![]
    }
}

/// Saves an image selected by the user to the application's data directory.
///
/// This function opens a file dialog for the user to select an image file. Once a file is selected,
/// it generates a unique filename for the image to be saved under the application's data directory
/// in a subdirectory named "images". If the "images" directory does not exist, it is created.
/// The image is then copied to the designated location, and information about the saved image
/// is returned.
///
/// # Arguments
///
/// * `app` - A `tauri::AppHandle` providing access to the application's environment and utilities.
///
/// # Returns
///
/// A `Result` which is:
/// - `Ok(ImageInfo)` containing the unique ID and path of the saved image if the operation succeeds.
/// - `Err(AppError)` describing the error if the operation fails.
///
/// # Errors
///
/// This function can return an `AppError` in the following cases:
/// - `FileSelectionFailed` if no file is selected by the user.
/// - Various errors related to file operations (e.g., failure to create the "images" directory,
///   failure to copy the file).
///
#[tauri::command]
pub async fn save_image(app: tauri::AppHandle) -> Result<ImageInfo, AppError> {
    let file_path_result = app.dialog().file().blocking_pick_file();
    let file_path = file_path_result.ok_or_else(|| AppError::FileSelectionFailed)?;

    let uuid = Uuid::new_v4();
    let filename = format!("{}.png", uuid);

    let dest_path_result = app
        .path()
        .app_data_dir()
        .map(|p| p.join("images").join(&filename));

    match dest_path_result {
        Ok(dest_path) => {
            if let Some(parent) = dest_path.parent() {
                fs::create_dir_all(parent)?;
            }

            fs::copy(file_path.path, &dest_path)?;

            Ok(ImageInfo {
                id: uuid.to_string(),
                path: dest_path.to_string_lossy().to_string(),
            })
        }
        Err(e) => Err(AppError::from(e)),
    }
}
