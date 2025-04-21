mod core;
mod system_info;
mod workspace;

use core::config::{commands, preferences};
use log::error;
use tauri::Manager;
use tauri_plugin_log::{fern::colors::ColoredLevelConfig, RotationStrategy};
#[cfg(not(target_os = "linux"))]
use tauri_plugin_decorum::WebviewWindowExt;

fn setup_logger(app: &mut tauri::App) -> Result<(), Box<dyn std::error::Error>> {
    // Set base log level based on environment
    let base_log_level = if cfg!(debug_assertions) {
        log::LevelFilter::Debug
    } else {
        log::LevelFilter::Info
    };

    // Create builder with environment-specific configuration
    let builder = tauri_plugin_log::Builder::new()
        .max_file_size(1024 * 1024 * 10) // 10 MB
        .rotation_strategy(RotationStrategy::KeepAll)
        .timezone_strategy(tauri_plugin_log::TimezoneStrategy::UseLocal)
        .with_colors(ColoredLevelConfig::default())
        // Set default level based on environment
        .level(base_log_level)
        // Configure specific module levels
        .level_for("tauri", log::LevelFilter::Warn)
        .level_for("wry", log::LevelFilter::Warn)
        .level_for("hyper", log::LevelFilter::Warn)
        .level_for("hyper::proto", log::LevelFilter::Warn)
        .level_for("hyper_util::client::legacy", log::LevelFilter::Warn)
        .level_for("devtools_core", log::LevelFilter::Off)
        .level_for("devtools_core::server", log::LevelFilter::Off)
        .level_for("tracing", log::LevelFilter::Warn)
        // Add zbus-related filters for production
        .level_for(
            "zbus",
            if cfg!(debug_assertions) {
                log::LevelFilter::Debug
            } else {
                log::LevelFilter::Error
            },
        )
        .level_for(
            "zbus::connection",
            if cfg!(debug_assertions) {
                log::LevelFilter::Debug
            } else {
                log::LevelFilter::Error
            },
        )
        .level_for(
            "zbus::connection::handshake",
            if cfg!(debug_assertions) {
                log::LevelFilter::Debug
            } else {
                log::LevelFilter::Error
            },
        )
        // Set application specific level
        .level_for(
            "lore_designer_lib",
            if cfg!(debug_assertions) {
                log::LevelFilter::Trace
            } else {
                log::LevelFilter::Info
            },
        );
    app.handle().plugin(builder.build())?;

    Ok(())
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let mut builder = tauri::Builder::default()
        .plugin(tauri_plugin_window_state::Builder::new().build())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .plugin(tauri_plugin_store::Builder::new().build())
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_clipboard_manager::init());

    // Solo incluir el plugin decorum si NO estamos en Linux
    #[cfg(not(target_os = "linux"))]
    {
        builder = builder.plugin(tauri_plugin_decorum::init());
    }

    builder
        .setup(|app| {
            setup_logger(app)?;
            if let Err(e) = system_info::log_system_info(app.handle()) {
                error!("Failed to log system info: {}", e);
            }
            preferences::init_preferences(app.handle())?;

            // Solo configurar el overlay titlebar si NO estamos en Linux
            #[cfg(not(target_os = "linux"))]
            {
                let main_window = app.get_webview_window("main").unwrap();
                main_window.create_overlay_titlebar().unwrap();

                #[cfg(target_os = "macos")]
                {
                    // Set a custom inset to the traffic lights
                    main_window.set_traffic_lights_inset(12.0, 16.0).unwrap();

                    // Make window transparent without privateApi
                    main_window.make_transparent().unwrap();

                    // Set window level
                    // NSWindowLevel: https://developer.apple.com/documentation/appkit/nswindowlevel
                    main_window.set_window_level(25).unwrap();
                }
            }

            Ok(())
        })
        .invoke_handler(tauri::generate_handler![
            commands::get_preferences,
            commands::save_preferences,
            commands::set_theme,
            commands::set_language,
            commands::update_last_project,
            workspace::commands::create_workspace,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}