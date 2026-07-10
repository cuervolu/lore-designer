import { beforeEach, expect, test } from 'vite-plus/test';
import { splitMentions } from '@features/document-editor/DocumentEditor';
import { WORKSPACE_DATASETS } from '@/store/mock-data';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';

beforeEach(() => {
  useEditorShellStore.setState({
    activeDatasetId: 'default',
    focusModeEnabled: true,
    focusedParagraphId: null,
    fontSize: 'medium',
    metadataOpen: true,
    projectName: 'The Shattered Coast',
    selectedEntryId: 'iskra-vane',
    settingsReturnPath: '/',
    viewMode: 'edit',
  });
});

test('keeps mention text and targets as distinct segments', () => {
  expect(splitMentions('From [[Thornmere Hold]] to [[The Ember Concord]].')).toEqual([
    { mention: false, text: 'From ' },
    { mention: true, text: 'Thornmere Hold' },
    { mention: false, text: ' to ' },
    { mention: true, text: 'The Ember Concord' },
    { mention: false, text: '.' },
  ]);
});

test('opens the example project and navigates relationships by name', () => {
  useEditorShellStore.getState().openExampleProject();
  expect(useEditorShellStore.getState().projectName).toBe('The Hollow');
  expect(getActiveEntry(useEditorShellStore.getState())?.title).toBe('Welcome');

  useEditorShellStore.getState().selectEntryByName('Saltreach');
  expect(getActiveEntry(useEditorShellStore.getState())?.id).toBe('saltreach');
});

test('opens a newly created project with the real blank dataset', () => {
  useEditorShellStore.getState().openBlankProject('A New World');
  const state = useEditorShellStore.getState();
  expect(state.projectName).toBe('A New World');
  expect(state.selectedEntryId).toBeNull();
  expect(WORKSPACE_DATASETS[state.activeDatasetId].entries).toHaveLength(0);
});

test('clears paragraph focus when focus mode or reading mode changes', () => {
  useEditorShellStore.getState().setFocusedParagraphId('iskra-vane-p0');
  useEditorShellStore.getState().setViewMode('codex');
  expect(useEditorShellStore.getState().focusedParagraphId).toBeNull();

  useEditorShellStore.getState().setViewMode('edit');
  useEditorShellStore.getState().setFocusedParagraphId('iskra-vane-p1');
  useEditorShellStore.getState().setFocusModeEnabled(false);
  expect(useEditorShellStore.getState().focusedParagraphId).toBeNull();
});
