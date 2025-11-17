use crate::models::{LogEntry, LogState};
use tauri::{command, AppHandle, Runtime, State};

#[command]
pub(crate) async fn log_trace<R: Runtime>(
    _app: AppHandle<R>,
    message: String,
    target: Option<String>,
) -> Result<(), ()> {
    let target = target.unwrap_or_else(|| "frontend".to_string());
    tracing::event!(tracing::Level::TRACE, frontend_target = %target, "{message}");
    Ok(())
}

#[command]
pub(crate) async fn log_debug<R: Runtime>(
    _app: AppHandle<R>,
    message: String,
    target: Option<String>,
) -> Result<(), ()> {
    let target = target.unwrap_or_else(|| "frontend".to_string());
    tracing::event!(tracing::Level::DEBUG, frontend_target = %target, "{message}");
    Ok(())
}

#[command]
pub(crate) async fn log_info<R: Runtime>(
    _app: AppHandle<R>,
    message: String,
    target: Option<String>,
) -> Result<(), ()> {
    let target = target.unwrap_or_else(|| "frontend".to_string());
    tracing::event!(tracing::Level::INFO, frontend_target = %target, "{message}");
    Ok(())
}

#[command]
pub(crate) async fn log_warn<R: Runtime>(
    _app: AppHandle<R>,
    message: String,
    target: Option<String>,
) -> Result<(), ()> {
    let target = target.unwrap_or_else(|| "frontend".to_string());
    tracing::event!(tracing::Level::WARN, frontend_target = %target, "{message}");
    Ok(())
}

#[command]
pub(crate) async fn log_error<R: Runtime>(
    _app: AppHandle<R>,
    message: String,
    target: Option<String>,
) -> Result<(), ()> {
    let target = target.unwrap_or_else(|| "frontend".to_string());
    tracing::event!(tracing::Level::ERROR, frontend_target = %target, "{message}");
    Ok(())
}


#[command]
pub(crate) fn get_log_history(state: State<'_, LogState>) -> Vec<LogEntry> {
    let buffer = state.buffer.lock().unwrap();
    buffer.iter().cloned().collect()
}

#[command]
pub(crate) fn clear_log_buffer(state: State<'_, LogState>) {
    let mut buffer = state.buffer.lock().unwrap();
    buffer.clear();
}