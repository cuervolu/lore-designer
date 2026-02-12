<script setup lang="ts">
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePlatform } from '@/core/platform/composable'
import { buildEditorMenu, buildWizardMenu } from '@/core/menu'
import type { MenuActionHandlers } from '@lore/shared'
import { error as logError } from '@fltsci/tauri-plugin-tracing'

const route = useRoute()
const { platform } = usePlatform()

const menuHandlers: MenuActionHandlers = {
  onNewFile: () => console.log('New file'),
  onOpenFile: async () => console.log('Open file'),
  onSave: () => console.log('Save'),
  onSaveAs: () => console.log('Save as'),
  onCloseFile: () => console.log('Close file'),
  onExitToWorkspaces: () => console.log('Exit to workspaces'),
  onWorkspacesHome: () => console.log('Workspaces home'),
  onNewWorkspace: () => console.log('New workspace'),
  onRefreshFileTree: () => console.log('Refresh file tree'),
  onWorkspaceSettings: () => console.log('Workspace settings'),
  onToggleConsole: () => console.log('Toggle console'),
  onToggleInspector: () => console.log('Toggle inspector'),
  onZoomIn: () => console.log('Zoom in'),
  onZoomOut: () => console.log('Zoom out'),
  onResetZoom: () => console.log('Reset zoom'),
  onDocumentation: () => console.log('Documentation'),
  onKeyboardShortcuts: () => console.log('Keyboard shortcuts'),
  onCheckUpdates: () => console.log('Check updates'),
  onAbout: () => console.log('About'),
  getConsoleOpen: () => false,
  getInspectorOpen: () => true,
}

async function updateNativeMenu(routeName: string | symbol | null | undefined) {
  try {
    const ctx = {
      platform: platform.value,
      handlers: menuHandlers,
    }

    if (routeName === 'editor') {
      await buildEditorMenu(ctx)
    } else {
      await buildWizardMenu(ctx)
    }
  } catch (err) {
    logError(`Failed to build native menu: ${err}`)
  }
}

watch(
  () => route.name,
  (newRouteName) => updateNativeMenu(newRouteName),
  { immediate: true },
)
</script>

<template>
  <main class="h-screen w-screen bg-background text-foreground">
    <router-view />
  </main>
</template>