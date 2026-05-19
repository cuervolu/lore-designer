export interface WorkspaceCover {
  from: string;
  glyph: string;
  to: string;
}

export interface WorkspaceCounts {
  characters: number;
  locations: number;
  lore: number;
}

export interface Workspace {
  counts: WorkspaceCounts;
  cover: WorkspaceCover;
  exists: boolean;
  id: string;
  lastOpened: string;
  name: string;
  path: string;
}

export interface WorkspaceTemplateSummary {
  description: string;
  displayName: string;
  id: string;
  isBuiltin: boolean;
  sortOrder: number;
  supportsCreation: boolean;
}

export interface CreateWorkspaceRequest {
  appVersion: string;
  name: string;
  path: string;
  templateId: string;
}

export interface WorkspaceVersion {
  major: number;
}

export interface CreateWorkspaceResult {
  createdAt: string;
  manifestPath: string;
  name: string;
  rootPath: string;
  templateId: string;
  workspaceVersion: WorkspaceVersion;
}
