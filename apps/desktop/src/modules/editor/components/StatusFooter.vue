<script setup lang="ts">
import {Loader2, AlertCircle} from 'lucide-vue-next';
import {Progress} from '@/components/ui/progress';
import {Badge} from '@/components/ui/badge';
import {onMounted, ref, computed} from 'vue';
import {getVersion} from '@tauri-apps/api/app';
import {error} from 'tauri-plugin-tracing';
import {useEditorStore} from '@editor/stores/editor.store';
import ImageValidationDialog from './ImageValidationDialog.vue';

const editorStore = useEditorStore();
const appVersion = ref('0.1.0');
const showValidationDialog = ref(false);

const isIndexing = computed(() => !!editorStore.indexingProgress && !editorStore.indexingProgress.completed);

const indexingProgressValue = computed(() => {
  const progressData = editorStore.indexingProgress;
  if (!progressData || progressData.total <= 0) {
    return 0;
  }
  const percentage = (progressData.processed / progressData.total) * 100;
  return Math.min(percentage, 100);
});

const showStats = computed(() =>
  editorStore.activeTab &&
  editorStore.activeFileCharCount !== null &&
  !isImageViewerActive.value
);

const isImageViewerActive = computed(() => {
  return editorStore.activeFileType === 'Image';
});

const hasImageIssues = computed(() => {
  const report = editorStore.imageValidationReport;
  return report && (report.missing_images.length > 0 || report.orphaned_images.length > 0);
});

const imageIssuesCount = computed(() => {
  const report = editorStore.imageValidationReport;
  if (!report) return 0;
  return report.missing_images.length + report.orphaned_images.length;
});

const openValidationDialog = () => {
  showValidationDialog.value = true;
};

const handleOpenFile = (filePath: string) => {
  editorStore.openFile(filePath);
  showValidationDialog.value = false;
};

onMounted(async () => {
  try {
    appVersion.value = await getVersion();
  } catch (err) {
    await error(`Failed to get app version: ${err}`);
  }
});
</script>

<template>
  <div
    class="border-t h-6 bg-muted flex items-center px-3 text-xs text-muted-foreground justify-between select-none"
  >
    <!-- Left side - status info -->
    <div class="flex items-center gap-2 min-w-0">
      <!-- Image validation warning (highest priority) -->
      <template v-if="hasImageIssues && !isIndexing">
        <button
          @click="openValidationDialog"
          class="flex items-center gap-1.5 hover:text-warning transition-colors cursor-pointer"
          title="Click to view image issues"
        >
          <AlertCircle class="h-3 w-3 text-warning shrink-0"/>
          <span class="shrink-0">Image issues detected</span>
          <Badge variant="warning" class="h-4 px-1.5 text-[10px]">
            {{ imageIssuesCount }}
          </Badge>
        </button>
      </template>
      
      <!-- Indexing takes priority -->
      <template v-else-if="isIndexing">
        <Loader2 class="h-3 w-3 animate-spin shrink-0"/>
        <span class="shrink-0">Indexing...</span>
        <Progress class="w-24 h-1.5 shrink-0" :model-value="indexingProgressValue"/>
      </template>
      
      <!-- Active Tab Info -->
      <template v-else-if="editorStore.activeTab">
        <span class="truncate" :title="editorStore.activeTab.path">
          {{ editorStore.activeTab.path }}
        </span>
        <span
          v-if="editorStore.activeTab.hasUnsavedChanges"
          class="ml-1 text-foreground font-bold shrink-0"
          title="Unsaved changes"
        >*</span>
      </template>
      
      <!-- Default -->
      <template v-else>
        <span>Ready</span>
      </template>
    </div>

    <!-- Middle - file stats -->
    <div v-if="showStats" class="flex items-center gap-2 px-2">
      <span title="Lines">{{ editorStore.activeFileLineCount ?? 0 }} L</span>
      <span class="text-muted-foreground/50">|</span>
      <span title="Words">{{ editorStore.activeFileWordCount ?? 0 }} W</span>
      <span class="text-muted-foreground/50">|</span>
      <span title="Characters">{{ editorStore.activeFileCharCount ?? 0 }} C</span>
    </div>
    <div v-else-if="editorStore.activeTab && !isImageViewerActive"
         class="flex items-center gap-2 px-2 text-muted-foreground/50">
      <span>- L</span>
      <span class="text-muted-foreground/30">|</span>
      <span>- W</span>
      <span class="text-muted-foreground/30">|</span>
      <span>- C</span>
    </div>

    <!-- Right side - version info -->
    <div class="shrink-0">Lore Designer v{{ appVersion }}</div>
  </div>

  <!-- Validation Dialog -->
  <ImageValidationDialog
    v-model:open="showValidationDialog"
    @open-file="handleOpenFile"
  />
</template>