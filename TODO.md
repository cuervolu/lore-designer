# Lore Designer TODO

## Backlog (Ideas / Unprioritized)
- [Feature] User-defined custom templates
- [Feature] Discord Rich Presence (via [tauri-plugin-drpc](https://github.com/smokingplaya/tauri-plugin-drpc) or custom)
- [Refactor] Improve custom `tauri-plugin-tracing`
- [Feature] Cross-document tagging system
- [Feature] Plugin/extension system
- [Feature] Export to PDF with styling
- [Feature] Complete translation system with Crowdin
- [Feature] File Watcher for real-time external changes detection (notify crate)
- [Feature] Automatic image reference updates on file watcher events
- [Feature] Smart image candidate search by hash for relocated images
- [Feature] Bulk image path update system for moved/renamed images
- [Feature] File Explorer enhancements (Sprint 4+)
  - Drag & drop support for moving files
  - Multi-selection (Ctrl+Click, Shift+Click)
  - Context menu using Tauri built-in
  - Real-time search/filter in explorer
  - Keyboard navigation (arrows, Enter, etc.)

## To Do (Prioritized)
- [CRITICAL] Refactor File System Watcher 
  - Fix cross-platform path filtering (Windows duplicates bug)
- [HIGH] Redesign File Explorer UI 
  - Custom hierarchical tree component (FileTreeNode.vue)
  - Optimize index updates (incremental instead of full refresh)
  - Dual representation: tree + flat list for search
  - Client-side filtering as safety net
- [MEDIUM] .loreignore system 
  - Create .loreignore with sensible defaults
  - Tauri commands for reading/updating ignore rules
  - Hot reload of ignore matcher on .loreignore changes
  - Optional: UI editor for ignore rules in Settings
- [Feature] Orphaned images cleanup with user confirmation
- [Feature] Implement markdown image drag-and-drop support
- [Enhancement] Visual feedback improvements for long operations
- [Feature] Add "Trash" view (`.lore/trash`)
- [Refactor] Refactor `editor-store.ts` (too large)
- [Docs] Write documentation for the `.lore` format
- [Enhancement] Improve File Explorer view (drag and drop, multi-select, context menu)
- [Feature] Implement autosave indicator in the UI
- [Feature] Add keyboard shortcuts for common actions

## In Progress
- [Feature] Upload Character Image in form view
  - [Testing] Test image upload, existing image load, and missing image handling
- [Bug] Auto-save doesn't work consistently
- [Feature] Dynamic character forms configuration (includes custom fields in `.lore/character_form_config.toml`)

## Done
- [CRITICAL] Refactor File System Watcher  - 2025-11-17
  - Integrate `ignore` crate with .loreignore support
  - Implement granular file system events (created/modified/deleted/renamed)
  - Improve debouncing (500ms + event coalescence)
- [Bug] Cannot scroll in the log console - 2025-11-16
- [Feature] Add vue-virtual-scroller to the log console - 2025-11-16
- [Feature] Upload Character Image in form view - 2025-11-16
  - [Backend] Create ImageRegistry with Mutex for thread-safe operations
  - [Backend] Implement register_image command with SHA-256 hash calculation
  - [Backend] Implement validate_image_index command for workspace validation
  - [Backend] Create image_references.json structure and persistence
  - [Frontend] Modify useImage.ts to invoke register_image on image copy
  - [Frontend] Add image field to CharacterFormView with upload button
  - [Frontend] Integrate image validation on component mount
  - [Frontend] Create MissingImagePlaceholder component
  - [Testing] Test image upload, existing image load, and missing image handling