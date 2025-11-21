# Lore Designer Roadmap

This document outlines the strategic development path toward the **First Release (v1.0)** of Lore Designer. The focus is on establishing a stable, scalable architecture for narrative management, file system reliability, and the foundation of an unopinionated, user-trust-based plugin ecosystem.

## Phase 1: Core Architecture & Workspace Stability
**Goal:** Ensure the application can reliably handle large workspaces (>1000 files) across Windows,MacOS and Linux without data corruption or performance degradation.

### File System Integrity
* **Refactor File System Watcher:** Implement a granular event system (created/modified/deleted/renamed) and fix cross-platform path filtering issues, specifically addressing Windows duplicates.
* **Implement .loreignore System:** Integrate the `ignore` crate to handle file exclusion rules with hot-reload capabilities for the matcher.
* **Workspace Management:** Finalize the `.lore` folder structure implementation, ensuring `settings.toml` and the manifest file (`.lore`) correctly persist state.

### Data & Performance
* **Refactor Editor Store:** Decompose the monolithic `editor-store.ts` to improve maintainability and reduce memory overhead.
* **Safe File Operations:** Ensure all file I/O is atomic to prevent data loss during saves or auto-saves.

## Phase 2: User Interface & Navigation
**Goal:** Provide a seamless, low-friction experience for navigating complex narrative structures.

### File Explorer Redesign
* **Hierarchical Tree Component:** Develop `FileTreeNode.vue` to support custom sorting and visual hierarchy.
* **Virtualization:** Implement index updates using incremental refreshes rather than full reloads to support large file counts efficiently.
* **Drag and Drop:** Implement full support for moving files within the explorer and dragging images directly into Markdown documents.
* **Dual Representation:** Support switching between the hierarchical tree view and a flat list view for efficient searching.

### Dynamic Query View
* **Query Engine:** Implement the backend logic to filter and aggregate content based on frontmatter tags and metadata.
* **Smart Lists:** Create UI views that automatically populate lists (e.g., "All Characters from Location X") based on current file attributes.

## Phase 3: Creative Engine & Content
**Goal:** Enable the creation and management of narrative elements (Characters, Locations, Stories) using a flexible schema system.

### Template System
* **Schema Definition:** Implement the system for defining `.character.md`, `.location.md`, and `.story.md` structures.
* **Custom Templates:** Enable user-defined custom templates to allow writers to extend the default schemas with their own fields.
* **Form Generation:** Dynamically generate editing forms based on the selected template schema (TOML/YAML frontmatter config).

### Markdown Editor Enhancements
* **Visual Feedback:** Improve indicators for long operations and auto-save status.
* **Media Management:** Finalize the image upload pipeline in forms and handle "orphaned" images cleanup.

## Phase 4: Visualization & Connections
**Goal:** Provide visual tools to track relationships between narrative entities.

### Connection Board
* **Relationship Mapping:** Implement the logic to parse links between files (e.g., `[[Character Name]]` inside a Location file).
* **Graph Visualization:** Render a node-based view (`.canvas.json`) representing the connections between entities.
* **Canvas Interaction:** Allow users to manually organize nodes and save the visual layout.

## Phase 5: Plugin System Core (Trust-Based Architecture)
**Goal:** Implement a lightweight, un-sandboxed plugin system that prioritizes performance and developer experience, shifting security responsibility to user consent.

### Backend (Rust)
* **Extension Crate:** Create the `lore-extensions` crate to encapsulate plugin logic, registry, and health tracking.
* **Protocol Handler:** Implement the `plugin://` URI scheme handler to serve plugin assets securely from the file system, preventing path traversal.
* **Manifest Parsing:** Build parsers for `plugin.toml` (identity/presentation) and `package.json` (dependencies/compatibility).
* **Storage & Health:** Implement isolated storage per plugin and a health tracking system to auto-disable crashing plugins.

### Frontend (TypeScript)
* **Plugin Loader:** Implement the logic to dynamically import ES modules via the custom protocol.
* **API Injection:** Create the `createAPI` factory to inject the `LoreDesignerAPI` (Workspace, Editor, UI, Storage, Events) into the plugin context.
* **Extension Store:** Implement a Pinia store (`extensions.store.ts`) to manage UI registrations (Sidebar Panels, Status Bar Items, Commands).

## Future Backlog (Post-v1.0)
These features are documented but prioritized for subsequent releases.

* **External Integrations:** Discord Rich Presence.
* **Advanced Export:** Styling and exporting content to PDF.
* **Cross-document Tagging:** A unified tagging system across different file types.
* **Bulk Operations:** Mass renaming and image path updates.
* **Plugin Marketplace:** Centralized registry for one-click installation and updates.