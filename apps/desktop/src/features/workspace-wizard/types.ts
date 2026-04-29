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
