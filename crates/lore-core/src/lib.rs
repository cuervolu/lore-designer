//! Shared Rust workspace crate placeholder.
//!
//! This crate exists so the Cargo workspace can be rooted around `crates/*`
//! from the beginning, even before the domain logic is extracted out of the
//! Tauri application crate.

pub fn workspace_ready() -> bool {
    true
}
