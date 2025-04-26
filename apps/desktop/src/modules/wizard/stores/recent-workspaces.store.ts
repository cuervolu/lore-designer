import { invoke } from '@tauri-apps/api/core'
import { error as logError } from '@tauri-apps/plugin-log'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface RecentWorkspace {
  name: string
  path: string
  last_accessed: number
  exists?: boolean
}

export const useRecentWorkspacesStore = defineStore('recentWorkspaces', () => {
  const workspaces = ref<RecentWorkspace[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  async function loadRecentWorkspaces() {
    try {
      isLoading.value = true
      error.value = null

      const recentWorkspaces = await invoke<RecentWorkspace[]>('get_recent_workspaces_command')

      for (const workspace of recentWorkspaces) {
        workspace.exists = await invoke<boolean>('check_workspace_exists_command', {
          path: workspace.path
        })
      }

      workspaces.value = recentWorkspaces
      return recentWorkspaces
    } catch (err) {
      const errorMessage = `Failed to load recent workspaces: ${err}`
      await logError(errorMessage)
      error.value = errorMessage
      throw err
    } finally {
      isLoading.value = false
    }
  }

  async function addRecentWorkspace(name: string, path: string) {
    try {
      await invoke('add_recent_workspace_command', { name, path })
      await loadRecentWorkspaces() // Refresh the list
    } catch (err) {
      const errorMessage = `Failed to add recent workspace: ${err}`
      await logError(errorMessage)
      error.value = errorMessage
      throw err
    }
  }

  async function removeRecentWorkspace(path: string) {
    try {
      await invoke('remove_recent_workspace_command', { path })
      await loadRecentWorkspaces() // Refresh the list
    } catch (err) {
      const errorMessage = `Failed to remove recent workspace: ${err}`
      await logError(errorMessage)
      error.value = errorMessage
      throw err
    }
  }

  return {
    workspaces,
    isLoading,
    error,
    loadRecentWorkspaces,
    addRecentWorkspace,
    removeRecentWorkspace
  }
})
