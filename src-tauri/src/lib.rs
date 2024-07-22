
use crate::utils::{get_fonts, save_image};

mod utils;
mod db;
mod models;
mod error;

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let mut builder = tauri::Builder::default();

    #[cfg(debug_assertions)]
    {
        let devtools = tauri_plugin_devtools::init();
        builder = builder.plugin(devtools);
    }

    #[cfg(not(debug_assertions))]
    {
        use tauri_plugin_log::{Builder, Target, TargetKind};
        use tauri_plugin_log::fern::colors::ColoredLevelConfig;
        use log::LevelFilter;

        let log_plugin = Builder::default()
            .targets([
                Target::new(TargetKind::Stdout),
                Target::new(TargetKind::LogDir { file_name: None }),
                Target::new(TargetKind::Webview),
            ])
            .with_colors(ColoredLevelConfig::default())
            .level(LevelFilter::Info)
            .level_for("tauri", LevelFilter::Warn)
            .level_for("wry", LevelFilter::Warn)
            .level_for("tracing", LevelFilter::Warn)
            .build();

        builder = builder.plugin(log_plugin);
    }

    builder
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_store::Builder::new().build())
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_os::init())
        .plugin(db::init_sql_plugin().build())
        .plugin(tauri_plugin_window_state::Builder::default().build())
        .plugin(tauri_plugin_clipboard_manager::init())
        .plugin(tauri_plugin_dialog::init())
        .invoke_handler(tauri::generate_handler![get_fonts,save_image])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
