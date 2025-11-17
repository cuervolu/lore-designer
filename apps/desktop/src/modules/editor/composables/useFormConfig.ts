import { ref, shallowRef } from 'vue'
import {error as logError} from "tauri-plugin-tracing";
import { invoke } from '@tauri-apps/api/core'
import { useEditorStore } from '@/modules/editor/stores/editor.store'
import type { FormConfig } from '@/modules/editor/types/form.type'


export function useFormConfig() {
  const editorStore = useEditorStore()

  const config = shallowRef<FormConfig | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)


  async function loadConfig() {
    const workspacePath = editorStore.currentWorkspace?.path
    if (!workspacePath) {
      error.value = 'No workspace path is set.'
      return
    }

    isLoading.value = true
    error.value = null
    config.value = null

    try {
      const result = await invoke<FormConfig>('get_character_form_config', {
        workspacePath,
      })
      config.value = result
    }
    catch (e) {
      await logError(`Failed to load form config: ${e}`)
      error.value = e as string
    }
    finally {
      isLoading.value = false
    }
  }

  async function saveConfig(newConfig: FormConfig) {
    const workspacePath = editorStore.currentWorkspace?.path
    if (!workspacePath) {
      error.value = 'No workspace path is set.'
      return
    }

    isLoading.value = true
    error.value = null

    const oldConfig = config.value
    config.value = newConfig

    try {
      await invoke('save_character_form_config', {
        workspacePath,
        config: newConfig,
      })
    }
    catch (e) {
     await logError(`Failed to save form config: ${e}`)
      error.value = e as string
      config.value = oldConfig
    }
    finally {
      isLoading.value = false
    }
  }

  return {
    config,
    isLoading,
    error,
    loadConfig,
    saveConfig,
  }
}
