export type LoreTypeTag = 'character' | 'location' | 'lore' | 'draft' | 'faction' | 'system';

export type WorkspaceNodeKind = 'directory' | 'file';

export interface WorkspaceNode {
  children?: WorkspaceNode[];
  icon?: string;
  id: string;
  kind: WorkspaceNodeKind;
  name: string;
  path: string;
  typeTag?: LoreTypeTag;
}

export type FrontmatterFieldType = 'text' | 'select' | 'number' | 'tags' | 'relations';

export interface FrontmatterField {
  key: string;
  label: string;
  options?: string[];
  type: FrontmatterFieldType;
  value: number | string | string[];
}

export interface InlineSegment {
  text: string;
  tone?: LoreTypeTag | 'muted';
}

export type DocumentContentBlock =
  | {
      id: string;
      segments: InlineSegment[];
      type: 'paragraph';
    }
  | {
      id: string;
      text: string;
      type: 'heading';
    }
  | {
      icon?: string;
      id: string;
      segments: InlineSegment[];
      type: 'callout';
    }
  | {
      id: string;
      items: InlineSegment[][];
      type: 'bullets';
    };

export interface DocumentRecord {
  backlinks: string[];
  content: DocumentContentBlock[];
  cover?: {
    accent: string;
    gradient: string;
  };
  frontmatter: FrontmatterField[];
  icon?: string;
  kind: LoreTypeTag;
  path: string;
  status?: 'draft' | 'saved' | 'unsaved';
  title: string;
}

export interface OpenTab {
  dirty: boolean;
  path: string;
  pinned?: boolean;
  title: string;
}

export interface EditorSelectionState {
  activePath: string;
  expandedPaths: string[];
  searchQuery: string;
}

export interface EditorShellState extends EditorSelectionState {
  documents: Record<string, DocumentRecord>;
  tabs: OpenTab[];
  workspaceName: string;
  workspaceNodes: WorkspaceNode[];
}
