mod core;
mod system_info;

use core::config::{commands, preferences};
use tracing::{error, info};
// use tauri_plugin_log::{fern::colors::ColoredLevelConfig, RotationStrategy};

// fn setup_logger(app: &mut tauri::App) -> Result<(), Box<dyn std::error::Error>> {
//     let base_log_level = if cfg!(debug_assertions) {
//         log::LevelFilter::Debug
//     } else {
//         log::LevelFilter::Info
//     };
//
//     let builder = tauri_plugin_log::Builder::new()
//         .max_file_size(1024 * 1024 * 10) // 10 MB
//         .rotation_strategy(RotationStrategy::KeepAll)
//         .timezone_strategy(tauri_plugin_log::TimezoneStrategy::UseLocal)
//         .with_colors(ColoredLevelConfig::default())
//         .level(base_log_level);
//
//     app.handle().plugin(builder.build())?;
//
//     Ok(())
// }

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_cli::init())
        .plugin(tauri_plugin_window_state::Builder::new().build())
        .plugin(tauri_plugin_updater::Builder::new().build())
        .plugin(tauri_plugin_store::Builder::new().build())
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_process::init())
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_clipboard_manager::init())
        .plugin(tauri_plugin_tracing::init())
        .setup(|app| {
            // setup_logger(app)?;

            info!("==================== Starting Lore Designer ====================");

            if let Err(e) = system_info::log_system_info(app.handle()) {
                error!("Failed to log system info: {}", e);
            }
            preferences::init_preferences(app.handle())?;

            Ok(())
        })
        .invoke_handler(tauri::generate_handler![
            commands::get_preferences,
            commands::save_preferences,
            commands::set_theme,
            commands::set_language,
            commands::update_last_project,
            lore_workspaces::create_workspace,
            lore_workspaces::get_workspace_icon,
            lore_workspaces::get_recent_workspaces_command,
            lore_workspaces::add_recent_workspace_command,
            lore_workspaces::remove_recent_workspace_command,
            lore_workspaces::check_workspace_exists_command,
            lore_workspaces::get_character_form_config,
            lore_workspaces::save_character_form_config,
            lore_editor::open_workspace_in_editor,
            lore_editor::get_workspace_file_tree,
            lore_editor::get_indexing_progress,
            lore_editor::get_file_content,
            lore_editor::save_file_content,
            lore_editor::load_editor_state,
            lore_editor::save_editor_state,
            lore_editor::open_file_in_editor,
            lore_editor::create_new_file,
            lore_editor::get_welcome_text,
            lore_editor::stop_watching_workspace,
            lore_editor::refresh_file_tree,
            lore_editor::create_file_from_template,
            lore_workspaces::open_existing_workspace,
            lore_editor::search_files_by_type,
            lore_editor::get_file_frontmatter,
            lore_editor::get_all_workspace_files,
        ])
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
