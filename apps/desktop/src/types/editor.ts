export type WorkspaceDatasetId = 'default' | 'hollow' | 'blank';

export type EditorViewMode = 'edit' | 'codex';
export type EditorFontSize = 'small' | 'medium' | 'large';
export type SettingsReturnPath = '/' | '/workspace';

export interface ProjectSummary {
  datasetId: WorkspaceDatasetId;
  entryCount: number;
  id: string;
  lastEdited: string;
  name: string;
  status?: string;
}

export interface EntryRelationship {
  name: string;
  note: string;
}

export interface LoreEntry {
  group: string;
  id: string;
  paragraphs: string[];
  relationships: EntryRelationship[];
  status: string;
  tags: string[];
  title: string;
  type: string;
}

export interface WorkspaceDataset {
  entries: LoreEntry[];
  groupOrder: string[];
}

export interface EditorPreferences {
  focusModeEnabled: boolean;
  fontSize: EditorFontSize;
}
