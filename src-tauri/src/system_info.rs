use chrono::Local;
use log::info;
use sysinfo::System;
use tauri::AppHandle;

pub fn log_system_info(app: &AppHandle) -> Result<(), Box<dyn std::error::Error>> {
    let app_version = app.package_info().version.to_string();
    let tauri_version = tauri::VERSION;
    let current_date = Local::now().format("%Y-%m-%d %H:%M:%S").to_string();

    info!(
        "Welcome to Lore Designer v{} (Tauri v{})",
        app_version, tauri_version
    );
    info!("Started at: {}", current_date);

    let kernel_version = System::kernel_version().unwrap_or_else(|| "Unknown".to_string());
    let long_os_version = System::long_os_version().unwrap_or_else(|| "Unknown".to_string());

    info!(
        "Running on: {}, Kernel Version: {}",
        long_os_version, kernel_version
    );

    #[cfg(target_os = "linux")]
    {
        // Información específica de Linux
        let id_like = System::distribution_id_like().join(", ");
        if !id_like.is_empty() {
            info!(
                "Linux distribution: {} (based on: {})",
                distribution_id, id_like
            );
        } else {
            info!("Linux distribution: {}", distribution_id);
        }

        if let Ok(desktop) = std::env::var("XDG_CURRENT_DESKTOP") {
            info!("Desktop Environment: {}", desktop);
        }

        if let Ok(session_type) = std::env::var("XDG_SESSION_TYPE") {
            info!("Display Server: {}", session_type);
        }
    }

    Ok(())
}
