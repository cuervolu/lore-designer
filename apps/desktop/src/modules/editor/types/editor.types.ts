export interface EditorFile {
  id: string;
  name: string; // File name without extension (usually)
  extension: string; // e.g., 'md', 'character.md', 'canvas.json'
  path: string; // Relative path from workspace root
  icon: string;
  hasUnsavedChanges?: boolean;
}

export interface FileTree {
  name: string;
  path: string;
  is_directory: boolean;
  icon?: string;
  children: FileTree[];
}

export interface EditorState {
  workspace_path: string;
  open_tabs: TabInfo[];
  active_tab?: string;
  panels: {
    console_visible: boolean;
    inspector_visible: boolean;
    explorer_width: number;
    inspector_width: number;
    console_height: number;
  };
  recently_opened: string[];
}

export interface TabInfo {
  id: string;
  path: string;
  title: string;
  icon?: string;
  has_unsaved_changes: boolean;
}

export type FileContent =
  | { type: 'Markdown'; data: { content: string } }
  | { type: 'Canvas'; data: { data: string } } // JSON string
  | {
  type: 'Character';
  data: { frontmatter: string | null; content: string };
}
  | {
  type: 'Location';
  data: { frontmatter: string | null; content: string };
}
  | { type: 'Lore'; data: { frontmatter: string | null; content: string } }
  | { type: 'Dialogue'; data: { data: string } }
  | { type: 'Image'; data: { path: string } }
  | { type: 'PlainText'; data: { content: string } };


export type SaveFileRequest =
  | { type: 'Text'; data: { content: string } }
  | { type: 'Json'; data: { content: string } }
  | {
  type: 'MarkdownWithFrontmatter';
  data: { frontmatter: string; content: string };
};

export interface IndexingProgress {
  total: number;
  processed: number;
  current_file?: string;
  completed: boolean;
}

export interface WorkspaceInfo {
  path: string;
  name: string;
  icon_path?: string;
  last_modified: number;
}

export enum FileType {
  Markdown = 'Markdown',
  Canvas = 'Canvas',
  Character = 'Character',
  Location = 'Location',
  Image = 'Image',
  Lore = 'Lore',
  Dialogue = 'Dialogue',
  Unknown = 'Unknown',
}
