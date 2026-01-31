use crate::{IgnoreMatcher, IndexManager};
use anyhow::Context;
use crossbeam_channel::Sender;
use notify::{
    Event, EventHandler, RecommendedWatcher, RecursiveMode, Watcher,
    event::{AccessKind, AccessMode, EventKind, ModifyKind, RenameMode},
};
use std::collections::HashMap;
use std::path::{Path, PathBuf};
use std::sync::{Arc, LazyLock, Mutex};
use std::time::{Duration, Instant};
use tauri::{AppHandle, Emitter};
use tracing::{debug, error};

const DEBOUNCE_MS: u64 = 500;

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

    ignore_matcher: Arc<Mutex<IgnoreMatcher>>,

    /// Tauri app handle
    pub(crate) app: Option<AppHandle>,
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

        let ignore_matcher = Arc::new(Mutex::new(IgnoreMatcher::new(&path)?));

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
            ignore_matcher,
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
        let mut pending_events: HashMap<PathBuf, FileEvent> = HashMap::new();
        let mut last_process = Instant::now();

        loop {
            let timeout = Duration::from_millis(DEBOUNCE_MS);

            match receiver.recv_timeout(timeout) {
                Ok(event) => {
                    let path = match &event {
                        FileEvent::Created(p) | FileEvent::Modified(p) | FileEvent::Deleted(p) => {
                            p.clone()
                        }
                        FileEvent::Renamed(_, new_p) => new_p.clone(),
                    };

                    if self.should_ignore(&path) {
                        continue;
                    }

                    pending_events.insert(path, event);
                }
                Err(_) => {
                    if !pending_events.is_empty()
                        && last_process.elapsed() >= Duration::from_millis(DEBOUNCE_MS)
                    {
                        for (path, event) in pending_events.drain() {
                            if let Err(e) = self.handle_event(&path, &event) {
                                error!("Failed to handle event for {:?}: {}", path, e);
                            }
                        }
                        last_process = Instant::now();
                    }
                }
            }
        }
    }

    fn should_ignore(&self, path: &Path) -> bool {
        let matcher = self.ignore_matcher.lock().unwrap();
        matcher.should_ignore(path)
    }

    fn handle_event(&self, path: &Path, event: &FileEvent) -> anyhow::Result<()> {
        debug!("Processing file event: {:?} for {:?}", event, path);

        match event {
            FileEvent::Created(_) | FileEvent::Modified(_) => {
                IndexManager::trigger_incremental_update(&self.path, path)?;
                self.emit_granular_event("file-modified", path)?;
            }
            FileEvent::Deleted(_) => {
                IndexManager::trigger_incremental_update(&self.path, path)?;
                self.emit_granular_event("file-deleted", path)?;
            }
            FileEvent::Renamed(old_path, new_path) => {
                IndexManager::trigger_incremental_update(&self.path, new_path)?;
                self.emit_rename_event(old_path, new_path)?;
            }
        }

        Ok(())
    }

    fn emit_granular_event(&self, event_name: &str, path: &Path) -> anyhow::Result<()> {
        if let Some(app) = &self.app {
            let rel_path = path
                .strip_prefix(&self.path)
                .unwrap_or(path)
                .display()
                .to_string();

            app.emit(
                event_name,
                serde_json::json!({
                    "workspace_path": self.path.display().to_string(),
                    "file_path": rel_path,
                    "timestamp": std::time::SystemTime::now()
                        .duration_since(std::time::UNIX_EPOCH)
                        .unwrap_or_default()
                        .as_secs()
                }),
            )
            .context(format!("Failed to emit {} event", event_name))?;
        }
        Ok(())
    }

    fn emit_rename_event(&self, old_path: &Path, new_path: &Path) -> anyhow::Result<()> {
        if let Some(app) = &self.app {
            let old_rel = old_path
                .strip_prefix(&self.path)
                .unwrap_or(old_path)
                .display()
                .to_string();

            let new_rel = new_path
                .strip_prefix(&self.path)
                .unwrap_or(new_path)
                .display()
                .to_string();

            app.emit(
                "file-renamed",
                serde_json::json!({
                    "workspace_path": self.path.display().to_string(),
                    "old_path": old_rel,
                    "new_path": new_rel,
                    "timestamp": std::time::SystemTime::now()
                        .duration_since(std::time::UNIX_EPOCH)
                        .unwrap_or_default()
                        .as_secs()
                }),
            )
            .context("Failed to emit file-renamed event")?;
        }
        Ok(())
    }

    pub fn reload_ignore_rules(&self) -> anyhow::Result<()> {
        let mut matcher = self.ignore_matcher.lock().unwrap();
        matcher.reload()
    }

    pub fn stop_all() -> anyhow::Result<()> {
        let mut watchers = WATCHERS.lock().unwrap();
        let paths: Vec<String> = watchers.keys().cloned().collect();

        for path in paths {
            if let Some(watcher) = watchers.remove(&path) {
                let mut watcher_guard = watcher._watcher.lock().unwrap();
                *watcher_guard = None;
                debug!("Stopped watching directory: {}", path);
            }
        }

        Ok(())
    }
}

impl EventHandler for FileSystemEventHandler {
    fn handle_event(&mut self, result: notify::Result<Event>) {
        match result {
            Ok(event) => match event.kind {
                EventKind::Create(_) => {
                    if let Some(path) = event.paths.first() {
                        let _ = self.event_sender.send(FileEvent::Created(path.clone()));
                    }
                }
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
                EventKind::Remove(_) => {
                    if let Some(path) = event.paths.first() {
                        let _ = self.event_sender.send(FileEvent::Deleted(path.clone()));
                    }
                }
                EventKind::Access(AccessKind::Close(AccessMode::Write)) => {
                    if let Some(path) = event.paths.first() {
                        let _ = self.event_sender.send(FileEvent::Modified(path.clone()));
                    }
                }
                _ => {}
            },
            Err(e) => {
                error!("Error watching files: {}", e);
            }
        }
    }
}
