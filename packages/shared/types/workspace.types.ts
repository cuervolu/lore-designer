export interface RecentWorkspace {
  name: string;
  path: string;
  last_accessed: number;
}

export interface WorkspacePreview {
  folders: FolderInfo[];
}

export interface FolderInfo {
  name: string;
  file_count: number;
}
