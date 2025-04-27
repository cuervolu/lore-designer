<script setup lang="ts">
import {ref, computed, watch} from "vue";
import {SidebarProvider, SidebarTrigger} from "@/components/ui/sidebar";
import EditorMenubar from "@editor/components/EditorMenubar.vue";
import FilesystemSidebar from "@editor/components/FilesystemSidebar.vue";
import EditorTabs from "@editor/components/EditorTabs.vue";
import EditorContent from "@editor/components/EditorContent.vue";
import ConsolePanel from "@editor/components/ConsolePanel.vue";
import InspectorPanel from "@editor/components/InspectorPanel.vue";
import StatusFooter from "@editor/components/StatusFooter.vue";
import {useAppTitle} from "@common/composables/useAppTitle";

const props = defineProps<{
  activeTab: string;
  files: {
    id: string;
    name: string;
    extension: string;
    path: string;
    icon: string;
  }[];
  workspaceName?: string;
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
  return props.files.find((file) => file.id === props.activeTab);
});

const {setEditorTitle, resetTitle} = useAppTitle();
watch(
  activeFile,
  (newFile) => {
    if (newFile) {
      const currentWorkspaceName = props.workspaceName || "My Workspace";
      setEditorTitle(newFile.name, newFile.extension, currentWorkspaceName);
    } else {
      resetTitle();
    }
  },
  {immediate: true}
);


const toggleConsole = () => {
  showConsole.value = !showConsole.value;
};
</script>

<template>
  <!-- Wrapper with fixed height and explicit flex layout to ensure footer visibility -->
  <div class="flex flex-col h-[calc(100vh-36px)] w-full">
    <!-- SidebarProvider goes inside the main content area -->
    <SidebarProvider class="flex flex-col flex-1 min-h-0 overflow-hidden">
      <!-- Menubar with higher z-index -->
      <EditorMenubar
        @toggle-console="toggleConsole"
        class="flex-shrink-0 z-20 relative"
      />

      <!-- Main content area with explicit flex properties and min-height:0 to ensure proper scrolling -->
      <div class="flex flex-1 min-h-0 overflow-hidden relative">
        <!-- Mobile sidebar trigger -->
        <div class="lg:hidden absolute top-4 left-4 z-30">
          <SidebarTrigger />
        </div>

        <!-- Filesystem sidebar with explicit height -->
        <FilesystemSidebar class="flex-shrink-0 h-full z-10" />

        <!-- Middle content area with correct flex properties -->
        <div class="flex flex-col flex-1 min-w-0 min-h-0 overflow-hidden relative">
          <!-- Editor tabs -->
          <EditorTabs
            :files="files"
            :active-tab="activeTab"
            class="flex-shrink-0"
          />

          <!-- Main editor content with max-height to ensure it doesn't push the footer out of view -->
          <div class="flex-1 min-h-0 overflow-hidden bg-background">
            <EditorContent
              v-if="activeFile"
              :key="activeFile.id"
              :file="activeFile"
              class="h-full"
            />
            <div
              v-else
              class="flex items-center justify-center h-full text-muted-foreground"
            >
              Select a file to view its content.
            </div>
          </div>

          <!-- Console panel when shown -->
          <ConsolePanel
            v-if="showConsole"
            @close="toggleConsole"
            class="flex-shrink-0"
          />
        </div>

        <!-- Inspector panel -->
        <InspectorPanel :file="activeFile" class="flex-shrink-0 h-full" />
      </div>

      <!-- Footer positioned at the bottom with high z-index -->
      <StatusFooter
        :is-indexing="isIndexing"
        :progress="progress"
        class="flex-shrink-0 z-20"
      />
    </SidebarProvider>
  </div>
</template>
