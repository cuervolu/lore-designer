<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next';
import { Progress } from '@/components/ui/progress';
import { onMounted, ref } from "vue";
import { getVersion } from "@tauri-apps/api/app";
import { error } from "@tauri-apps/plugin-log";
import { useEditorStore } from '@editor/stores/editor.store';

const props = withDefaults(defineProps<{
  isIndexing?: boolean;
  progress?: number;
}>(), {
  isIndexing: false,
  progress: 0
});

const editorStore = useEditorStore();
const appVersion = ref('0.1.0');

onMounted(async () => {
  try {
    appVersion.value = await getVersion();
  } catch (err) {
    await error(`Failed to get app version: ${err}`);
  }
});

const getFileStats = () => {
  if (!editorStore.activeTab) return null;

  // This could be expanded to get more stats based on file type
  return {
    lines: 0,
    words: 0,
    characters: 0
  };
};
</script>

<template>
  <div class="border-t h-6 bg-muted/20 flex items-center px-3 text-xs text-muted-foreground justify-between">
    <!-- Left side - status info -->
    <div class="flex items-center">
      <div v-if="isIndexing" class="flex items-center gap-2">
        <Loader2 class="h-3 w-3 animate-spin" />
        <span>Indexing...</span>
        <Progress class="w-24 h-1.5" :model-value="progress" />
      </div>
      <div v-else-if="editorStore.activeTab">
        <span>{{ editorStore.activeTab.path }}</span>
        <span v-if="editorStore.activeTab.hasUnsavedChanges" class="ml-2 text-foreground font-medium">*</span>
      </div>
      <div v-else>Ready</div>
    </div>

    <!-- Middle - file stats (if any) -->
    <div v-if="editorStore.activeTab && getFileStats()">
      {{ getFileStats()?.lines }} lines | {{ getFileStats()?.words }} words
    </div>

    <!-- Right side - version info -->
    <div>Lore Designer v{{ appVersion }}</div>
  </div>
</template>
