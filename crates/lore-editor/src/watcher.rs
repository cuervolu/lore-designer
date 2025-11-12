use crate::IndexManager;
use anyhow::Context;
use crossbeam_channel::Sender;
use tracing::{debug, error};
use notify::{
    Event, EventHandler, RecommendedWatcher, RecursiveMode, Watcher,
    event::{AccessKind, AccessMode, EventKind, ModifyKind, RenameMode},
};
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::sync::{Arc, LazyLock, Mutex};
use std::time::{Duration, Instant};
use tauri::{AppHandle, Emitter};

const DEBOUNCE_MS: u64 = 300;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum FileEvent {
    Created(PathBuf),
    Modified(PathBuf),
    Deleted(PathBuf),
    Renamed(PathBuf, PathBuf),
}

static WATCHERS: LazyLock<Mutex<HashMap<String, Arc<FileSystemWatcher>>>> =
    LazyLock::new(|| Mutex::new(HashMap::new()));

pub struct FileSystemWatcher {
    /// Path being watched
    path: PathBuf,

    /// The actual file system watcher
    _watcher: Mutex<Option<RecommendedWatcher>>,

    /// Last event time for debouncing
    last_events: Mutex<HashMap<PathBuf, Instant>>,

    /// Tauri app handle
    app: Option<AppHandle>,
}

struct FileSystemEventHandler {
    /// Path being watched
    workspace_path: PathBuf,

    /// Channel sender for events
    event_sender: Sender<FileEvent>,

    /// Tauri app handle
    app: Option<AppHandle>,
}

impl FileSystemWatcher {
    pub fn new(
        workspace_path: impl AsRef<Path>,
        app: Option<AppHandle>,
    ) -> anyhow::Result<Arc<Self>> {
        let path = workspace_path.as_ref().to_path_buf();
        let path_str = path.display().to_string();

        {
            let watchers = WATCHERS.lock().unwrap();
            if let Some(watcher) = watchers.get(&path_str) {
                return Ok(watcher.clone());
            }
        }

        let (event_sender, event_receiver) = crossbeam_channel::unbounded();

        let event_handler = FileSystemEventHandler {
            workspace_path: path.clone(),
            event_sender,
            app: app.clone(),
        };

        let mut watcher = notify::recommended_watcher(event_handler)?;

        watcher.watch(&path, RecursiveMode::Recursive)?;

        debug!("Started watching directory: {}", path.display());

        let watcher_instance = Arc::new(Self {
            path,
            _watcher: Mutex::new(Some(watcher)),
            last_events: Mutex::new(HashMap::new()),
            app,
        });

        {
            let mut watchers = WATCHERS.lock().unwrap();
            watchers.insert(path_str, watcher_instance.clone());
        }

        let watcher_clone = watcher_instance.clone();
        std::thread::spawn(move || {
            watcher_clone.process_events(event_receiver);
        });

        Ok(watcher_instance)
    }

    pub fn get(workspace_path: impl AsRef<Path>) -> Option<Arc<Self>> {
        let path_str = workspace_path.as_ref().display().to_string();
        let watchers = WATCHERS.lock().unwrap();
        watchers.get(&path_str).cloned()
    }

    pub fn stop(workspace_path: impl AsRef<Path>) -> anyhow::Result<()> {
        let path_str = workspace_path.as_ref().display().to_string();

        let mut watchers = WATCHERS.lock().unwrap();
        if let Some(watcher) = watchers.remove(&path_str) {
            // Explicitly drop the watcher to stop it
            let mut watcher_guard = watcher._watcher.lock().unwrap();
            *watcher_guard = None;

            debug!("Stopped watching directory: {}", path_str);
        }

        Ok(())
    }

    fn process_events(&self, receiver: crossbeam_channel::Receiver<FileEvent>) {
        for event in receiver {
            // Skip if this is a duplicate event within the debounce period
            let should_process = {
                let mut last_events = self.last_events.lock().unwrap();
                let now = Instant::now();

                let path = match &event {
                    FileEvent::Created(path) => path,
                    FileEvent::Modified(path) => path,
                    FileEvent::Deleted(path) => path,
                    FileEvent::Renamed(old_path, _) => old_path,
                };

                if let Some(last_time) = last_events.get(path) {
                    if now.duration_since(*last_time) < Duration::from_millis(DEBOUNCE_MS) {
                        false
                    } else {
                        last_events.insert(path.clone(), now);
                        true
                    }
                } else {
                    last_events.insert(path.clone(), now);
                    true
                }
            };

            if !should_process {
                continue;
            }

            debug!("Processing file event: {:?}", event);

            // Update file index based on the event
            match &event {
                FileEvent::Created(path) | FileEvent::Modified(path) => {
                    if let Err(e) = self.handle_file_change(path) {
                        error!("Failed to handle file change: {}", e);
                    }
                }
                FileEvent::Deleted(path) => {
                    if let Err(e) = self.handle_file_deletion(path) {
                        error!("Failed to handle file deletion: {}", e);
                    }
                }
                FileEvent::Renamed(old_path, new_path) => {
                    if let Err(e) = self.handle_file_rename(old_path, new_path) {
                        error!("Failed to handle file rename: {}", e);
                    }
                }
            }
        }
    }

    fn handle_file_change(&self, path: &Path) -> anyhow::Result<()> {
        // If path is in .lore directory, ignore it
        if is_in_lore_dir(path, &self.path) {
            return Ok(());
        }

        IndexManager::trigger_incremental_update(&self.path, path)?;

        if let Some(app) = &self.app {
            emit_file_change_event(app, self.path.display().to_string())?;
        }

        Ok(())
    }

    /// Handle file deletion
    fn handle_file_deletion(&self, path: &Path) -> anyhow::Result<()> {
        if is_in_lore_dir(path, &self.path) {
            return Ok(());
        }

        IndexManager::trigger_incremental_update(&self.path, path)?;

        if let Some(app) = &self.app {
            emit_file_change_event(app, self.path.display().to_string())?;
        }

        Ok(())
    }

    fn handle_file_rename(&self, old_path: &Path, new_path: &Path) -> anyhow::Result<()> {
        if is_in_lore_dir(old_path, &self.path) || is_in_lore_dir(new_path, &self.path) {
            return Ok(());
        }

        IndexManager::trigger_incremental_update(&self.path, new_path)?;

        if let Some(app) = &self.app {
            emit_file_change_event(app, self.path.display().to_string())?;
        }

        Ok(())
    }
}

impl EventHandler for FileSystemEventHandler {
    fn handle_event(&mut self, result: notify::Result<Event>) {
        match result {
            Ok(event) => {
                // Skip events for .lore directory
                if event
                    .paths
                    .iter()
                    .any(|p| is_in_lore_dir(p, &self.workspace_path))
                {
                    return;
                }

                match event.kind {
                    // File created
                    EventKind::Create(_) => {
                        if let Some(path) = event.paths.first() {
                            let _ = self.event_sender.send(FileEvent::Created(path.clone()));
                        }
                    }

                    // File modified
                    EventKind::Modify(kind) => match kind {
                        ModifyKind::Data(_) | ModifyKind::Metadata(_) => {
                            if let Some(path) = event.paths.first() {
                                let _ = self.event_sender.send(FileEvent::Modified(path.clone()));
                            }
                        }
                        ModifyKind::Name(RenameMode::Both) => {
                            if event.paths.len() >= 2 {
                                let _ = self.event_sender.send(FileEvent::Renamed(
                                    event.paths[0].clone(),
                                    event.paths[1].clone(),
                                ));
                            }
                        }
                        _ => {}
                    },

                    // File removed
                    EventKind::Remove(_) => {
                        if let Some(path) = event.paths.first() {
                            let _ = self.event_sender.send(FileEvent::Deleted(path.clone()));
                        }
                    }

                    // File accessed - we usually don't care about this, but just in case
                    EventKind::Access(kind) => {
                        if matches!(kind, AccessKind::Close(AccessMode::Write)) {
                            if let Some(path) = event.paths.first() {
                                let _ = self.event_sender.send(FileEvent::Modified(path.clone()));
                            }
                        }
                    }

                    // Other events we don't care about at the moment
                    _ => {}
                }
            }
            Err(e) => {
                error!("Error watching files: {}", e);
            }
        }
    }
}

fn is_in_lore_dir(path: &Path, workspace_path: &Path) -> bool {
    if let Ok(rel_path) = path.strip_prefix(workspace_path) {
        let path_str = rel_path.to_string_lossy();
        path_str.starts_with(".lore/")
    } else {
        false
    }
}

fn emit_file_change_event(app: &AppHandle, workspace_path: String) -> anyhow::Result<()> {
    app.emit(
        "file-system-changed",
        serde_json::json!({
            "workspace_path": workspace_path,
            "timestamp": std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap_or_default()
                .as_secs()
        }),
    )
    .context("Failed to emit file-system-changed event")?;

    Ok(())
}
