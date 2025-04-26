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
  <SidebarProvider>
    <div class="flex flex-col h-full w-full overflow-hidden">
      <EditorMenubar
        @toggle-console="toggleConsole"
        class="flex-shrink-0 z-10"
      />

      <div class="flex flex-1 overflow-hidden">
        <div class="lg:hidden absolute top-4 left-4 z-20">
          <SidebarTrigger />
        </div>

        <FilesystemSidebar class="flex-shrink-0" />

        <div class="flex flex-col flex-1 h-full overflow-hidden min-h-0">
          <EditorTabs
            :files="files"
            :active-tab="activeTab"
            class="flex-shrink-0"
          />

          <div class="flex-1 overflow-auto bg-background">
            <EditorContent
              v-if="activeFile"
              :key="activeFile.id"
              :file="activeFile"
            />
            <div
              v-else
              class="flex items-center justify-center h-full text-muted-foreground"
            >
              Select a file to view its content.
            </div>
          </div>

          <ConsolePanel
            v-if="showConsole"
            @close="toggleConsole"
            class="flex-shrink-0"
          />
        </div>

        <InspectorPanel :file="activeFile" class="flex-shrink-0" />
      </div>

      <StatusFooter
        :is-indexing="isIndexing"
        :progress="progress"
        class="flex-shrink-0"
      />
    </div>
  </SidebarProvider>
</template>
