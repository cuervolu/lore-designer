mod core;
mod system_info;
use core::config::{commands, preferences};
use tauri_plugin_tracing::{Builder, Rotation, RotationStrategy};
use tracing::{error, info};


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
            lore_editor::refresh_file_tree,
            lore_editor::create_file_from_template,
            lore_workspaces::open_existing_workspace,
            lore_editor::search_files_by_type,
            lore_editor::get_file_frontmatter,
            lore_editor::get_all_workspace_files,
            lore_workspaces::register_image,
            lore_workspaces::update_image_references,
            lore_workspaces::validate_image_index,
            lore_workspaces::find_image_candidates,
            lore_workspaces::bulk_update_image_path,
            lore_editor::get_ignore_rules,
            lore_editor::update_ignore_rules,
            lore_editor::close_workspace,
        ])
        .on_window_event(|_window, event| {
            if let tauri::WindowEvent::Destroyed = event {
                // Cleanup when window is destroyed
                let _ = lore_editor::FileSystemWatcher::stop_all();
            }
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
