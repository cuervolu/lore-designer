<script setup lang="ts">
import { ref, computed } from 'vue';
import { SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '@/components/ui/resizable';
import EditorMenubar from "@editor/components/EditorMenubar.vue";
import FilesystemSidebar from "@editor/components/FilesystemSidebar.vue";
import EditorTabs from "@editor/components/EditorTabs.vue";
import EditorContent from "@editor/components/EditorContent.vue";
import ConsolePanel from "@editor/components/ConsolePanel.vue";
import InspectorPanel from "@editor/components/InspectorPanel.vue";
import StatusFooter from "@editor/components/StatusFooter.vue";

const props = defineProps<{
  activeTab: string;
  files: {
    id: string;
    name: string;
    extension: string;
    path: string;
    icon: string;
  }[];
}>();

const showConsole = ref(false);
const isIndexing = ref(true);
const progress = ref(0);
const indexingInterval = setInterval(() => {
  progress.value += 5;
  if (progress.value >= 100) {
    clearInterval(indexingInterval);
    isIndexing.value = false;
  }
}, 500);

const activeFile = computed(() => {
  return props.files.find(file => file.id === props.activeTab);
});

// Toggle console visibility
const toggleConsole = () => {
  showConsole.value = !showConsole.value;
};
</script>

<template>
  <SidebarProvider>
    <div class="flex flex-col h-full">
      <!-- Editor Menubar -->
      <EditorMenubar @toggle-console="toggleConsole" />

      <div class="flex-1 flex overflow-hidden">
        <!-- Sidebar Trigger (visible on mobile) -->
        <div class="lg:hidden absolute top-4 left-4 z-20">
          <SidebarTrigger />
        </div>

        <ResizablePanelGroup direction="horizontal" class="w-full overflow-hidden">
          <!-- Left Sidebar - Filesystem -->
          <ResizablePanel :defaultSize="20" :minSize="15" :maxSize="30" class="h-full">
            <FilesystemSidebar />
          </ResizablePanel>

          <ResizableHandle />

          <!-- Main Editor Area -->
          <ResizablePanel :defaultSize="60" class="h-full">
            <div class="flex-1 flex flex-col h-full overflow-hidden">
              <!-- Editor Tabs -->
              <EditorTabs
                :files="files"
                :active-tab="activeTab"
              />

              <!-- Editor Content -->
              <div class="flex-1 overflow-auto">
                <EditorContent
                  v-if="activeFile"
                  :file="activeFile"
                />
              </div>

              <!-- Console Panel (conditionally shown) -->
              <ConsolePanel v-if="showConsole" />
            </div>
          </ResizablePanel>

          <ResizableHandle />

          <!-- Right Sidebar - Inspector -->
          <ResizablePanel :defaultSize="20" :minSize="15" :maxSize="30" class="h-full">
            <InspectorPanel :file="activeFile" />
          </ResizablePanel>
        </ResizablePanelGroup>
      </div>

      <!-- Status Footer -->
      <StatusFooter
        :is-indexing="isIndexing"
        :progress="progress"
      />
    </div>
  </SidebarProvider>
</template>
