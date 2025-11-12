import {error as logError} from "tauri-plugin-tracing";
import {ref} from 'vue';
import {useMagicKeys, whenever, useIntervalFn} from '@vueuse/core';
import {toast} from 'vue-sonner';
import {useEditorStore} from '@editor/stores/editor.store';
import {usePreferencesStore} from '@common/stores/preferences.store';


export function useAutoSave() {
  const editorStore = useEditorStore();
  const preferencesStore = usePreferencesStore();
  const isSaving = ref(false);

  const saveNow = async () => {
    const activeTab = editorStore.activeTab;
    if (!activeTab || !activeTab.hasUnsavedChanges || isSaving.value) return;

    try {
      isSaving.value = true;
      await editorStore.saveFileContent(
        activeTab.path,
        editorStore.activeFileContent,
        editorStore.activeFileFrontmatter
      );
    } catch (error) {
      await logError(`Auto-save failed: ${error}`);
      toast.error('Failed to save file');
    } finally {
      isSaving.value = false;
    }
  };

  const keys = useMagicKeys();
  const ctrlS = keys['ctrl+s'];
  const metaS = keys['meta+s'];

  whenever(() => ctrlS?.value || metaS?.value, () => {
    void saveNow();
  });

  useIntervalFn(() => {
    if (preferencesStore.auto_save && editorStore.activeTab?.hasUnsavedChanges) {
      void saveNow();
    }
  }, () => preferencesStore.auto_save_interval_seconds * 1000);

  return {saveNow, isSaving};
}
