<!--VITE PLUS START-->

# Using Vite+, the Unified Toolchain for the Web

This project is using Vite+, a unified toolchain built on top of Vite, Rolldown, Vitest, tsdown, Oxlint, Oxfmt, and Vite Task. Vite+ wraps runtime management, package management, and frontend tooling in a single global CLI called `vp`. Vite+ is distinct from Vite, and it invokes Vite through `vp dev` and `vp build`. Run `vp help` to print a list of commands and `vp <command> --help` for information about a specific command.

Docs are local at `node_modules/vite-plus/docs` or online at https://viteplus.dev/guide/.

## Review Checklist

- [ ] Run `vp install` after pulling remote changes and before getting started.
- [ ] Run `vp check` and `vp test` to format, lint, type check and test changes.
- [ ] Check if there are `vite.config.ts` tasks or `package.json` scripts necessary for validation, run via `vp run <script>`.

<!--VITE PLUS END-->

---

<!--TAURI START-->

# Tauri v2 — Desktop App Development

Lore Designer is a **Tauri v2** desktop application. Tauri runs a Rust backend as the native process and serves the frontend (React + TypeScript) inside a WebView. The two sides communicate exclusively through **Commands** (frontend → Rust) and **Events** (Rust → frontend or frontend → frontend).

## Documentation References (LLM-friendly)

Full Tauri v2 docs index for LLMs: **<https://v2.tauri.app/llms.txt>**

Key sections most relevant to this project:

| Topic                             | URL                                                        |
| --------------------------------- | ---------------------------------------------------------- |
| Architecture overview             | <https://v2.tauri.app/concept/architecture>                |
| Inter-Process Communication       | <https://v2.tauri.app/concept/inter-process-communication> |
| Calling Rust from the frontend    | <https://v2.tauri.app/develop/calling-rust>                |
| Calling the frontend from Rust    | <https://v2.tauri.app/develop/calling-frontend>            |
| State management (Rust side)      | <https://v2.tauri.app/develop/state-management>            |
| Configuration files               | <https://v2.tauri.app/develop/configuration-files>         |
| Plugin development                | <https://v2.tauri.app/develop/plugins>                     |
| Mocking Tauri APIs in tests       | <https://v2.tauri.app/develop/tests/mocking>               |
| Debug in VS Code                  | <https://v2.tauri.app/develop/debug/vscode>                |
| Permissions & Capabilities        | <https://v2.tauri.app/security/capabilities>               |
| Embedding extra files (resources) | <https://v2.tauri.app/develop/resources>                   |

## Running the App

**Never run `vite` or `cargo` directly.** Use the Tauri CLI, which coordinates both processes:

```bash
# Development (starts Vite dev server + Rust watcher simultaneously)
vp run tauri dev

# Production build
vp run tauri build
```

Internally `tauri dev` does:

1. Starts the Vite dev server (`vp dev`) on the configured port.
2. Compiles the Rust crate in debug mode (`cargo build`).
3. Spawns the native window pointing at `http://localhost:<port>`.

Any change to a `.rs` file triggers a Rust recompile. Frontend hot-reload works normally via Vite HMR.

## Running Tests

```bash
# Frontend unit tests (Vitest via Vite+)
vp test

# Rust unit tests
cargo test --workspace

# Rust tests for a specific crate
cargo test -p lore-editor
cargo test -p lore-workspaces
```

Frontend tests that exercise Tauri commands must mock the `@tauri-apps/api` module — see the **Mocking** section below.

## Project Structure

```
lore-designer/
├── apps/
│   └── desktop/                  # Tauri app
│       ├── src/                  # React frontend (TypeScript)
│       │   ├── features/         # Feature-scoped modules
│       │   ├── widgets/          # Large UI blocks
│       │   └── core/             # Base utilities, router, stores
│       ├── src-tauri/            # Rust backend
│       │   ├── src/
│       │   │   ├── commands/     # #[tauri::command] handlers (thin wrappers only)
│       │   │   ├── state.rs      # AppState structs managed via Tauri State<T>
│       │   │   └── lib.rs        # tauri::Builder setup, plugin registration
│       │   ├── capabilities/     # JSON capability files (permissions)
│       │   └── tauri.conf.json   # Main Tauri config
├── packages/
│   └── shared/                   # @lore/shared — pure TS types, schemas, utils
├── crates/
│   ├── lore-editor/              # Content parsing, SQLite index, frontmatter
│   └── lore-workspaces/          # Workspace open/create/scan, file watcher
└── package.json                  # pnpm workspace root
```

## Tauri Commands — Pattern

Commands are the **only** way the frontend calls Rust. Keep command handlers thin; real logic lives in `lore-editor` / `lore-workspaces` crates.

**Rust side (`src-tauri/src/commands/`):**

```rust
// commands/workspace.rs
#[tauri::command]
pub async fn open_workspace(
    path: String,
    state: tauri::State<'_, AppState>,
) -> Result<WorkspaceManifest, String> {
    lore_workspaces::open(&path, &state.db)
        .await
        .map_err(|e| e.to_string())
}
```

Register in `lib.rs`:

```rust
tauri::Builder::default()
    .invoke_handler(tauri::generate_handler![
        commands::workspace::open_workspace,
    ])
```

**Frontend side:**

```typescript
import { invoke } from "@tauri-apps/api/core";

const manifest = await invoke<WorkspaceManifest>("open_workspace", { path });
```

Return types must implement `serde::Serialize` (Rust → TS). Arguments must implement `serde::Deserialize`. Mirror the shape in `@lore/shared` as TypeScript types.

## Tauri Events — Pattern

Use events for **Rust-initiated push** (e.g., file watcher notifying the frontend of external changes).

**Rust → Frontend:**

```rust
// Emit from anywhere that has an AppHandle
app_handle.emit("file:changed", FileChangedPayload { path, kind })?;
```

**Frontend listener:**

```typescript
import { listen } from "@tauri-apps/api/event";

const unlisten = await listen<FileChangedPayload>("file:changed", (event) => {
  store.handleExternalChange(event.payload);
});
// Call unlisten() on component unmount
```

## State Management (Rust Side)

Shared mutable state across command handlers uses `tauri::State<T>` backed by `Arc<Mutex<T>>` or `Arc<RwLock<T>>`.

```rust
// state.rs
pub struct AppState {
    pub db: Arc<Mutex<rusqlite::Connection>>,
    pub watcher_tx: Arc<Mutex<Option<Sender<WatcherCommand>>>>,
}
```

Register in `lib.rs`:

```rust
.manage(AppState {
    db: Arc::new(Mutex::new(open_db()?)),
    watcher_tx: Arc::new(Mutex::new(None)),
})
```

## Permissions & Capabilities

Tauri v2 requires explicit capability declarations for any filesystem, shell, or dialog access. Files live in `src-tauri/capabilities/`.

```json
{
  "identifier": "workspace-access",
  "description": "Read/write workspace directories",
  "permissions": ["fs:read-all", "fs:write-all", "dialog:open", "dialog:save"]
}
```

If a command fails silently in production but works in dev, check capabilities first.

## Mocking Tauri APIs in Frontend Tests

Tauri APIs (`invoke`, `listen`, etc.) are not available in the Vitest environment. Mock them:

```typescript
// src/__mocks__/@tauri-apps/api/core.ts
import { vi } from "vite-plus/test";

export const invoke = vi.fn();
```

```typescript
// In a test file
import { invoke } from "@tauri-apps/api/core";
import { vi, expect, test } from "vite-plus/test";

vi.mock("@tauri-apps/api/core");

test("open_workspace returns manifest", async () => {
  vi.mocked(invoke).mockResolvedValueOnce({ name: "Test", version: 1 });
  // ... test component or composable
});
```

## SQLite Index

The SQLite database (`lore-editor` crate) is a **disposable cache** — never the source of truth. The filesystem is authoritative. The DB is rebuilt from file `mtime` comparisons on startup. WAL mode is enabled to allow concurrent reads from the file watcher thread and command handlers.

Delete the DB file and restart the app to force a full reindex. Tests that touch the DB should use an in-memory connection (`rusqlite::Connection::open_in_memory()`).

## File Format Conventions

All content is standard `.md` files with YAML frontmatter. The `type` field in frontmatter — not the filename — determines content type.

```markdown
---
type: character
name: Arín el Valiente
status: alive
faction: Free Cities
tags: [human, warrior]
---

## Biography

...
```

Configuration files use TOML (`.lore` workspace manifest, `settings.toml`). JSON is reserved for future post-MVP tools (canvas, timelines).

## Common Pitfalls

- **`tauri-plugin-sql` is explicitly NOT used.** It routes DB access through the frontend and places the DB in the app config dir instead of per-workspace. Use `rusqlite` directly in `lore-editor`.
- **Log emissions during Tauri setup fire before the frontend mounts.** Delay startup events with `tauri::async_runtime::spawn` + `tokio::time::sleep` if the frontend needs to receive them.
- **File watcher events are the single source of change notifications.** Do not add a separate diff layer on top — the watcher already emits granular events.
- **Command handlers must be `async`** if they lock a `Mutex` or call async crate functions, to avoid blocking the main thread.
- **`tauri::command` serialization errors surface as generic JS errors.** Add `#[derive(Debug)]` to error types and use `.map_err(|e| e.to_string())` until a proper error enum is in place.

<!--TAURI END-->

---

<!--LORE DESIGNER START-->

# Lore Designer — Project Context

## What It Is

An offline-first desktop app for writers and worldbuilders. Think of it as the midpoint between Obsidian (too much DIY) and World Anvil (too rigid and online). Users get structured templates and a visual inspector on top of plain `.md` files they own forever.

Platforms: **Linux and Windows** (initial targets).

## Core Philosophy

- **File Over App:** Data lives as Markdown files on disk. The app is a structured layer on top, not a database. Users can open their workspace in any editor (VS Code, Neovim, Obsidian) and everything still works.
- **Offline first.** No accounts, no sync, no subscriptions.
- **Open formats.** Files are readable without Lore Designer in 20 years.

## Tech Stack

| Layer           | Technology                                  |
| --------------- | ------------------------------------------- |
| Frontend        | React 19 + TypeScript                       |
| Styling         | TailwindCSS + shadcn/ui                     |
| State           | Zustand (or TanStack Query for async)       |
| Backend         | Rust via Tauri v2                           |
| Content parsing | `lore-editor` crate (gray-matter, rusqlite) |
| Workspace ops   | `lore-workspaces` crate                     |
| Shared types    | `@lore/shared` (pure TS, no framework deps) |
| Build           | Vite+ (pnpm monorepo)                       |

## Workspace File Structure

```
MyProject/
├── MyProject.lore            # TOML manifest (name, version, created_at)
├── .lore/
│   ├── settings.toml         # User preferences for this workspace
│   └── state.json            # Last open files, scroll positions (disposable)
├── Characters/
│   └── MainHero.md
├── Locations/
│   └── StartingVillage.md
├── Lore/
│   └── AncientProphecy.md
└── Notes/
    └── LooseIdeas.md
```

## MVP Feature Scope

1. **Workspace management** — create, open, recent workspaces.
2. **File browser sidebar** — mirrors the real directory tree.
3. **Rich text editor** — Markdown with a Notion-like `/` command menu. Frontmatter is hidden by default; a "source" toggle reveals it.
4. **Properties inspector panel** — reads frontmatter fields and renders them as a form. Edits write back to the file.
5. **File templates** — default templates for `character`, `location`, `lore`. Users can create custom types by defining a new `type` value and frontmatter schema.
6. **Dynamic Query View** — SQL-like queries across the workspace (`SHOW Characters WHERE faction = "Empire"`). Powered by the SQLite index in `lore-editor`.

Post-MVP (do not implement now): Timelines, Canvas/connection board, plugin system, mobile app.

## Architectural Rules

### The Shared Boundary

- Types, validation schemas, and frontmatter parsing rules → `packages/shared` (`@lore/shared`).
- shadcn source files, generated components, and generated hooks live in `packages/ui`; `apps/desktop` consumes `@lore/ui` and should not keep its own generated shadcn components or hooks.
- `apps/desktop` imports from `@lore/shared`.
- `@lore/shared` never imports from `apps/desktop` or any Tauri/browser API.

### Frontend ↔ Rust Contract

- All file I/O goes through Tauri commands. The frontend never touches the filesystem directly.
- Rust structs that cross the boundary must mirror TypeScript types in `@lore/shared`.
- Unidirectional data flow: **Rust → store → UI components**. Components do not write directly to Rust state.

### Content Type Identification

Content type is determined by the `type` field in frontmatter, not by filename. A file named `Hero.md` is a character because it has `type: character` in its frontmatter. This is intentional for maximum interoperability with external editors.

### Extensibility

Avoid hardcoding logic that would prevent future plugin hooks. Register commands, panel types, and template renderers through a registry pattern rather than hardcoded switches. This keeps the plugin architecture viable without over-engineering it now.

## Anti-Patterns to Avoid

- Do not use `tauri-plugin-sql`.
- Do not encode content type in filenames (no `Hero.character.md`).
- Do not store canonical data in SQLite — it is always a cache.
- Do not write business logic inside Tauri command handlers — they delegate to crates.
- Do not add frontend dependencies without checking if VueUse, native React APIs, or existing libraries already cover it.
- Do not add Rust dependencies without checking if `std` or existing workspace crates cover it.

<!--LORE DESIGNER END-->
