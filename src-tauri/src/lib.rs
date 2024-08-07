use crate::commands::github::get_release_notes;
use crate::commands::help::get_system_info;
use crate::utils::{clean_cache, get_fonts, open_browser, save_image};
use tauri_plugin_log::fern::colors::ColoredLevelConfig;
mod commands;
mod db;
mod error;
mod models;
mod utils;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let builder = tauri::Builder::default()

        .setup(|app| {
            // create the log plugin as usual, but call split() instead of build()
            let (tauri_plugin_log, max_level, logger) = tauri_plugin_log::Builder::new()
                .level(log::LevelFilter::Info)
                .with_colors(ColoredLevelConfig::default())
                .level_for("tauri", log::LevelFilter::Warn)
                .level_for("wry", log::LevelFilter::Warn)
                .level_for("tracing", log::LevelFilter::Warn)
                .level_for("app_lib", log::LevelFilter::Info)
                .split(app.handle())?;

            // on debug builds, set up the DevTools plugin and pipe the logger from tauri-plugin-log
            #[cfg(debug_assertions)]
            {
                let mut devtools_builder = tauri_plugin_devtools::Builder::default();
                devtools_builder.attach_logger(logger);
                app.handle().plugin(devtools_builder.init())?;
            }
            // on release builds, only attach the logger from tauri-plugin-log
            #[cfg(not(debug_assertions))]
            {
                tauri_plugin_log::attach_logger(max_level, logger);
            }

            app.handle().plugin(tauri_plugin_log)?;

            Ok(())
        });

    builder
        .plugin(tauri_plugin_updater::Builder::new().build())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_store::Builder::new().build())
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_os::init())
        .plugin(db::init_sql_plugin().build())
        .plugin(tauri_plugin_window_state::Builder::default().build())
        .plugin(tauri_plugin_clipboard_manager::init())
        .plugin(tauri_plugin_dialog::init())
        .invoke_handler(tauri::generate_handler![
            get_fonts,
            save_image,
            clean_cache,
            open_browser,
            get_release_notes,
            get_system_info
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
