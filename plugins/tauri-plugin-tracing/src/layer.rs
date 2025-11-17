use crate::models::{LogEntry, LogState};
use tauri::{AppHandle, Emitter, Runtime};
use tracing::Subscriber;
use tracing_subscriber::layer::{Context, Layer};
use tracing_subscriber::registry::LookupSpan;

/// The event emitted to the frontend.
const LOG_EVENT: &str = "log:entry";

/// A `tracing::Layer` that captures events and emits them to the Tauri webview.
pub struct WebviewEventLayer<R: Runtime> {
    app_handle: AppHandle<R>,
    log_state: LogState,
}

impl<R: Runtime> WebviewEventLayer<R> {
    pub fn new(app_handle: AppHandle<R>, log_state: LogState) -> Self {
        Self {
            app_handle,
            log_state,
        }
    }

    fn emit_batch(&self, batch: Vec<LogEntry>) {
        if let Err(e) = self.app_handle.emit(LOG_EVENT, &batch) {
            // We can't use tracing::error! here, it would create an infinite loop.
            eprintln!("[tauri-plugin-tracing] Failed to emit log batch: {}", e);
        }
    }
}

impl<S, R> Layer<S> for WebviewEventLayer<R>
where
    S: Subscriber + for<'a> LookupSpan<'a>,
    R: Runtime,
{
    fn on_event(&self, event: &tracing::Event<'_>, _ctx: Context<'_, S>) {
        let entry = LogEntry::new(event);
        self.log_state.add_to_buffer(entry.clone());

        let mut limiter = self.log_state.rate_limiter.lock().unwrap();
        if let Some(batch) = limiter.add(entry) {
            drop(limiter); // Release the mutex before emitting
            self.emit_batch(batch);
        }
    }

    fn on_close(&self, _id: tracing::Id, _ctx: Context<'_, S>) {
        let mut limiter = self.log_state.rate_limiter.lock().unwrap();
        if let Some(batch) = limiter.flush() {
            drop(limiter);
            self.emit_batch(batch);
        }
    }
}