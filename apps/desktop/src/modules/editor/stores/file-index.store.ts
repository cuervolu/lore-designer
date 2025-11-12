import { invoke } from '@tauri-apps/api/core';
import { debug, error as logError } from 'tauri-plugin-tracing';
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type {
  FileSearchResult,
  FrontmatterResult,
  EntityType
} from '@editor/types/editor.types';
import { useEditorStore } from './editor.store';

export const useFileIndexStore = defineStore('fileIndex', () => {
  const allFiles = ref<FileSearchResult[]>([]);
  const isIndexing = ref(false);
  const lastRefresh = ref<number>(0);
  const indexError = ref<string | null>(null);

  const frontmatterCache = ref<Map<string, FrontmatterResult>>(new Map());

  const characterFiles = computed(() =>
    allFiles.value.filter(file => file.file_type === 'Character')
  );

  const locationFiles = computed(() =>
    allFiles.value.filter(file => file.file_type === 'Location')
  );

  const loreFiles = computed(() =>
    allFiles.value.filter(file => file.file_type === 'Lore')
  );

  const markdownFiles = computed(() =>
    allFiles.value.filter(file => file.file_type === 'Markdown')
  );

  const canvasFiles = computed(() =>
    allFiles.value.filter(file => file.file_type === 'Canvas')
  );

  // Actions

  /**
   * Refresh the complete file index from backend
   */
  async function refreshIndex(): Promise<void> {
    const editorStore = useEditorStore();

    if (!editorStore.currentWorkspace) {
      await logError('Cannot refresh file index: No workspace open');
      return;
    }

    const workspacePath = editorStore.currentWorkspace.path;

    try {
      isIndexing.value = true;
      indexError.value = null;
      await debug(`Refreshing file index for workspace: ${workspacePath}`);

      const files = await invoke<FileSearchResult[]>('get_all_workspace_files', {
        workspacePath
      });

      allFiles.value = files;
      lastRefresh.value = Date.now();

      // Clear frontmatter cache when refreshing
      frontmatterCache.value.clear();

      await debug(`File index refreshed: ${files.length} files loaded`);
    } catch (error) {
      const errorMsg = `Failed to refresh file index: ${error}`;
      await logError(errorMsg);
      indexError.value = errorMsg;
      throw error;
    } finally {
      isIndexing.value = false;
    }
  }

  /**
   * Search files by type with optional query filter
   */
  async function searchFilesByType(
    type: EntityType,
    query?: string
  ): Promise<FileSearchResult[]> {
    const editorStore = useEditorStore();

    if (!editorStore.currentWorkspace) {
      await logError('Cannot search files: No workspace open');
      return [];
    }

    const workspacePath = editorStore.currentWorkspace.path;

    try {
      await debug(`Searching files: type=${type}, query=${query || 'none'}`);

      const results = await invoke<FileSearchResult[]>('search_files_by_type', {
        workspacePath,
        fileType: type,
        query: query || null
      });

      await debug(`Search results: ${results.length} files found`);
      return results;
    } catch (error) {
      await logError(`Failed to search files by type ${type}: ${error}`);
      return [];
    }
  }

  /**
   * Get frontmatter for a specific file (with caching)
   */
  async function getFileFrontmatter(filePath: string): Promise<FrontmatterResult | null> {
    const editorStore = useEditorStore();

    if (!editorStore.currentWorkspace) {
      return null;
    }

    // Check cache first
    if (frontmatterCache.value.has(filePath)) {
      return frontmatterCache.value.get(filePath)!;
    }

    const workspacePath = editorStore.currentWorkspace.path;

    try {
      const result = await invoke<FrontmatterResult>('get_file_frontmatter', {
        workspacePath,
        filePath
      });

      // Cache the result
      frontmatterCache.value.set(filePath, result);
      return result;
    } catch (error) {
      await logError(`Failed to get frontmatter for ${filePath}: ${error}`);
      return null;
    }
  }

  /**
   * Filter files from current index by query (local filtering)
   */
  function filterFiles(query: string, fileType?: EntityType): FileSearchResult[] {
    let files = fileType ? getFilesByType(fileType) : allFiles.value;

    if (!query.trim()) {
      return files;
    }

    const queryLower = query.toLowerCase();
    return files.filter(file =>
      file.name.toLowerCase().includes(queryLower) ||
      file.path.toLowerCase().includes(queryLower)
    );
  }

  /**
   * Get files by type from current index (local)
   */
  function getFilesByType(type: EntityType): FileSearchResult[] {
    switch (type) {
      case 'character': return characterFiles.value;
      case 'location': return locationFiles.value;
      case 'lore': return loreFiles.value;
      case 'markdown': return markdownFiles.value;
      case 'canvas': return canvasFiles.value;
      default: return [];
    }
  }

  /**
   * Find a file by path in current index
   */
  function findFileByPath(path: string): FileSearchResult | null {
    return allFiles.value.find(file => file.path === path) || null;
  }

  /**
   * Clear the index (called when workspace closes)
   */
  function clearIndex(): void {
    allFiles.value = [];
    frontmatterCache.value.clear();
    lastRefresh.value = 0;
    indexError.value = null;
    isIndexing.value = false;
  }

  /**
   * Handle file system changes (called by EditorStore)
   */
  async function handleFileSystemChange(): Promise<void> {
    // Debounce rapid file changes
    const now = Date.now();
    if (now - lastRefresh.value < 1000) {
      return; // Skip if refreshed less than 1 second ago
    }

    await debug('File system change detected, refreshing file index');
    await refreshIndex();
  }

  return {
    // State
    allFiles,
    isIndexing,
    lastRefresh,
    indexError,

    // Computed
    characterFiles,
    locationFiles,
    loreFiles,
    markdownFiles,
    canvasFiles,

    // Actions
    refreshIndex,
    searchFilesByType,
    getFileFrontmatter,
    filterFiles,
    getFilesByType,
    findFileByPath,
    clearIndex,
    handleFileSystemChange,
  };
});
