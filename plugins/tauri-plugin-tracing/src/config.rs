use serde::{Deserialize, Serialize};
use std::path::PathBuf;
use tracing_subscriber::filter::LevelFilter;

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "camelCase")]
pub enum LogTarget {
    /// Writes logs to stdout.
    Stdout,
    /// Emits log events to the webview.
    Webview,
    /// Writes logs to a file in the app's default log directory.
    LogDir {
        /// File name. Defaults to the bundle name.
        fileName: Option<String>,
    },
    /// Writes logs to a file in a custom folder.
    Folder {
        path: PathBuf,
        /// File name. Defaults to the bundle name.
        fileName: Option<String>,
    },
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct Config {
    /// Global log level.
    #[serde(default = "default_level", with = "serde_level_filter")]
    pub level: LevelFilter,
    /// List of log targets to activate.
    #[serde(default = "default_targets")]
    pub targets: Vec<LogTarget>,
    /// File rotation configuration.
    #[serde(default)]
    pub rotation: RotationConfig,
}

/// Serde helper module for `tracing_subscriber::filter::LevelFilter`
mod serde_level_filter {
    use std::str::FromStr;
    use serde::{Deserializer, Serializer};
    use serde::de::Error;
    use super::*;

    /// Serializes `LevelFilter` to a string.
    pub fn serialize<S>(level: &LevelFilter, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        serializer.serialize_str(&level.to_string())
    }

    /// Deserializes `LevelFilter` from a string.
    pub fn deserialize<'de, D>(deserializer: D) -> Result<LevelFilter, D::Error>
    where
        D: Deserializer<'de>,
    {
        let s: String = Deserialize::deserialize(deserializer)?;
        LevelFilter::from_str(&s)
            .map_err(|e| Error::custom(format!("invalid log level '{}': {}", s, e)))
    }
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "camelCase")]
pub enum RotationStrategy {
    /// Never rotates.
    None,
    /// Rotates daily.
    Daily,
}

#[derive(Debug, Deserialize, Serialize, Clone)]
#[serde(rename_all = "camelCase")]
pub struct RotationConfig {
    /// Rotation strategy.
    #[serde(default)]
    pub strategy: RotationStrategy,
    /// Maximum number of log files to keep (if rotating).
    #[serde(default = "default_max_files")]
    pub max_files: Option<usize>,
}

impl Default for RotationStrategy {
    fn default() -> Self {
        RotationStrategy::None
    }
}

impl Default for RotationConfig {
    fn default() -> Self {
        Self {
            strategy: RotationStrategy::None,
            max_files: default_max_files(),
        }
    }
}

fn default_level() -> LevelFilter {
    LevelFilter::INFO
}

fn default_targets() -> Vec<LogTarget> {
    vec![LogTarget::Stdout]
}

fn default_max_files() -> Option<usize> {
    Some(5)
}

impl Default for Config {
    fn default() -> Self {
        Self {
            level: default_level(),
            targets: default_targets(),
            rotation: RotationConfig::default(),
        }
    }
}