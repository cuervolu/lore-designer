use crate::error::AppError;
use sysinfo::{System, Components, Disks};

#[tauri::command]
pub fn get_system_info() -> Result<String, AppError> {
    let mut sys = System::new_all();
    sys.refresh_all();

    let os_info = format!(
        "OS: {} {}",
        System::name().unwrap_or_else(|| "Unknown".to_string()),
        System::os_version().unwrap_or_else(|| "Unknown".to_string()),
    );

    let memory_info = format!(
        "Memory: {:.1} GB used / {:.1} GB total",
        sys.used_memory() as f64 / 1_073_741_824.0,
        sys.total_memory() as f64 / 1_073_741_824.0,
    );

    let cpu_info = format!(
        "CPU: {} x {}",
        sys.cpus().len(),
        sys.cpus()[0].brand()
    );

    let disks = Disks::new_with_refreshed_list();
    let total_space: u64 = disks.iter().map(|disk| disk.total_space()).sum();
    let available_space: u64 = disks.iter().map(|disk| disk.available_space()).sum();
    let disk_info = format!(
        "Disk: {:.1} GB free / {:.1} GB total",
        available_space as f64 / 1_073_741_824.0,
        total_space as f64 / 1_073_741_824.0,
    );

    let components = Components::new_with_refreshed_list();
    let max_temp = components.iter()
        .filter_map(|component| {
            if component.label().to_lowercase().contains("cpu") {
                Some(component.temperature())
            } else {
                None
            }
        })
        .max_by(|a, b| a.partial_cmp(b).unwrap())
        .unwrap_or(0.0);

    let temp_info = format!("CPU Temperature: {:.1}°C", max_temp);

    let uptime = System::uptime();
    let uptime_str = format!(
        "Uptime: {}d {}h {}m",
        uptime / 86400,
        (uptime % 86400) / 3600,
        (uptime % 3600) / 60
    );

    Ok(format!(
        "{}\n{}\n{}\n{}\n{}\n{}",
        os_info, cpu_info, memory_info, disk_info, temp_info, uptime_str
    ))
}