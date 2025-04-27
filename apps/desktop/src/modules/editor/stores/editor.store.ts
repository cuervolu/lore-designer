import {defineStore} from 'pinia'
import {ref, computed} from 'vue'
import {invoke} from '@tauri-apps/api/core'
import {error as logError} from '@tauri-apps/plugin-log'
import {toast} from 'vue-sonner'
import {usePreferencesStore} from '@common/stores/preferences.store'
import type {
  WorkspaceInfo,
  FileTree,
  EditorFile,
  FileContent,
  EditorState,
  TabInfo,
  IndexingProgress
} from "@editor/types/editor.types";

export const useEditorStore = defineStore('stores', () => {
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

  const activeTab = computed(() => {
    return openTabs.value.find(tab => tab.id === activeTabId.value) || null
  })

  // Actions

  /**
   * Open a workspace in the stores
   */
  async function openWorkspace(workspacePath: string) {
    try {
      isLoading.value = true
      editorError.value = null

      const workspace = await invoke<WorkspaceInfo>('open_workspace_in_editor', {
        workspacePath
      })

      currentWorkspace.value = workspace

      // Load file tree
      await loadFileTree()

      // Start polling for indexing progress
      startIndexingProgressPoll()

      // Load stores state
      await loadEditorState()

      // Update preferences
      await preferencesStore.updateLastProject(workspacePath)

      return workspace
    } catch (err) {
      const errorMessage = `Failed to open workspace: ${err}`
      await logError(errorMessage)
      editorError.value = errorMessage

      toast.error('Failed to open workspace', {
        description: String(err)
      })

      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Load the file tree for the current workspace
   */
  async function loadFileTree() {
    if (!currentWorkspace.value) {
      return []
    }

    try {
      const tree = await invoke<FileTree[]>('get_workspace_file_tree', {
        workspacePath: currentWorkspace.value.path
      })

      fileTree.value = tree
      return tree
    } catch (err) {
      const errorMessage = `Failed to load file tree: ${err}`
      await logError(errorMessage)

      toast.error('Failed to load file tree', {
        description: 'The workspace may still be indexing. Please try again later.'
      })

      return []
    }
  }

  /**
   * Start polling for indexing progress
   */
  function startIndexingProgressPoll() {
    if (!currentWorkspace.value) return

    const pollInterval = setInterval(async () => {
      try {
        const progress = await invoke<IndexingProgress | null>('get_indexing_progress', {
          workspacePath: currentWorkspace.value?.path
        })

        indexingProgress.value = progress

        // Stop polling when complete
        if (progress?.completed) {
          clearInterval(pollInterval)

          // Reload file tree after indexing completes
          await loadFileTree()
        }
      } catch (err) {
        await logError(`Error polling indexing progress: ${err}`)
        clearInterval(pollInterval)
      }
    }, 1000)

    // Cleanup function
    return () => clearInterval(pollInterval)
  }

  /**
   * Load the stores state for the current workspace
   */
  async function loadEditorState() {
    if (!currentWorkspace.value) return

    try {
      const state = await invoke<EditorState>('load_editor_state', {
        workspacePath: currentWorkspace.value.path
      })

      // Convert tabs to our format
      openTabs.value = state.open_tabs.map(tab => ({
        id: tab.id,
        name: tab.title,
        path: tab.path,
        extension: tab.path.split('.').pop() || '',
        icon: tab.icon || 'fileText',
        hasUnsavedChanges: tab.has_unsaved_changes
      }))

      activeTabId.value = state.active_tab || null
      showConsole.value = state.panels.console_visible
      showInspector.value = state.panels.inspector_visible

      // If no tabs are open, but we have recently opened files, open the first one
      if (openTabs.value.length === 0 && state.recently_opened.length > 0) {
        await openFile(state.recently_opened[0])
      }

      return state
    } catch (err) {
      const errorMessage = `Failed to load editor state: ${err}`
      await logError(errorMessage)

      // Don't show toast to avoid overwhelming the user
      return null
    }
  }

  /**
   * Save the current stores state
   */
  async function saveEditorState() {
    if (!currentWorkspace.value) return

    try {
      const tabsInfo = openTabs.value.map(tab => ({
        id: tab.id,
        path: tab.path,
        title: tab.name,
        icon: tab.icon,
        has_unsaved_changes: tab.hasUnsavedChanges || false
      }))

      const state: EditorState = {
        workspace_path: currentWorkspace.value.path,
        open_tabs: tabsInfo,
        active_tab: activeTabId.value || undefined,
        panels: {
          console_visible: showConsole.value,
          inspector_visible: showInspector.value,
          explorer_width: 250, // Default or stored values
          inspector_width: 300,
          console_height: 200
        },
        recently_opened: openTabs.value.map(tab => tab.path)
      }

      await invoke('save_editor_state', {state})
    } catch (err) {
      await logError(`Failed to save editor state: ${err}`)
      // We don't show a toast here to avoid spamming the user
    }
  }

  /**
   * Open a file in the stores
   */
  async function openFile(filePath: string) {
    if (!currentWorkspace.value) return null

    try {
      // Check if already open
      const existingTab = openTabs.value.find(tab => tab.path === filePath)
      if (existingTab) {
        activeTabId.value = existingTab.id
        return existingTab
      }

      const tab = await invoke<TabInfo>('open_file_in_editor', {
        workspacePath: currentWorkspace.value.path,
        filePath
      })

      // Convert to our format
      const newTab: EditorFile = {
        id: tab.id,
        name: tab.title,
        path: tab.path,
        extension: tab.path.split('.').pop() || '',
        icon: tab.icon || 'fileText',
        hasUnsavedChanges: tab.has_unsaved_changes
      }

      openTabs.value.push(newTab)
      activeTabId.value = newTab.id

      // Save state
      await saveEditorState()

      return newTab
    } catch (err) {
      const errorMessage = `Failed to open file: ${err}`
      await logError(errorMessage)

      toast.error('Failed to open file', {
        description: String(err)
      })

      return null
    }
  }

  /**
   * Close a tab
   */
  async function closeTab(tabId: string) {
    // Find tab index
    const index = openTabs.value.findIndex(tab => tab.id === tabId)
    if (index === -1) return

    // If active tab, switch to another
    if (activeTabId.value === tabId) {
      // Try to select the tab to the left, if not available, try right
      if (index > 0) {
        activeTabId.value = openTabs.value[index - 1].id
      } else if (openTabs.value.length > 1) {
        activeTabId.value = openTabs.value[1].id
      } else {
        activeTabId.value = null
      }
    }

    // Remove the tab
    openTabs.value.splice(index, 1)

    // Save state
    await saveEditorState()
  }

  /**
   * Get content of a file
   */
  async function getFileContent(filePath: string): Promise<FileContent> {
    if (!currentWorkspace.value) {
      throw new Error('No workspace open')
    }

    return await invoke<FileContent>('get_file_content', {
      workspacePath: currentWorkspace.value.path,
      filePath
    })
  }

  /**
   * Save content to a file
   */
  async function saveFileContent(filePath: string, content: string, isJson = false) {
    if (!currentWorkspace.value) {
      throw new Error('No workspace open')
    }

    try {
      const request = isJson ?
        {type: 'Json', data: {content}} :
        {type: 'Text', data: {content}}

      await invoke('save_file_content', {
        workspacePath: currentWorkspace.value.path,
        filePath,
        content: request
      })

      // Update tab status
      const tab = openTabs.value.find(tab => tab.path === filePath)
      if (tab) {
        tab.hasUnsavedChanges = false
      }

      // Save state
      await saveEditorState()

      toast.success('File saved successfully')
      return true
    } catch (err) {
      const errorMessage = `Failed to save file: ${err}`
      await logError(errorMessage)

      toast.error('Failed to save file', {
        description: String(err)
      })

      return false
    }
  }

  /**
   * Create a new file
   */
  async function createNewFile(parentDir: string, fileName: string, initialContent = '') {
    if (!currentWorkspace.value) {
      throw new Error('No workspace open')
    }

    try {
      const filePath = await invoke<string>('create_new_file', {
        workspacePath: currentWorkspace.value.path,
        parentDir,
        fileName,
        initialContent
      })

      // Reload file tree
      await loadFileTree()

      // Open the new file
      await openFile(filePath)

      toast.success('File created successfully')
      return filePath
    } catch (err) {
      const errorMessage = `Failed to create file: ${err}`
      await logError(errorMessage)

      toast.error('Failed to create file', {
        description: String(err)
      })

      return null
    }
  }

  /**
   * Toggle console visibility
   */
  async function toggleConsole() {
    showConsole.value = !showConsole.value
    await saveEditorState()
  }

  /**
   * Toggle inspector visibility
   */
  async function toggleInspector() {
    showInspector.value = !showInspector.value
    await saveEditorState()
  }

  /**
   * Get welcome text for a new workspace
   */
  async function getWelcomeText(workspaceName: string): Promise<string> {
    return await invoke<string>('get_welcome_text', {workspaceName})
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

    // Computed
    activeTab,

    // Actions
    openWorkspace,
    loadFileTree,
    openFile,
    closeTab,
    getFileContent,
    saveFileContent,
    createNewFile,
    toggleConsole,
    toggleInspector,
    getWelcomeText
  }
})
