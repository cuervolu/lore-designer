import {defineStore} from 'pinia'
import {ref, computed} from 'vue'
import {invoke} from '@tauri-apps/api/core'
import {debug, error as logError, warn} from '@tauri-apps/plugin-log'
import {toast} from 'vue-sonner'
import {listen, type UnlistenFn} from '@tauri-apps/api/event'
import {usePreferencesStore} from '@common/stores/preferences.store'
import type {
  WorkspaceInfo,
  FileTree,
  EditorFile,
  FileContent,
  EditorState,
  TabInfo,
  IndexingProgress,
  SaveFileRequest
} from "@editor/types/editor.types";

export const useEditorStore = defineStore('editor', () => {
  const preferencesStore = usePreferencesStore()

  const currentWorkspace = ref<WorkspaceInfo | null>(null)
  const fileTree = ref<FileTree[]>([])
  const openTabs = ref<EditorFile[]>([])
  const activeTabId = ref<string | null>(null)
  const showConsole = ref(false)
  const showInspector = ref(true)
  const indexingProgress = ref<IndexingProgress | null>(null)
  const isLoading = ref(false)
  const editorError = ref<string | null>(null)
  const fileSystemUnlistener = ref<UnlistenFn | null>(null)

  // Temporary state for the content being edited in EditorContent
  const activeFileContent = ref<string>('');
  // Temporary state for the frontmatter YAML string being edited in InspectorPanel
  const activeFileFrontmatter = ref<string | null>(null);

  const activeTab = computed(() => {
    return openTabs.value.find(tab => tab.id === activeTabId.value) || null
  })

  // Actions

  async function openWorkspace(workspacePath: string) {
    try {
      isLoading.value = true;
      editorError.value = null;
      await debug(`Attempting to open workspace: ${workspacePath}`);

      const workspace = await invoke<WorkspaceInfo>('open_workspace_in_editor', {
        workspacePath,
      });
      await debug(`Workspace opened successfully: ${workspace.name}`);

      currentWorkspace.value = workspace;

      await loadFileTree();
      await startIndexingProgressPoll();
      await loadEditorState(); // Load saved tabs, etc.
      await preferencesStore.updateLastProject(workspacePath);
      await setupFileSystemListener(); // Start watching for FS changes

      return workspace;
    } catch (err) {
      const errorMessage = `Failed to open workspace at ${workspacePath}: ${err}`;
      await logError(errorMessage);
      editorError.value = errorMessage;
      toast.error('Failed to open workspace', {
        description: String(err),
      });
      currentWorkspace.value = null;
      throw err;
    } finally {
      isLoading.value = false;
    }
  }

  async function setupFileSystemListener() {
    if (fileSystemUnlistener.value) {
      await debug('Stopping existing file system listener.');
      fileSystemUnlistener.value(); // Call the unlisten function
      fileSystemUnlistener.value = null;
    }

    if (!currentWorkspace.value) {
      await warn('No current workspace, skipping file system listener setup.');
      return;
    }

    const workspacePath = currentWorkspace.value.path;
    await debug(`Setting up file system listener for: ${workspacePath}`);

    try {
      fileSystemUnlistener.value = await listen(
        'file-system-changed',
        async (event) => {
          await debug(`File system change detected: ${JSON.stringify(event.payload)}`);
          const eventData = event.payload as {
            workspace_path: string;
            timestamp: number;
          };

          if (currentWorkspace.value && eventData.workspace_path === currentWorkspace.value.path) {
            await debug(`Refreshing file tree for ${currentWorkspace.value.path} due to filesystem change.`);
            await loadFileTree(); // Refresh the file tree in the UI
          } else {
            await warn(`Ignoring file system event for different workspace: ${eventData.workspace_path}`);
          }
        }
      );
      await debug(`File system listener successfully set up for: ${workspacePath}`);
    } catch (err) {
      await logError(`Failed to set up file system listener for ${workspacePath}: ${err}`);
      fileSystemUnlistener.value = null;
    }
  }

  async function closeWorkspace() {
    const path = currentWorkspace.value?.path;
    if (!path) {
      await warn('Attempted to close workspace, but none was open.');
      return;
    }

    await warn(`Closing workspace: ${path}`);

    if (fileSystemUnlistener.value) {
      await debug(`Stopping file system listener for ${path}.`);
      fileSystemUnlistener.value();
      fileSystemUnlistener.value = null;
    } else {
      await warn(`No active file system listener found for ${path} during close.`);
    }
    try {
      await debug(`Invoking backend to stop watching workspace: ${path}`);
      await invoke('stop_watching_workspace', {workspacePath: path});
      await debug(`Backend stopped watching workspace: ${path}`);
    } catch (err) {
      await logError(`Failed to stop backend file watcher for ${path}: ${err}`);
    }

    currentWorkspace.value = null;
    fileTree.value = [];
    openTabs.value = [];
    activeTabId.value = null;
    indexingProgress.value = null; // Reset indexing progress
    editorError.value = null; // Clear any previous errors
    isLoading.value = false; // Ensure loading state is reset

    await debug(`Workspace ${path} closed and local state cleared.`);
  }

  async function loadFileTree() {
    if (!currentWorkspace.value) {
      await warn('Cannot load file tree: No workspace is open.');
      fileTree.value = [];
      return [];
    }
    const workspacePath = currentWorkspace.value.path;
    await debug(`Loading file tree for: ${workspacePath}`);

    try {
      const tree = await invoke<FileTree[]>('get_workspace_file_tree', {
        workspacePath,
      });
      fileTree.value = tree;
      await debug(`File tree loaded successfully for ${workspacePath}. Items: ${tree.length}`);
      return tree;
    } catch (err) {
      const errorMessage = `Failed to load file tree for ${workspacePath}: ${err}`;
      await logError(errorMessage);
      if (!indexingProgress.value || indexingProgress.value.completed) {
        toast.error('Failed to load file tree', {
          description: 'Please try refreshing manually.',
        });
      }
      fileTree.value = [];
      return [];
    }
  }

  async function refreshFileTree() {
    if (!currentWorkspace.value) {
      await warn('Cannot refresh file tree: No workspace is open.');
      return [];
    }
    const workspacePath = currentWorkspace.value.path;
    await debug(`Refreshing file tree for: ${workspacePath}`);
    toast.info('Refreshing file tree...');

    try {
      const tree = await invoke<FileTree[]>('refresh_file_tree', {
        workspacePath,
      });
      fileTree.value = tree;
      toast.success('File tree refreshed');
      await debug(`File tree refreshed successfully for ${workspacePath}. Items: ${tree.length}`);
      startIndexingProgressPoll();
      return tree;
    } catch (err) {
      const errorMessage = `Failed to refresh file tree for ${workspacePath}: ${err}`;
      await logError(errorMessage);
      toast.error('Failed to refresh file tree', {
        description: String(err),
      });
      return fileTree.value; // Return the potentially stale tree
    }
  }

  async function startIndexingProgressPoll() {
    if (!currentWorkspace.value) {
      await debug('Skipping indexing poll: No workspace open.');
      return () => {
      }; // Return a no-op cleanup function
    }

    const workspacePath = currentWorkspace.value.path;
    let pollIntervalId: number | null = null;
    let isPolling = false; // Prevent multiple concurrent polls

    const poll = async () => {
      if (isPolling || !currentWorkspace.value || currentWorkspace.value.path !== workspacePath) {
        if (pollIntervalId) clearInterval(pollIntervalId);
        return;
      }
      isPolling = true;
      // await debug(`Polling indexing progress for: ${workspacePath}`);

      try {
        const progress = await invoke<IndexingProgress | null>('get_indexing_progress', {
          workspacePath,
        });

        // Only update if the workspace hasn't changed during the async call
        if (currentWorkspace.value && currentWorkspace.value.path === workspacePath) {
          indexingProgress.value = progress;
          // await debug(`Indexing progress: ${JSON.stringify(progress)}`);

          if (progress?.completed) {
            await debug(`Indexing completed for ${workspacePath}. Stopping poll.`);
            if (pollIntervalId) clearInterval(pollIntervalId);
            pollIntervalId = null;
          }
        } else {
          await debug(`Workspace changed during poll for ${workspacePath}. Stopping poll.`);
          if (pollIntervalId) clearInterval(pollIntervalId);
          pollIntervalId = null;
        }
      } catch (err) {
        await logError(`Error polling indexing progress for ${workspacePath}: ${err}`);
        if (pollIntervalId) clearInterval(pollIntervalId);
        pollIntervalId = null;
        indexingProgress.value = null;
      } finally {
        isPolling = false;
      }
    };

    // Clear any existing interval before starting a new one
    // Note: At the moment only one poll should be active at a time.
    // If multiple workspaces could be polled, this needs adjustment.
    // Since we only have one active workspace, this is fine.
    // We need a mechanism to clear the *specific* interval for the *previous* workspace
    // when a new one is opened. `closeWorkspace` handles this.

    await poll();
    pollIntervalId = setInterval(poll, 2000) as unknown as number;

    return () => {
      if (pollIntervalId) {
        debug(`Cleaning up indexing poll interval for ${workspacePath}.`);
        clearInterval(pollIntervalId);
        pollIntervalId = null;
      }
    };
  }

  async function loadEditorState() {
    if (!currentWorkspace.value) {
      await warn('Cannot load editor state: No workspace open.');
      return null;
    }
    const workspacePath = currentWorkspace.value.path;
    await debug(`Loading editor state for: ${workspacePath}`);

    try {
      const state = await invoke<EditorState>('load_editor_state', {
        workspacePath,
      });
      await debug(`Editor state loaded: ${JSON.stringify(state)}`);

      openTabs.value = state.open_tabs.map((tab) => {
        const parts = tab.path.split(/[\\/]/);
        const fileNameWithExt = parts.pop() || tab.title;
        const extension = fileNameWithExt.includes('.') ? fileNameWithExt.split('.').pop()! : '';
        return {
          id: tab.id,
          name: tab.title, // Use title from backend state first
          path: tab.path,
          extension: extension,
          icon: tab.icon || 'file', // Default icon
          hasUnsavedChanges: tab.has_unsaved_changes || false,
        };
      });

      if (state.active_tab && openTabs.value.some(tab => tab.id === state.active_tab)) {
        activeTabId.value = state.active_tab;
      } else if (openTabs.value.length > 0) {
        activeTabId.value = openTabs.value[0].id;
      } else {
        activeTabId.value = null;
      }
      showConsole.value = state.panels?.console_visible ?? false;
      showInspector.value = state.panels?.inspector_visible ?? true; // Default to true

      await debug(`Restored state: ${openTabs.value.length} tabs, active: ${activeTabId.value}, console: ${showConsole.value}, inspector: ${showInspector.value}`);

      // If no tabs were restored, but there are recently opened files, open the most recent one
      if (openTabs.value.length === 0 && state.recently_opened && state.recently_opened.length > 0) {
        const lastOpened = state.recently_opened[0];
        await debug(`No tabs restored, opening last recently opened file: ${lastOpened}`);
        // Use try-catch as the file might no longer exist
        try {
          await openFile(lastOpened);
        } catch (openErr) {
          await logError(`Failed to auto-open last recently opened file ${lastOpened}: ${openErr}`);
          // Remove the problematic file from recently opened? Maybe too aggressive.
        }
      } else if (openTabs.value.length > 0 && !activeTabId.value) {
        // If tabs were loaded but no active tab set (e.g., invalid saved ID), activate the first one
        activeTabId.value = openTabs.value[0].id;
        await debug(`Setting active tab to the first loaded tab: ${activeTabId.value}`);
      }

      return state;
    } catch (err) {
      const errorMessage = `Failed to load editor state for ${workspacePath}: ${err}`;
      await logError(errorMessage);
      // Don't toast here, could be annoying on startup
      // Reset to defaults on error?
      openTabs.value = [];
      activeTabId.value = null;
      showConsole.value = false;
      showInspector.value = true;
      return null;
    }
  }

  async function saveEditorState() {
    if (!currentWorkspace.value) {
      await warn('Cannot save editor state: No workspace open.');
      return; // Silently return if no workspace
    }
    const workspacePath = currentWorkspace.value.path;
    // await debug(`Saving editor state for: ${workspacePath}`);

    try {
      const recentPaths = openTabs.value.map(tab => tab.path);

      const state: EditorState = {
        workspace_path: workspacePath,
        open_tabs: openTabs.value.map(tab => ({
          id: tab.id,
          path: tab.path,
          title: tab.name, // Save the potentially cleaned name
          icon: tab.icon,
          has_unsaved_changes: tab.hasUnsavedChanges || false,
        })),
        active_tab: activeTabId.value || undefined, // Save undefined if null
        panels: {
          console_visible: showConsole.value,
          inspector_visible: showInspector.value,
          // TODO: Get actual widths/heights if needed later
          explorer_width: 250,
          inspector_width: 300,
          console_height: 200,
        },
        // Limit recently opened? For now, save all open tabs as recent.
        recently_opened: recentPaths,
      };

      await invoke('save_editor_state', {state});
      // await debug(`Editor state saved successfully for ${workspacePath}.`);

    } catch (err) {
      // Log error but don't bother the user with a toast for background saves
      await logError(`Failed to save editor state for ${workspacePath}: ${err}`);
    }
  }

  async function openFile(filePath: string): Promise<EditorFile | null> {

    if (!currentWorkspace.value) {
      await logError('Cannot open file: No workspace open.');
      toast.error('Cannot open file', { description: 'No workspace is currently open.' });
      return null;
    }
    const workspacePath = currentWorkspace.value.path;
    // Create a new variable for the absolute path to avoid reassigning the parameter
    // Use simple string concatenation for now, ensure correct separator if needed,
    // although Tauri backend path handling is usually robust.
    // IMPORTANT: Ensure filePath is *always* relative when passed to this function.
    const absoluteFilePath = `${workspacePath}/${filePath.replace(/^[\\/]/, '')}`; // Ensure no leading slash on relative part

    await debug(`Attempting to open file: ${absoluteFilePath} (relative: ${filePath}) in workspace: ${workspacePath}`);

    const existingTab = openTabs.value.find(tab => tab.path === filePath);
    if (existingTab) {
      await debug(`File ${filePath} is already open. Activating tab ${existingTab.id}.`);
      activeTabId.value = existingTab.id;
      await saveEditorState();
      return existingTab;
    }

    try {
      const tabInfo = await invoke<TabInfo>('open_file_in_editor', {
        workspacePath: workspacePath,
        filePath: absoluteFilePath
      });


      await debug(`Backend opened file, received TabInfo: ${JSON.stringify(tabInfo)}`);

      activeFileContent.value = '';
      activeFileFrontmatter.value = null;

      const parts = tabInfo.path.split(/[\\/]/);
      const fileNameWithExt = parts.pop() || tabInfo.title;

      const extension = fileNameWithExt.includes('.')
        ? (fileNameWithExt.split('.').pop() ?? '')
        : '';

      const name = tabInfo.title;

      const newTab: EditorFile = {
        id: tabInfo.id,
        name: name,
        path: tabInfo.path, // Store the relative path from backend
        extension: extension,
        icon: tabInfo.icon || 'file',
        hasUnsavedChanges: tabInfo.has_unsaved_changes || false,
      };

      openTabs.value.push(newTab);
      activeTabId.value = newTab.id;
      await debug(`Added new tab ${newTab.id} for ${newTab.path} and activated it.`);

      await saveEditorState();

      return newTab;

    } catch (err) {
      const errorMessage = `Failed to open file ${absoluteFilePath}: ${err}`;
      await logError(errorMessage);
      toast.error('Failed to open file', {
        description: `Path: ${filePath}. Error: ${String(err)}`,
      });
      return null;
    }
  }


  async function closeTab(tabId: string) {
    const index = openTabs.value.findIndex(tab => tab.id === tabId);
    if (index === -1) {
      await warn(`Attempted to close non-existent tab ID: ${tabId}`);
      return;
    }

    const closingTab = openTabs.value[index];
    await debug(`Closing tab: ${closingTab.path} (ID: ${tabId})`);

    // TODO: Check for unsaved changes before closing
    if (closingTab.hasUnsavedChanges) {
      const confirmed = window.confirm(`"${closingTab.name}" has unsaved changes. Close anyway?`);
      if (!confirmed) {
        await debug(`Close cancelled for unsaved tab: ${tabId}`);
        return;
      }
      await warn(`Closing tab ${tabId} with unsaved changes.`);
    }

    const wasActive = activeTabId.value === tabId;
    openTabs.value.splice(index, 1);

    if (wasActive) {
      activeFileContent.value = '';
      activeFileFrontmatter.value = null;
      if (openTabs.value.length === 0) {
        activeTabId.value = null;
        await debug('Last tab closed. No active tab.');
      } else {
        const newActiveIndex = Math.max(0, index - 1);
        activeTabId.value = openTabs.value[newActiveIndex].id;
        await debug(`Setting new active tab to: ${activeTabId.value}`);
      }
    }

    await saveEditorState();
  }

  async function getFileContent(filePath: string): Promise<FileContent | null> {
    if (!currentWorkspace.value) {
      await logError('Cannot get file content: No workspace open.');
      toast.error('Cannot get file content', {description: 'No workspace is open.'});
      return null;
    }
    const workspacePath = currentWorkspace.value.path;
    if (filePath.startsWith(workspacePath)) {
      filePath = filePath.substring(workspacePath.length).replace(/^[\\/]/, '');
    }
    await debug(`Getting file content for: ${filePath}`);

    try {
      const content = await invoke<FileContent>('get_file_content', {
        workspacePath,
        filePath,
      });
      await debug(`Received file content type: ${content.type}`);
      activeFileContent.value = '';
      activeFileFrontmatter.value = null;
      switch (content.type) {
        case 'Markdown':
        case 'PlainText':
          activeFileContent.value = content.data.content || '';
          break;
        case 'Character':
        case 'Location':
        case 'Lore':
          activeFileContent.value = content.data.content || '';
          activeFileFrontmatter.value = content.data.frontmatter; // Can be null
          break;
        case 'Canvas':
        case 'Dialogue':
          activeFileContent.value = content.data.data || ''; // Store JSON/Text data
          break;
        case 'Image':
          break;
      }
      await debug('Updated activeFileContent and activeFileFrontmatter');
      return content;
    } catch (err) {
      activeFileContent.value = '';
      activeFileFrontmatter.value = null;
      const errorMessage = `Failed to get content for file ${filePath}: ${err}`;
      await logError(errorMessage);
      toast.error('Failed to load file content', {
        description: String(err),
      });
      return null;
    }
  }

  async function saveFileContent(
    filePath: string,
    content: string,
    frontmatter: string | null // Accept optional frontmatter string
  ): Promise<boolean> {
    if (!currentWorkspace.value) {
      await logError('Cannot save file: No workspace open.');
      toast.error('Cannot save file', {description: 'No workspace is open.'});
      return false;
    }
    const workspacePath = currentWorkspace.value.path;
    if (filePath.startsWith(workspacePath)) {
      filePath = filePath.substring(workspacePath.length).replace(/^[\\/]/, '');
    }
    await debug(`Attempting to save file: ${filePath}`);

    try {
      let request: SaveFileRequest;

      // Determine the correct request type based on frontmatter presence
      if (frontmatter !== null && frontmatter !== undefined) {
        // If frontmatter is provided (even if empty string), use MarkdownWithFrontmatter
        request = {
          type: 'MarkdownWithFrontmatter',
          data: {frontmatter: frontmatter, content: content},
        };
        await debug(`Saving ${filePath} with frontmatter.`);
      } else {
        // Check if the file type suggests JSON (Canvas)
        // TODO: Improve this check - maybe pass file type explicitly?
        const tab = openTabs.value.find(t => t.path === filePath);
        if (tab?.extension?.toLowerCase().endsWith('canvas.json')) {
          request = {type: 'Json', data: {content: content}};
          await debug(`Saving ${filePath} as JSON.`);
        } else {
          // Otherwise, save as plain text
          request = {type: 'Text', data: {content: content}};
          await debug(`Saving ${filePath} as Text.`);
        }
      }

      await invoke('save_file_content', {
        workspacePath,
        filePath,
        content: request,
      });

      const tab = openTabs.value.find((tab) => tab.path === filePath);
      if (tab) {
        tab.hasUnsavedChanges = false;
        await debug(`Marked tab ${tab.id} as saved.`);
      }
      await saveEditorState();

      toast.success(`"${tab?.name || filePath}" saved successfully`);
      return true;
    } catch (err) {
      const errorMessage = `Failed to save file ${filePath}: ${err}`;
      await logError(errorMessage);
      toast.error('Failed to save file', {
        description: String(err),
      });
      return false;
    }
  }

  async function createNewFile(
    parentDir: string,
    fileName: string,
    initialContent = ''
  ): Promise<string | null> {
    if (!currentWorkspace.value) {
      await logError('Cannot create file: No workspace open.');
      toast.error('Cannot create file', {description: 'No workspace is open.'});
      return null;
    }
    const workspacePath = currentWorkspace.value.path;
    if (parentDir.startsWith(workspacePath)) {
      parentDir = parentDir.substring(workspacePath.length).replace(/^[\\/]/, '');
    }
    await debug(`Creating new file "${fileName}" in "${parentDir}"`);

    try {
      const relativePath = await invoke<string>('create_new_file', {
        workspacePath,
        parentDir,
        fileName,
        initialContent,
      });
      await debug(`File created at relative path: ${relativePath}`);

      await loadFileTree(); // Use loadFileTree instead of refresh to avoid re-indexing

      await openFile(relativePath);

      toast.success(`File "${fileName}" created successfully`);
      return relativePath;
    } catch (err) {
      const errorMessage = `Failed to create file "${fileName}": ${err}`;
      await logError(errorMessage);
      toast.error('Failed to create file', {
        description: String(err),
      });
      return null;
    }
  }

  async function createFromTemplate(
    parentDir: string,
    fileName: string,
    fileType: string // Use string matching backend FileType enum names
  ): Promise<string | null> {
    if (!currentWorkspace.value) {
      await logError('Cannot create file from template: No workspace open.');
      toast.error('Cannot create file', {description: 'No workspace is open.'});
      return null;
    }
    const workspacePath = currentWorkspace.value.path;
    if (parentDir.startsWith(workspacePath)) {
      parentDir = parentDir.substring(workspacePath.length).replace(/^[\\/]/, '');
    }
    await debug(`Creating new file "${fileName}" from template "${fileType}" in "${parentDir}"`);

    try {
      let finalFileName = fileName;
      const expectedExtension = `.${fileType.toLowerCase()}.md`;
      if (!finalFileName.toLowerCase().endsWith(expectedExtension)) {
        if (finalFileName.toLowerCase().endsWith('.md')) {
          finalFileName = finalFileName.slice(0, -3);
        }
        finalFileName += expectedExtension;
      }


      const relativePath = await invoke<string>('create_file_from_template', {
        workspacePath,
        parentDir,
        fileName: finalFileName,
        fileType, // Pass the type string (e.g., "Character", "Location")
      });
      await debug(`File created from template at relative path: ${relativePath}`);

      await loadFileTree();
      await openFile(relativePath);

      toast.success(`File "${finalFileName}" created from template`);
      return relativePath;
    } catch (err) {
      const errorMessage = `Failed to create file "${fileName}" from template: ${err}`;
      await logError(errorMessage);
      toast.error('Failed to create file from template', {
        description: String(err),
      });
      return null;
    }
  }

  async function toggleConsole() {
    showConsole.value = !showConsole.value
    await saveEditorState()
  }

  async function toggleInspector() {
    showInspector.value = !showInspector.value
    await saveEditorState()
  }

  async function getWelcomeText(workspaceName: string): Promise<string> {
    return await invoke<string>('get_welcome_text', {workspaceName})
  }

  async function markTabAsChanged(tabId: string | null = activeTabId.value) {
    if (!tabId) return;
    const tab = openTabs.value.find(t => t.id === tabId);
    if (tab && !tab.hasUnsavedChanges) {
      tab.hasUnsavedChanges = true;
      await debug(`Marked tab ${tabId} as having unsaved changes.`);
    }
  }

  return {
    // State
    currentWorkspace,
    fileTree,
    openTabs,
    activeTabId,
    showConsole,
    showInspector,
    indexingProgress,
    isLoading,
    editorError,
    activeFileContent,
    activeFileFrontmatter,

    // Computed
    activeTab,

    // Actions
    openWorkspace,
    closeWorkspace,
    loadFileTree,
    refreshFileTree,
    openFile,
    closeTab,
    getFileContent,
    saveFileContent,
    createNewFile,
    createFromTemplate,
    toggleConsole,
    toggleInspector,
    getWelcomeText,
    markTabAsChanged,
    saveEditorState,
  };
});
