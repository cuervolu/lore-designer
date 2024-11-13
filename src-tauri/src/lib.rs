#[cfg(not(any(target_os = "android", target_os = "ios")))]
use tauri_plugin_log::fern::colors::ColoredLevelConfig;
#[cfg(not(any(target_os = "android", target_os = "ios")))]
use tauri_plugin_log::RotationStrategy;


fn setup_logger(app: &mut tauri::App) -> Result<(), Box<dyn std::error::Error>> {
    #[cfg(not(any(target_os = "android", target_os = "ios")))]
    {
        let base_log_level =
            if cfg!(debug_assertions) { log::LevelFilter::Debug } else { log::LevelFilter::Info };

        let builder = tauri_plugin_log::Builder::new()
            .max_file_size(1024 * 1024 * 10)
            .rotation_strategy(RotationStrategy::KeepAll)
            .timezone_strategy(tauri_plugin_log::TimezoneStrategy::UseLocal)
            .with_colors(ColoredLevelConfig::default())
            .level(base_log_level)
            .level_for("tauri", log::LevelFilter::Warn)
            .level_for("wry", log::LevelFilter::Warn)
            .level_for("hyper", log::LevelFilter::Warn)
            .level_for("hyper::proto", log::LevelFilter::Warn)
            .level_for("hyper_util::client::legacy", log::LevelFilter::Warn)
            .level_for("devtools_core", log::LevelFilter::Warn)
            .level_for("tracing", log::LevelFilter::Warn)
            .level_for(
                "lore_designer_lib",
                if cfg!(debug_assertions) { log::LevelFilter::Trace } else { log::LevelFilter::Info },
            );

        #[cfg(debug_assertions)]
        {
            let (plugin, _max_level, logger) = builder.split(app.handle())?;
            let mut devtools_builder = tauri_plugin_devtools::Builder::default();
            devtools_builder.attach_logger(logger);
            app.handle().plugin(devtools_builder.init())?;
            app.handle().plugin(plugin)?;
        }

        #[cfg(not(debug_assertions))]
        {
            app.handle().plugin(builder.build())?;
        }
    }

    Ok(())
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let mut builder = tauri::Builder::default()
        .setup(|app| {
            setup_logger(app)?;
            Ok(())
        });

    builder = builder
        .plugin(tauri_plugin_store::Builder::new().build())
        .plugin(tauri_plugin_sql::Builder::new().build())
        .plugin(tauri_plugin_shell::init())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_clipboard_manager::init());

    #[cfg(not(any(target_os = "android", target_os = "ios")))]
    {
        builder = builder
            .plugin(tauri_plugin_window_state::Builder::new().build())
            .plugin(tauri_plugin_updater::Builder::new().build());
    }

    builder
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}