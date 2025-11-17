use crate::config::{Config, LogTarget, RotationConfig, RotationStrategy};
use crate::layer::WebviewEventLayer;
use crate::models::LogState;
use std::path::PathBuf;
use tauri::{
    plugin::{PluginApi},
    AppHandle, Manager, Runtime,
};
use tracing_appender::non_blocking::WorkerGuard;
use tracing_subscriber::layer::SubscriberExt;
use tracing_subscriber::util::SubscriberInitExt;
use tracing_subscriber::{fmt, Layer};

/// Alias for the complex BoxedLayer type.
type BoxedLayer<S> = Box<dyn Layer<S> + Send + Sync + 'static>;

/// Alias for the complex FileLayer type.
type FileLayer = fmt::Layer<
    tracing_subscriber::Registry,
    fmt::format::DefaultFields,
    fmt::format::Format,
    tracing_appender::non_blocking::NonBlocking,
>;

/// Initializes the tracing subscriber.
pub fn init<R: Runtime>(
    app: &AppHandle<R>,
    api: PluginApi<R, Config>,
) -> crate::Result<()> {
    let config = api.config().clone();
    let app_handle = app.clone();
    let app_name = app.package_info().name.clone();

    let filter = config.level;

    let mut log_state = LogState::new();

    let mut layers: Vec<BoxedLayer<tracing_subscriber::Registry>> = Vec::new();
    let mut guards: Vec<WorkerGuard> = Vec::new();

    for target in config.targets {
        match target {
            LogTarget::Stdout => {
                let layer = fmt::layer()
                    .with_writer(std::io::stdout)
                    .with_filter(filter);
                layers.push(layer.boxed());
            }
            LogTarget::Webview => {
                let layer = WebviewEventLayer::new(app_handle.clone(), log_state.clone())
                    .with_filter(filter);
                layers.push(layer.boxed());
            }
            LogTarget::LogDir { fileName } => {
                if let Ok(log_dir) = app_handle.path().app_log_dir() {
                    let file_name = fileName.unwrap_or_else(|| format!("{}.log", app_name));
                    let path = log_dir.join(file_name);
                    let (layer, guard) = create_file_layer(path, &config.rotation);
                    if let Some(l) = layer {
                        layers.push(l.with_filter(filter).boxed());
                    }
                    if let Some(g) = guard {
                        guards.push(g);
                    }
                }
            }
            LogTarget::Folder { path, fileName } => {
                let file_name = fileName.unwrap_or_else(|| format!("{}.log", app_name));
                let path = path.join(file_name);
                let (layer, guard) = create_file_layer(path, &config.rotation);
                if let Some(l) = layer {
                    layers.push(l.with_filter(filter).boxed());
                }
                if let Some(g) = guard {
                    guards.push(g);
                }
            }
        }
    }

    log_state.file_guards.extend(guards);
    app.manage(log_state);

    tracing_subscriber::registry()
        .with(layers)
        .try_init()
        .map_err(|e| crate::Error::TracingInit(e.to_string()))?;

    // Log en inglés
    tracing::info!(
        "tauri-plugin-tracing initialized. Level: {}, App: {}",
        config.level,
        app_name
    );

    Ok(())
}

/// Helper to create a file layer with rotation
fn create_file_layer(
    path: PathBuf,
    config: &RotationConfig,
) -> (
    Option<FileLayer>, 
    Option<WorkerGuard>,
) {
    if let Some(parent) = path.parent() {
        if !parent.exists() {
            if let Err(e) = std::fs::create_dir_all(parent) {
                eprintln!("[tauri-plugin-tracing] Failed to create log dir: {}", e);
                return (None, None);
            }
        }
    }

    let (appender, guard) = match &config.strategy {
        RotationStrategy::None => {
            let file = match std::fs::File::create(path) {
                Ok(f) => f,
                Err(e) => {
                    eprintln!("[tauri-plugin-tracing] Failed to create log file: {}", e);
                    return (None, None);
                }
            };
            tracing_appender::non_blocking(file)
        }
        RotationStrategy::Daily => {
            tracing_appender::non_blocking(tracing_appender::rolling::daily(
                path.parent().unwrap(),
                path.file_name().unwrap(),
            ))
        }
    };

    let layer = fmt::layer().with_writer(appender).with_ansi(false);
    (Some(layer), Some(guard))
}