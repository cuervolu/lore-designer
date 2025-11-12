import {lstat} from '@tauri-apps/plugin-fs';
import {debug, error as logError, warn} from 'tauri-plugin-tracing';
import {computed, nextTick, ref, type Ref, watch} from 'vue';
import {parse as yamlParse, stringify as yamlStringify} from 'yaml';
import {toast} from 'vue-sonner';
import type {EditorFile} from '@editor/types/editor.types';
import type {useEditorStore} from '@editor/stores/editor.store';

type EditorStoreInstance = ReturnType<typeof useEditorStore>;

export type FileProperty = { name: string; value: string };
export type FrontmatterData = Record<string, unknown>;
export type SectionVisibility = Record<string, boolean>;

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${Number.parseFloat((bytes / 1024 ** i).toFixed(1))} ${units[i]}`;
}

export function useInspector(
  fileRef: Ref<EditorFile | null | undefined>,
  editorStore: EditorStoreInstance
) {
  const searchQuery = ref('');
  const isLoading = ref(false);
  const fileProperties = ref<FileProperty[]>([]);
  const frontmatterData = ref<FrontmatterData | null>(null);
  const frontmatterParseError = ref<string | null>(null);
  const isFrontmatterDirty = ref(false);
  const isFrontmatterSupportedType = ref(false);
  const openSections = ref<SectionVisibility>({
    'File Info': true,
    Frontmatter: true,
  });

  const hasFrontmatter = computed(() => {
    return (
      frontmatterData.value !== null &&
      Object.keys(frontmatterData.value).length > 0
    );
  });

  const filteredFileProperties = computed(() => {
    if (!searchQuery.value) return fileProperties.value;
    const query = searchQuery.value.toLowerCase();
    return fileProperties.value.filter(
      (prop) =>
        prop.name.toLowerCase().includes(query) ||
        prop.value.toLowerCase().includes(query)
    );
  });

  const filteredFrontmatterEntries = computed(() => {
    if (!frontmatterData.value || !searchQuery.value) {
      return Object.entries(frontmatterData.value || {});
    }
    const query = searchQuery.value.toLowerCase();
    return Object.entries(frontmatterData.value).filter(([key]) =>
      key.toLowerCase().includes(query)
    );
  });

  function resetInspectorState() {
    fileProperties.value = [];
    frontmatterData.value = null;
    frontmatterParseError.value = null;
    isFrontmatterDirty.value = false;
    isFrontmatterSupportedType.value = false;
  }

  async function loadBasicFileProperties(file: EditorFile) {
    if (!editorStore.currentWorkspace) return;
    const fullPath = `${editorStore.currentWorkspace.path}/${file.path}`;
    try {
      const metadata = await lstat(fullPath);
      fileProperties.value = [
        { name: 'Filename', value: file.name },
        { name: 'Path', value: file.path },
        { name: 'Extension', value: file.extension },
        { name: 'Size', value: formatFileSize(metadata.size) },
        {
          name: 'Modified',
          value: metadata.mtime?.toLocaleString() ?? 'N/A',
        },
        {
          name: 'Created',
          value: metadata.birthtime?.toLocaleString() ?? 'N/A',
        },
      ];
    } catch (err: unknown) {
      await logError(
        `Inspector Compos.: Failed to get file metadata for ${fullPath}: ${err}`
      );
      fileProperties.value = [
        { name: 'Filename', value: file.name },
        { name: 'Path', value: file.path },
        { name: 'Error', value: 'Could not load file metadata' },
      ];
    }
  }

  async function parseFrontmatter(yamlString: string) {
    await debug(
      `Inspector Compos.: Parsing YAML. Length: ${yamlString?.length}`
    );
    try {
      const trimmedYaml = yamlString.trim();
      if (trimmedYaml === '') {
        await debug(
          'Inspector Compos.: YAML string empty after trim, setting {}'
        );
        frontmatterData.value = {};
        frontmatterParseError.value = null;
        return;
      }

      const parsedData = yamlParse(trimmedYaml);

      if (typeof parsedData === 'object' && parsedData !== null) {
        // Assign first
        frontmatterData.value = parsedData as FrontmatterData; // Cast if needed, yamlParse returns any

        // *** FIX: Add null check before Object.keys ***
        if (frontmatterData.value) {
          await debug(
            `Inspector Compos.: YAML Parse OK. Keys: ${Object.keys(
              frontmatterData.value
            ).join(', ')}`
          );
        } else {
          await debug(
            'Inspector Compos.: YAML Parse OK, but frontmatterData is nullish? Should not happen here.'
          );
        }

      } else {
        await warn(
          `Inspector Compos.: YAML parsed to non-object (${typeof parsedData}). Setting {}`
        );
        frontmatterData.value = {};
      }
      frontmatterParseError.value = null;
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : String(e);
      await logError(`Inspector Compos.: YAML Parse ERROR: ${message}`);
      frontmatterParseError.value = `Invalid YAML: ${message}`;
      frontmatterData.value = null;
    } finally {
      await nextTick();
    }
  }

  async function loadInspectorData(file: EditorFile | null) {
    await debug(
      `Inspector Compos.: loadInspectorData START for file: ${
        file?.path ?? 'null'
      }`
    );

    if (!file || !editorStore.currentWorkspace) {
      await debug('Inspector Compos.: No file/workspace, resetting.');
      resetInspectorState();
      return;
    }

    isLoading.value = true;
    resetInspectorState();

    try {
      await loadBasicFileProperties(file);

      const fileContent = await editorStore.getFileContent(file.path);
      await debug(
        `Inspector Compos.: Store fetched content. Type: ${
          fileContent?.type ?? 'null'
        }`
      );

      if (fileContent) {
        isFrontmatterSupportedType.value = [
          'Character',
          'Location',
          'Lore',
        ].includes(fileContent.type);

        if (isFrontmatterSupportedType.value) {
          await debug('Inspector Compos.: File type supports frontmatter.');
          const rawFrontmatter = editorStore.activeFileFrontmatter;

          await debug(
            `Inspector Compos.: Raw frontmatter from store. Length: ${
              rawFrontmatter?.length ?? 'null'
            }`
          );

          if (rawFrontmatter && rawFrontmatter.trim() !== '') {
            await parseFrontmatter(rawFrontmatter);
          } else {
            await debug(
              'Inspector Compos.: No raw frontmatter found, setting {}.'
            );
            frontmatterData.value = {};
            frontmatterParseError.value = null;
          }
        } else {
          await debug(
            `Inspector Compos.: File type ${fileContent.type} does not use frontmatter.`
          );
          frontmatterData.value = null;
        }
      } else {
        await debug('Inspector Compos.: No file content received.');
        frontmatterData.value = null;
        isFrontmatterSupportedType.value = false;
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err);
      await logError(
        `Inspector Compos.: ERROR in loadInspectorData for ${file.path}: ${message}`
      );
      frontmatterParseError.value = `Failed to load data: ${message}`;
      frontmatterData.value = null;
      isFrontmatterSupportedType.value = false;
    } finally {
      isLoading.value = false;
      isFrontmatterDirty.value = false;
      await nextTick();
      await debug('Inspector Compos.: loadInspectorData FINISHED.');
    }
  }

  watch(
    fileRef,
    async (newFile) => {
      if (newFile !== undefined) {
        await loadInspectorData(newFile);
        await nextTick();
      } else {
        await debug('Inspector Compos.: Watcher received undefined file, resetting.');
        resetInspectorState();
      }
    },
    { immediate: true, deep: false }
  );

  // Watch for local changes to mark dirty
  watch(
    frontmatterData,
    (newValue, oldValue) => {
      if (oldValue !== undefined && !isLoading.value) {
      }
    },
    { deep: true }
  );


  function handleInputChange(key: string, value: unknown) {
    if (!frontmatterData.value) return;
    frontmatterData.value[key] = value;
    if (!isLoading.value) {
      isFrontmatterDirty.value = true;
    }
  }

  async function applyFrontmatterChanges() {
    if (!isFrontmatterDirty.value || frontmatterData.value === null) {
      await debug("Inspector Compos.: Apply changes called, but not dirty or data is null.");
      return;
    }
    // Now accesses the store instance correctly
    if (!editorStore.activeTab) {
      await logError("Inspector Compos.: Cannot apply changes, no active tab.");
      toast.error("Cannot apply changes", { description: "No file is active." });
      return;
    }

    await debug("Inspector Compos.: Applying frontmatter changes to store...");
    try {
      editorStore.activeFileFrontmatter = yamlStringify(frontmatterData.value);
      isFrontmatterDirty.value = false;
      await editorStore.markTabAsChanged();
      await editorStore.saveEditorState();
      toast.success("Frontmatter changes applied", { description: "Remember to save the file."});
      await debug("Inspector Compos.: Store updated, marked tab dirty.");
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : String(e);
      await logError(`Inspector Compos.: YAML Stringify Error: ${message}`);
      toast.error("Error applying frontmatter", {
        description: "Could not serialize data to YAML.",
      });
    }
  }

  function discardFrontmatterChanges() {
    if (!isFrontmatterDirty.value) {
      return;
    }
    const originalFrontmatter = editorStore.activeFileFrontmatter;
    if (originalFrontmatter !== null) {
      parseFrontmatter(originalFrontmatter);
    } else {
      frontmatterData.value = isFrontmatterSupportedType.value ? {} : null;
    }
    isFrontmatterDirty.value = false;
    toast.info("Frontmatter changes discarded.");
  }


  function toggleSection(sectionName: string) {
    openSections.value[sectionName] = !openSections.value[sectionName];
  }

  return {
    searchQuery,
    isLoading,
    fileProperties,
    frontmatterData,
    frontmatterParseError,
    isFrontmatterDirty,
    isFrontmatterSupportedType,
    openSections,
    hasFrontmatter,
    filteredFileProperties,
    filteredFrontmatterEntries,
    handleInputChange,
    applyFrontmatterChanges,
    discardFrontmatterChanges,
    toggleSection,
    formatFileSize,
  };
}
