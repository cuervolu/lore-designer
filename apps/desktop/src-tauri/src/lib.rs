mod core;
mod system_info;
use core::config::{commands, preferences};
use tauri_plugin_tracing::{Builder, Rotation, RotationStrategy};
use tracing::{error, info};

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_window_state::Builder::new().build())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .plugin(
            Builder::new()
                .with_max_level(tauri_plugin_tracing::LevelFilter::DEBUG)
                .with_colors()
                .with_file_logging()
                .with_rotation(Rotation::Daily)
                .with_rotation_strategy(RotationStrategy::KeepSome(7))
                .with_default_subscriber()
                .build(),
        )
        .setup(move |app| {
            preferences::init_preferences(app.handle())?;

            let handle = app.handle().clone();
            tauri::async_runtime::spawn(async move {
                tokio::time::sleep(tokio::time::Duration::from_millis(500)).await;

                info!("==================== Starting Lore Designer ====================");

                if let Err(e) = system_info::log_system_info(&handle) {
                    error!("Failed to log system info: {}", e);
                }
            });

            Ok(())
        })
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_clipboard_manager::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_notification::init())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_store::Builder::new().build())
        .invoke_handler(tauri::generate_handler![
            commands::get_preferences,
            commands::save_preferences,
            commands::set_theme,
            commands::set_language,
            commands::update_last_project,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
