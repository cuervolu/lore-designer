import { create } from 'zustand';
import type { DocumentRecord, EditorShellState, FrontmatterField, OpenTab } from '@/types/editor';
import { mapWorkspaceToTreesData } from './editor-shell-helpers';
import { createInitialEditorShellState } from './mock-data';

interface EditorShellStore extends EditorShellState {
  activeDocument: DocumentRecord;
  treePaths: string[];
  openDocument: (path: string) => void;
  setSearchQuery: (value: string) => void;
  updateFrontmatterField: (
    path: string,
    fieldKey: string,
    value: FrontmatterField['value'],
  ) => void;
}

function upsertTab(tabs: OpenTab[], nextTab: OpenTab) {
  if (tabs.some((tab) => tab.path === nextTab.path)) {
    return tabs;
  }
  return [...tabs, nextTab];
}

const initialState = createInitialEditorShellState();

export const useEditorShellStore = create<EditorShellStore>()((set, get) => ({
  ...initialState,
  activeDocument: initialState.documents[initialState.activePath],
  treePaths: mapWorkspaceToTreesData(initialState.workspaceNodes),

  openDocument(path) {
    const { documents } = get();
    const document = documents[path];
    if (document == null) return;

    set((state) => ({
      activePath: path,
      activeDocument: document,
      tabs: upsertTab(state.tabs, {
        dirty: document.status === 'unsaved',
        path: document.path,
        pinned: false,
        title: document.title,
      }),
    }));
  },

  setSearchQuery(value) {
    set({ searchQuery: value });
  },

  updateFrontmatterField(path, fieldKey, value) {
    const { documents, activePath } = get();
    const document = documents[path];
    if (document == null) return;

    const updatedDocument: DocumentRecord = {
      ...document,
      frontmatter: document.frontmatter.map((field) =>
        field.key === fieldKey ? { ...field, value } : field,
      ),
      status: 'unsaved',
    };

    set((state) => ({
      documents: { ...state.documents, [path]: updatedDocument },
      tabs: state.tabs.map((tab) => (tab.path === path ? { ...tab, dirty: true } : tab)),
      activeDocument: activePath === path ? updatedDocument : state.activeDocument,
    }));
  },
}));
