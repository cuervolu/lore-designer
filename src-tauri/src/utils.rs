use std::collections::{HashMap, HashSet};
use crate::error::AppError;
use font_kit::{error::SelectionError, source::SystemSource};
use log::{error, info, warn};
use std::fs;
use tauri_plugin_shell::ShellExt;
use tauri::Manager;
use tauri_plugin_dialog::DialogExt;
use uuid::Uuid;
use sqlx::sqlite::SqliteQueryResult;
use sqlx::SqlitePool;
use chrono::NaiveDateTime;

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

#[tauri::command]
pub async fn clean_cache(app_handle: tauri::AppHandle) -> Result<String, AppError> {
    info!("Cleaning up unused files and database entries");
    let images_path = app_handle.path().app_data_dir()
        .map_err(|_| AppError::AppDataDirError)?
        .join("images");

    let db = get_db(&app_handle).await?;

    info!("Images path: {:?}", images_path);

    // Get all images from the database, including their timestamps and character associations
    let db_images: Vec<(String, String, String, Option<i64>)> = sqlx::query_as(
        "SELECT i.UUID, i.Path, i.Timestamp, i.CharacterID 
         FROM Images i"
    )
        .fetch_all(&db)
        .await?;

    info!("Found {} images in the database", db_images.len());

    // Get all image files in the directory
    let files = fs::read_dir(&images_path)?;
    let file_paths: Vec<_> = files
        .filter_map(Result::ok)
        .filter(|entry| entry.path().extension().and_then(|ext| ext.to_str()) == Some("png"))
        .map(|entry| entry.path())
        .collect();

    info!("Found {} image files in the directory", file_paths.len());

    let mut deleted_files = 0;
    let mut deleted_db_entries = 0;

    // Create a HashMap to store the most recent image for each character
    let mut most_recent_images: HashMap<i64, (String, NaiveDateTime)> = HashMap::new();

    // Identify the most recent image for each character
    for (uuid, _, timestamp_str, character_id) in &db_images {
        if let Some(char_id) = character_id {
            if let Ok(timestamp) = NaiveDateTime::parse_from_str(timestamp_str, "%Y-%m-%d %H:%M:%S") {
                most_recent_images
                    .entry(*char_id)
                    .and_modify(|(existing_uuid, existing_timestamp)| {
                        if timestamp > *existing_timestamp {
                            *existing_uuid = uuid.clone();
                            *existing_timestamp = timestamp;
                        }
                    })
                    .or_insert((uuid.clone(), timestamp));
            }
        }
    }

    // Create a set of UUIDs to keep (most recent for each character + images not associated with characters)
    let mut uuids_to_keep: HashSet<String> = most_recent_images.values().map(|(uuid, _)| uuid.clone()).collect();
    uuids_to_keep.extend(db_images.iter().filter_map(|(uuid, _, _, character_id)| {
        if character_id.is_none() { Some(uuid.clone()) } else { None }
    }));

    // Delete files that are not in the set of UUIDs to keep
    for file_path in &file_paths {
        let file_name = file_path.file_name().unwrap().to_str().unwrap();
        let file_name_without_ext = file_name.trim_end_matches(".png");

        if !uuids_to_keep.contains(file_name_without_ext) {
            if let Err(e) = fs::remove_file(file_path) {
                error!("Failed to delete file {:?}: {}", file_path, e);
            } else {
                info!("Deleted unused file: {:?}", file_path);
                deleted_files += 1;
            }
        }
    }

    // Delete database entries for files that don't exist or are not in the set of UUIDs to keep
    for (uuid, _, _, _) in &db_images {
        let file_name = format!("{}.png", uuid);
        let full_path = images_path.join(&file_name);

        if !full_path.exists() || !uuids_to_keep.contains(uuid) {
            let result: SqliteQueryResult = sqlx::query("DELETE FROM Images WHERE UUID = ?")
                .bind(uuid)
                .execute(&db)
                .await?;
            info!("Deleted database entry: {}", uuid);
            deleted_db_entries += result.rows_affected();
        }
    }

    info!("Cleaned up {} unused files and {} unused database entries", deleted_files, deleted_db_entries);
    Ok(format!("Cleaned up {} unused files and {} unused database entries", deleted_files, deleted_db_entries))
}

/// Retrieves the SQLite database connection pool for the application.
///
/// This function constructs the path to the application's configuration directory and appends
/// the database filename (`loredesigner.db`). It then attempts to establish a connection to the
/// SQLite database at the constructed path. If successful, it returns the connection pool.
///
/// # Arguments
///
/// * `app_handle` - A reference to the `tauri::AppHandle` providing access to the application's environment and utilities.
///
/// # Returns
///
/// A `Result` which is:
/// - `Ok(SqlitePool)` containing the connection pool to the SQLite database if the operation succeeds.
/// - `Err(AppError)` describing the error if the operation fails.
///
/// # Errors
///
/// This function can return an `AppError` in the following cases:
/// - `AppDataDirError` if the application configuration directory cannot be determined.
/// - `DatabaseError` if there is an error connecting to the SQLite database.
async fn get_db(app_handle: &tauri::AppHandle) -> Result<SqlitePool, AppError> {
    let path = app_handle.path().app_config_dir()
        .map_err(|_| AppError::AppDataDirError)?;
    let db_path = path.join("loredesigner.db");
    info!("Opening database at {:?}", db_path);

    SqlitePool::connect(db_path.to_str().unwrap())
        .await
        .map_err(AppError::DatabaseError)
}

/// Opens the default web browser with the specified URL.
///
/// This function uses the `tauri_plugin_shell` to open the default web browser
/// and navigate to the provided URL. It requires a valid `tauri::AppHandle` to
/// access the shell extension.
///
/// # Arguments
///
/// * `app_handle` - A `tauri::AppHandle` providing access to the application's environment and utilities.
/// * `url` - A `String` containing the URL to be opened in the default web browser.
///
/// # Returns
///
/// A `Result` which is:
/// - `Ok(())` if the browser was successfully opened.
/// - `Err(AppError)` if there was an error opening the browser.
///
/// # Errors
///
/// This function can return an `AppError` if there is an issue with the shell extension or if the URL is invalid.
#[tauri::command]
pub fn open_browser(app_handle: tauri::AppHandle, url: String) -> Result<(), AppError> {
    let shell = app_handle.shell();
    shell.open(url, None)?;

    Ok(())
}