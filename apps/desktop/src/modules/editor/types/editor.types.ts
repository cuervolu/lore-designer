export interface EditorFile {
  id: string
  name: string
  extension: string
  path: string
  icon: string
  hasUnsavedChanges?: boolean
}

export interface FileTree {
  name: string
  path: string
  is_directory: boolean
  icon?: string
  children: FileTree[]
}

export interface EditorState {
  workspace_path: string
  open_tabs: TabInfo[]
  active_tab?: string
  panels: {
    console_visible: boolean
    inspector_visible: boolean
    explorer_width: number
    inspector_width: number
    console_height: number
  }
  recently_opened: string[]
}

export interface TabInfo {
  id: string
  path: string
  title: string
  icon?: string
  has_unsaved_changes: boolean
}

export interface FileContent {
  type: 'Markdown' | 'Canvas' | 'Character' | 'Image' | 'PlainText'
  data: {
    content?: string
    data?: string
    path?: string
  }
}

export interface IndexingProgress {
  total: number
  processed: number
  current_file?: string
  completed: boolean
}

export interface WorkspaceInfo {
  path: string
  name: string
  icon_path?: string
  last_modified: number
}
