const COMMANDS: &[&str] = &[
    "log_trace",
    "log_debug",
    "log_info",
    "log_warn",
    "log_error",
    "get_log_history",
    "clear_log_buffer",
];

fn main() {
    tauri_plugin::Builder::new(COMMANDS)
        // .android_path("android")
        // .ios_path("ios")
        .build();
}