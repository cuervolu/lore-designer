<script setup lang="ts">
import { computed } from 'vue'
import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { useEditorStore } from '@editor/stores/editor.store'
import EditorMenubar from '@editor/components/EditorMenubar.vue'
import FilesystemSidebar from '@editor/components/FilesystemSidebar.vue'
import EditorTabs from '@editor/components/EditorTabs.vue'
import EditorContent from '@editor/components/EditorContent.vue'
import ConsolePanel from '@editor/components/ConsolePanel.vue'
import InspectorPanel from '@editor/components/InspectorPanel.vue'
import StatusFooter from '@editor/components/StatusFooter.vue'

const editorStore = useEditorStore()

const toggleConsole = () => {
  editorStore.toggleConsole()
}

const isIndexing = computed(() => {
  return editorStore.indexingProgress && !editorStore.indexingProgress.completed
})

const indexingProgress = computed(() => {
  if (!editorStore.indexingProgress) return 0

  const { processed, total } = editorStore.indexingProgress
  if (total === 0) return 0

  return Math.floor((processed / total) * 100)
})
</script>

<template>
  <div class="flex flex-col h-[calc(100vh-36px)] w-full">
    <EditorMenubar
      @toggle-console="toggleConsole"
      class="flex-shrink-0 z-30 relative"
    />

    <SidebarProvider class="flex flex-col flex-1 min-h-0 overflow-hidden">
      <div class="flex flex-1 min-h-0 overflow-hidden relative">
        <div class="lg:hidden absolute top-4 left-4 z-10">
          <SidebarTrigger />
        </div>

        <FilesystemSidebar class="flex-shrink-0 h-full z-10" />

        <div class="flex flex-col flex-1 min-w-0 min-h-0 overflow-hidden relative">
          <EditorTabs
            :files="editorStore.openTabs"
            :active-tab="editorStore.activeTabId"
            @close-tab="editorStore.closeTab"
            class="flex-shrink-0"
          />

          <div class="flex-1 min-h-0 overflow-hidden bg-background">
            <EditorContent
              v-if="editorStore.activeTab"
              :key="editorStore.activeTab.id"
              :file="editorStore.activeTab"
              class="h-full"
            />
            <div
              v-else
              class="flex items-center justify-center h-full text-muted-foreground"
            >
              Select a file to view its content or create a new one.
            </div>
          </div>

          <ConsolePanel
            v-if="editorStore.showConsole"
            @close="toggleConsole"
            class="flex-shrink-0"
          />
        </div>

        <InspectorPanel
          v-if="editorStore.showInspector"
          :file="editorStore.activeTab"
          class="flex-shrink-0 h-full"
        />
      </div>

      <StatusFooter
        :is-indexing="isIndexing"
        :progress="indexingProgress"
        class="flex-shrink-0 z-20"
      />
    </SidebarProvider>
  </div>
</template>
