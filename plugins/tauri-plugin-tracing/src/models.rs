use serde::{Deserialize, Serialize};
use std::collections::VecDeque;
use std::sync::{Arc, Mutex};
use std::time::{Duration, Instant};
use tracing::field::Field;
use tracing::{Event, Level};
use tracing_appender::non_blocking::WorkerGuard;

#[derive(Clone, Debug, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct LogEntry {
    pub timestamp: String,
    pub level: String,
    pub target: String,
    pub message: String,
}

impl LogEntry {
    /// Creates a LogEntry from a tracing event.
    pub fn new(event: &Event<'_>) -> Self {
        let timestamp = chrono::Local::now().format("%H:%M:%S%.3f").to_string();

        let level = match *event.metadata().level() {
            Level::ERROR => "ERROR",
            Level::WARN => "WARN",
            Level::INFO => "INFO",
            Level::DEBUG => "DEBUG",
            Level::TRACE => "TRACE",
        }
            .to_string();

        let mut message = String::new();
        let mut fe_target = None; // To store the dynamic target
        event.record(&mut MessageVisitor::new(&mut message, &mut fe_target));

        // Use dynamic target if present, otherwise fallback to module path
        let final_target = fe_target.unwrap_or_else(|| event.metadata().target().to_string());

        Self {
            timestamp,
            level,
            target: final_target,
            message,
        }
    }
}

/// Simple visitor to extract fields from a tracing event.
pub struct MessageVisitor<'a> {
    message: &'a mut String,
    fe_target: &'a mut Option<String>,
}

impl<'a> MessageVisitor<'a> {
    /// Creates a new visitor.
    pub fn new(message: &'a mut String, fe_target: &'a mut Option<String>) -> Self {
        Self { message, fe_target }
    }
}

impl<'a> tracing::field::Visit for MessageVisitor<'a> {
    // Handle targets passed as `&str`
    fn record_str(&mut self, field: &Field, value: &str) {
        if field.name() == "frontend_target" {
            *self.fe_target = Some(value.to_string());
        }
    }

    fn record_debug(&mut self, field: &Field, value: &dyn std::fmt::Debug) {
        if field.name() == "message" {
            *self.message = format!("{:?}", value);
        } else if field.name() == "frontend_target" {
            // Handle targets passed as `%target`
            *self.fe_target = Some(format!("{:?}", value));
        }
    }
}


const MAX_BUFFER_SIZE: usize = 1000;
const BATCH_SIZE: usize = 20;
const BATCH_INTERVAL_MS: u64 = 75; // A bit more time to reduce load

#[derive(Debug)]
pub struct RateLimiter {
    batch: Vec<LogEntry>,
    last_flush: Instant,
}

impl RateLimiter {
    pub fn new() -> Self {
        Self {
            batch: Vec::new(),
            last_flush: Instant::now(),
        }
    }

    fn should_flush(&self) -> bool {
        self.batch.len() >= BATCH_SIZE
            || self.last_flush.elapsed() >= Duration::from_millis(BATCH_INTERVAL_MS)
    }

    pub fn add(&mut self, entry: LogEntry) -> Option<Vec<LogEntry>> {
        self.batch.push(entry);
        if self.should_flush() {
            let batch = std::mem::take(&mut self.batch);
            self.last_flush = Instant::now();
            Some(batch)
        } else {
            None
        }
    }

    pub fn flush(&mut self) -> Option<Vec<LogEntry>> {
        if !self.batch.is_empty() {
            let batch = std::mem::take(&mut self.batch);
            self.last_flush = Instant::now();
            Some(batch)
        } else {
            None
        }
    }
}

/// The Tauri-managed state that holds the buffer, rate-limiter, and file guards.
#[derive(Debug)]
pub struct LogState {
    pub buffer: Arc<Mutex<VecDeque<LogEntry>>>,
    pub rate_limiter: Arc<Mutex<RateLimiter>>,
    /// Keep worker guards alive to ensure non-blocking file logs are written.
    #[allow(dead_code)]
    pub file_guards: Vec<WorkerGuard>,
}

impl LogState {
    pub fn new() -> Self {
        Self {
            buffer: Arc::new(Mutex::new(VecDeque::with_capacity(MAX_BUFFER_SIZE))),
            rate_limiter: Arc::new(Mutex::new(RateLimiter::new())),
            file_guards: Vec::new(),
        }
    }

    pub fn add_to_buffer(&self, entry: LogEntry) {
        let mut buffer = self.buffer.lock().unwrap();
        if buffer.len() >= MAX_BUFFER_SIZE {
            buffer.pop_front(); // Removes the oldest if the buffer is full
        }
        buffer.push_back(entry);
    }
}

impl Clone for LogState {
    fn clone(&self) -> Self {
        Self {
            buffer: self.buffer.clone(),
            rate_limiter: Arc::new(Mutex::new(RateLimiter::new())),
            file_guards: Vec::new(), // Guards are not cloneable, new state gets new guards
        }
    }
}