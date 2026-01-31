use anyhow::{Context, Result};
use ignore::gitignore::{Gitignore, GitignoreBuilder};
use std::path::Path;
use std::sync::Arc;

const DEFAULT_LOREIGNORE: &str = r#"# Lore Designer - File Explorer Ignore Rules
# Syntax: same as .gitignore

# Internal directories
.lore/
.git/

# Build artifacts
node_modules/
dist/
build/
target/

# System files
.DS_Store
Thumbs.db
desktop.ini

# Temporary files
*.tmp
*.bak
~*
*.swp
*.swo

# User can add custom patterns below:
"#;

pub struct IgnoreMatcher {
    gitignore: Gitignore,
    workspace_path: Arc<Path>,
}

impl IgnoreMatcher {
    pub fn new(workspace_path: impl AsRef<Path>) -> Result<Self> {
        let workspace = workspace_path.as_ref();
        let ignore_path = workspace.join(".loreignore");

        if !ignore_path.exists() {
            Self::create_default_loreignore(&ignore_path)?;
        }

        let mut builder = GitignoreBuilder::new(workspace);
        builder.add(&ignore_path);

        let gitignore = builder.build()?;

        Ok(Self {
            gitignore,
            workspace_path: Arc::from(workspace),
        })
    }

pub fn should_ignore(&self, path: &Path) -> bool {
    let relative_path = match path.strip_prefix(&*self.workspace_path) {
        Ok(rel) => rel,
        Err(_) => return false, // If we can't get relative path, don't ignore
    };

    // Always ignore anything in .lore directory
    if let Some(first_component) = relative_path.components().next()
        && first_component.as_os_str() == ".lore" {
            return true;
        }

    // Check against gitignore rules
    self.gitignore
        .matched(relative_path, path.is_dir())
        .is_ignore()
}

    pub fn reload(&mut self) -> Result<()> {
        let ignore_path = self.workspace_path.join(".loreignore");
        let mut builder = GitignoreBuilder::new(&*self.workspace_path);
        builder.add(&ignore_path);
        self.gitignore = builder.build()?;
        Ok(())
    }

    fn create_default_loreignore(path: &Path) -> Result<()> {
        std::fs::write(path, DEFAULT_LOREIGNORE)
            .context("Failed to create default .loreignore file")
    }
}

pub fn get_loreignore_content(workspace_path: impl AsRef<Path>) -> Result<String> {
    let ignore_path = workspace_path.as_ref().join(".loreignore");

    if !ignore_path.exists() {
        return Ok(DEFAULT_LOREIGNORE.to_string());
    }

    std::fs::read_to_string(&ignore_path).context("Failed to read .loreignore file")
}

pub fn update_loreignore_content(workspace_path: impl AsRef<Path>, content: String) -> Result<()> {
    let ignore_path = workspace_path.as_ref().join(".loreignore");
    std::fs::write(&ignore_path, content).context("Failed to update .loreignore file")
}
