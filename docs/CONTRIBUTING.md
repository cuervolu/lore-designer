# Contributing to Lore Designer

Thank you for your interest in contributing to Lore Designer! This guide will help you get started with the project.

## Prerequisites

Before you begin, make sure you have the following installed:

- **Bun** (latest version) - [Installation guide](https://bun.sh)
- **Rust** (1.70 or later) - [Installation guide](https://www.rust-lang.org/tools/install)
- **Node.js** (18 or later) - Required for some build tools
- **At least 5-8 GB of free disk space** - The project dependencies, Rust toolchain, and build artifacts can be quite large

### Platform-Specific Requirements

**Windows:**

- Visual Studio Build Tools or Visual Studio with C++ development tools
- WebView2 (usually pre-installed on Windows 10/11)

**Linux:**

- WebKit2GTK development libraries
- Build essentials (`build-essential` on Debian/Ubuntu)

Check the [Tauri prerequisites guide](https://tauri.app/start/prerequisites/) for detailed platform-specific setup.

## Project Structure

Lore Designer is organized as a Bun monorepo:

```
lore-designer/
├── apps/
│   ├── desktop/          # Main Tauri application (Vue.js + Rust)
│   └── docs/             # Documentation website (Astro)
├── packages/             # Shared packages
└── plugins/
    └── tauri-plugin-tracing/  # Custom Tauri plugin for logging
```

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/cuervolu/lore-designer.git
cd lore-designer
```

### 2. Install Dependencies

```bash
bun install
```

### 3. Build the Tracing Plugin

The `tauri-plugin-tracing` is not published to npm, so you need to build it locally:

```bash
cd plugins/tauri-plugin-tracing
bun run build
cd ../..
```

This generates TypeScript declarations and bundles the plugin using Rollup.

### 4. Run the Desktop App

```bash
bun run dev:desktop
```

This starts the Tauri development server with hot-reload enabled.

## Development Commands

| Command                      | Description                                   |
| ---------------------------- | --------------------------------------------- |
| `bun run dev:desktop`        | Run the desktop app in development mode       |
| `bun run build:desktop`      | Build the desktop app for production          |
| `bun run dev:docs`           | Run the documentation site locally            |
| `bun shadcn add [component]` | Add a shadcn-vue component to the desktop app |
| `bun run lint`               | Run the linter across the codebase            |

## Code Style and Conventions

### TypeScript/Vue

- Use **Composition API** with `<script setup lang="ts">`
- Prefer `const` over `let` when possible
- Use TypeScript strict mode
- Avoid unnecessary dependencies
- Keep components focused and single-purpose

### Rust

- Follow idiomatic Rust conventions
- Use `PathBuf` for cross-platform file paths
- Handle errors with proper context (use `anyhow` or custom error types)
- Make I/O operations async when appropriate
- Respect the crate structure (`lore-workspaces`, `lore-editor`, etc.)

### General Guidelines
- Write descriptive commit messages
- Add comments only when the code isn't self-explanatory
- Prioritize simplicity over cleverness
- Test on all platforms when possible

## Adding shadcn-vue Components

To add a new component from shadcn-vue:

```bash
bun shadcn add button
```

This will add the component to `apps/desktop/src/components/ui/`.

## Architecture Principles

1. **Cross-platform first** - All code must work on all supported OSes
2. **Data safety** - User content is critical; avoid data loss at all costs
3. **Performance** - Support workspaces with 1000+ files efficiently
4. **Modular design** - Respect separation of concerns between crates and modules
5. **User experience** - Writers are not developers; keep it simple

## Proposing Changes

### For New Features

- Open an issue first to discuss the approach
- Explain the problem you're solving
- Consider impact on performance, security, and compatibility
- If adding a dependency, justify why it's necessary

### For Bug Fixes

- Reference the issue number in your PR
- Include steps to reproduce the bug
- Explain how your fix resolves the issue

### Pull Request Process

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Make your changes following the code style guidelines
4. Test on all platforms if possible
5. Commit with clear, descriptive messages
6. Push to your fork and open a pull request
7. Respond to code review feedback

## Testing

Currently, the project uses manual testing. We're working on establishing automated test coverage. When contributing:

- Test your changes thoroughly
- Check edge cases (empty workspaces, large files, special characters in paths)
- Verify cross-platform behavior when possible

## Getting Help

- Open an issue for bug reports or feature requests
- Check existing issues before creating a new one
- Be respectful and constructive in discussions

## License

By contributing to Lore Designer, you agree that your contributions will be licensed under the project's license.

---

Thank you for helping make Lore Designer better for writers and worldbuilders everywhere! Happy coding! :D
