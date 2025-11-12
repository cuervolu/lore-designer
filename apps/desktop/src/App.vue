<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Toaster } from '@/components/ui/sonner'
import 'vue-sonner/style.css'
import { usePreferencesStore } from '@common/stores/preferences.store'
import { usePlatform } from '@common/composables/usePlatform'
import { useEditorStore } from '@editor/stores/editor.store'
import { useUiStore } from '@common/stores/ui.store'
import {
  buildEditorMenu,
  buildWizardMenu,
} from '@common/services/menu.builder'
import AboutModal from '@common/components/AboutModal.vue'
import { open } from '@tauri-apps/plugin-dialog'
import { toast } from 'vue-sonner'
import { error as logError } from 'tauri-plugin-tracing'
import {useAutoSave} from "@editor/composables/useAutoSave.ts";

const preferencesStore = usePreferencesStore()
const route = useRoute()
const router = useRouter()
const i18n = useI18n()
const { t, locale } = i18n
const { saveNow } = useAutoSave()
const platform = usePlatform()
const editorStore = useEditorStore()
const uiStore = useUiStore()

const menuActions = {
  onNewFile: () => uiStore.openCreateFileModal(),
  onOpenFile: async () => {
    try {
      const path = await open({ directory: false, multiple: false })
      if (typeof path === 'string') {
        await editorStore.openFile(path)
      }
    } catch (err) {
      toast.error(t('editor.errors.openFileFailed', { err }))
    }
  },
  onSave: saveNow,
  onSaveAs: () => {
    /* TODO: editorStore.saveActiveFileAs() no existe aún */
  },
  onCloseFile: () =>
    // Corregido: Usar closeTab y activeTab.id
    editorStore.activeTab && editorStore.closeTab(editorStore.activeTab.id),
  onExitToWorkspaces: () => router.push({ name: 'workspaces' }),
  onWorkspacesHome: () => router.push({ name: 'workspaces' }),
  onNewWorkspace: () => router.push({ name: 'new-workspace' }),
  onRefreshFileTree: () => editorStore.refreshFileTree(),
  onWorkspaceSettings: () => {
    /* TODO */
  },
  onToggleConsole: () => editorStore.toggleConsole(),
  onToggleInspector: () => editorStore.toggleInspector(),
  onZoomIn: () => {
    /* TODO */
  },
  onZoomOut: () => {
    /* TODO */
  },
  onResetZoom: () => {
    /* TODO */
  },
  onDocumentation: () => {
    /* TODO */
  },
  onKeyboardShortcuts: () => {
    /* TODO */
  },
  onCheckUpdates: () => {
    /* TODO */
  },
  onAbout: () => uiStore.openAboutModal(),
  getConsoleOpen: () => editorStore.isConsoleOpen,
  getInspectorOpen: () => editorStore.isInspectorOpen,
}

async function updateNativeMenu(routeName: string | symbol | null | undefined) {
  try {
    if (routeName === 'editor') {
      await buildEditorMenu(i18n, platform, menuActions)
    } else {
      await buildWizardMenu(i18n, platform, menuActions)
    }
  } catch (err) {
    await logError(`Failed to build native menu: ${err}`)
  }
}

watch(
  () => route.name,
  (newRouteName) => {
    updateNativeMenu(newRouteName)
  },
  { immediate: true },
)

watch(locale, () => {
  updateNativeMenu(route.name)
})

onMounted(async () => {
  await preferencesStore.loadPreferences()
})
</script>

<template>
  <main class="h-screen w-screen bg-background text-foreground">
    <Toaster position="bottom-right" rich-colors />
    <router-view />
  </main>

  <AboutModal v-model:is-open="uiStore.isAboutModalOpen" />
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(128, 128, 128, 0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(128, 128, 128, 0.5);
}
</style>
