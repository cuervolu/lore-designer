use std::sync::{Arc, Mutex};
use tauri::{AppHandle, Emitter};
use tauri_plugin_tracing::{tracing_subscriber::layer::Context, tracing_subscriber::Layer};
use tracing::{Event, Subscriber};

#[derive(Clone, serde::Serialize)]
struct ConsoleEvent {
    level: String,
    message: String,
    target: String,
    timestamp: String,
}

#[derive(Clone)]
pub struct ConsoleLayer {
    app: Arc<Mutex<Option<AppHandle>>>,
}

impl ConsoleLayer {
    pub fn new() -> Self {
        Self {
            app: Arc::new(Mutex::new(None)),
        }
    }

    pub fn set_app_handle(&self, app: AppHandle) {
        let mut guard = self.app.lock().unwrap();
        *guard = Some(app);
    }
}

impl<S> Layer<S> for ConsoleLayer
where
    S: Subscriber,
{
    fn on_event(&self, event: &Event, _ctx: Context<'_, S>) {
        if let Some(app) = self.app.lock().unwrap().as_ref() {
            let metadata = event.metadata();
            let level = metadata.level().to_string();
            let target = metadata.target().to_string();
            let timestamp = chrono::Local::now().format("%H:%M:%S").to_string();

            let mut visitor = MessageVisitor::default();
            event.record(&mut visitor);

            let payload = ConsoleEvent {
                level,
                target,
                message: visitor.message,
                timestamp,
            };

            let _ = app.emit("lore://console/log", payload);
        }
    }
}

#[derive(Default)]
struct MessageVisitor {
    message: String,
}

impl tracing::field::Visit for MessageVisitor {
    fn record_debug(&mut self, field: &tracing::field::Field, value: &dyn std::fmt::Debug) {
        if field.name() == "message" {
            self.message = format!("{:?}", value);
        }
    }

    fn record_str(&mut self, field: &tracing::field::Field, value: &str) {
        if field.name() == "message" {
            self.message = value.to_string();
        }
    }
}
