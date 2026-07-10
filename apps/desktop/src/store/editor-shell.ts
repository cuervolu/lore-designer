import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { WORKSPACE_DATASETS } from './mock-data';
import type {
  EditorFontSize,
  EditorViewMode,
  LoreEntry,
  ProjectSummary,
  SettingsReturnPath,
  WorkspaceDataset,
  WorkspaceDatasetId,
} from '@/types/editor';

interface EditorShellStore {
  activeDatasetId: WorkspaceDatasetId;
  focusModeEnabled: boolean;
  focusedParagraphId: string | null;
  fontSize: EditorFontSize;
  metadataOpen: boolean;
  projectName: string;
  selectedEntryId: string | null;
  settingsReturnPath: SettingsReturnPath;
  viewMode: EditorViewMode;
  openBlankProject: (name: string) => void;
  openExampleProject: () => void;
  openProject: (project: ProjectSummary) => void;
  selectEntry: (id: string) => void;
  selectEntryByName: (name: string) => void;
  setFocusModeEnabled: (enabled: boolean) => void;
  setFocusedParagraphId: (id: string | null) => void;
  setFontSize: (size: EditorFontSize) => void;
  setMetadataOpen: (open: boolean) => void;
  setSettingsReturnPath: (path: SettingsReturnPath) => void;
  setViewMode: (mode: EditorViewMode) => void;
}

function firstEntryId(datasetId: WorkspaceDatasetId) {
  return WORKSPACE_DATASETS[datasetId].entries[0]?.id ?? null;
}

export const useEditorShellStore = create<EditorShellStore>()(
  persist(
    (set, get) => ({
      activeDatasetId: 'default',
      focusModeEnabled: true,
      focusedParagraphId: null,
      fontSize: 'medium',
      metadataOpen: true,
      projectName: 'The Shattered Coast',
      selectedEntryId: firstEntryId('default'),
      settingsReturnPath: '/',
      viewMode: 'edit',

      openBlankProject(name) {
        set({
          activeDatasetId: 'blank',
          focusedParagraphId: null,
          metadataOpen: true,
          projectName: name,
          selectedEntryId: null,
          viewMode: 'edit',
        });
      },
      openExampleProject() {
        set({
          activeDatasetId: 'hollow',
          focusedParagraphId: null,
          metadataOpen: true,
          projectName: 'The Hollow',
          selectedEntryId: firstEntryId('hollow'),
          viewMode: 'edit',
        });
      },
      openProject(project) {
        set({
          activeDatasetId: project.datasetId,
          focusedParagraphId: null,
          metadataOpen: true,
          projectName: project.name,
          selectedEntryId: firstEntryId(project.datasetId),
          viewMode: 'edit',
        });
      },
      selectEntry(id) {
        const dataset = WORKSPACE_DATASETS[get().activeDatasetId];
        if (!dataset.entries.some((entry) => entry.id === id)) return;
        set({ selectedEntryId: id, focusedParagraphId: null });
      },
      selectEntryByName(name) {
        const entry = WORKSPACE_DATASETS[get().activeDatasetId].entries.find(
          (candidate) => candidate.title === name,
        );
        if (entry) set({ selectedEntryId: entry.id, focusedParagraphId: null });
      },
      setFocusModeEnabled: (focusModeEnabled) =>
        set({ focusModeEnabled, focusedParagraphId: null }),
      setFocusedParagraphId: (focusedParagraphId) => set({ focusedParagraphId }),
      setFontSize: (fontSize) => set({ fontSize }),
      setMetadataOpen: (metadataOpen) => set({ metadataOpen }),
      setSettingsReturnPath: (settingsReturnPath) => set({ settingsReturnPath }),
      setViewMode: (viewMode) => set({ viewMode, focusedParagraphId: null }),
    }),
    {
      name: 'lore-designer-editor',
      partialize: (state) => ({
        focusModeEnabled: state.focusModeEnabled,
        fontSize: state.fontSize,
        metadataOpen: state.metadataOpen,
      }),
    },
  ),
);

export function getActiveDataset(
  state: Pick<EditorShellStore, 'activeDatasetId'>,
): WorkspaceDataset {
  return WORKSPACE_DATASETS[state.activeDatasetId];
}

export function getActiveEntry(
  state: Pick<EditorShellStore, 'activeDatasetId' | 'selectedEntryId'>,
): LoreEntry | null {
  if (!state.selectedEntryId) return null;
  return (
    WORKSPACE_DATASETS[state.activeDatasetId].entries.find(
      (entry) => entry.id === state.selectedEntryId,
    ) ?? null
  );
}
