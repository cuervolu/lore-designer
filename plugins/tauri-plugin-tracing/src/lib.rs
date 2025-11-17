use tauri::{
    plugin::{Builder, TauriPlugin},
    Runtime,
};

mod commands;
mod config;
mod desktop;
mod error;
mod layer;
mod models;

pub use error::{Error, Result};

use config::Config;

/// Initializes the plugin.
/// This is the function called from the app's `main.rs`.
pub fn init<R: Runtime>() -> TauriPlugin<R, Config> {
    Builder::<R, Config>::new("tracing")
        .invoke_handler(tauri::generate_handler![
            commands::log_trace,
            commands::log_debug,
            commands::log_info,
            commands::log_warn,
            commands::log_error,
            commands::get_log_history,
            commands::clear_log_buffer
        ])
        .setup(|app, api| {
            // Initialize the tracing logic (subscriber, etc.)
            desktop::init(app, api)?;
            Ok(())
        })
        .build()
}
