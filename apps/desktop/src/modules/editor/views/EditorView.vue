<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useEditorStore } from '@editor/stores/editor.store';
import { useAppTitle } from '@common/composables/useAppTitle';
import EditorLayout from '@editor/layouts/EditorLayout.vue';
import { Progress } from '@/components/ui/progress';
import { Button } from '@/components/ui/button';
import { usePlatform } from '@common/composables/usePlatform';

const route = useRoute();
const router = useRouter();
const editorStore = useEditorStore();
const { setEditorTitle, resetTitle } = useAppTitle();
const { platform } = usePlatform();

const workspacePath = ref('');
const isLoadingWorkspace = ref(true);
const errorMessage = ref('');

const indexProgress = computed(() => {
  if (!editorStore.indexingProgress) return 0;

  const { processed, total } = editorStore.indexingProgress;
  if (total === 0) return 0;
  return Math.floor((processed / total) * 100);
});

const handleKeyDown = (event: KeyboardEvent) => {
  const isMacPlatform = platform.value === 'macos';
  const isSaveShortcut = (isMacPlatform && event.metaKey && event.key === 's') ||
    (!isMacPlatform && event.ctrlKey && event.key === 's');

  if (isSaveShortcut) {
    event.preventDefault();
    triggerSave();
  }
};

const triggerSave = async () => {
  if (!editorStore.activeTab) {
    return;
  }
  if (!editorStore.activeTab.hasUnsavedChanges) {
    return;
  }
  const contentToSave = editorStore.activeFileContent;
  const frontmatterToSave = editorStore.activeFileFrontmatter;

  await editorStore.saveFileContent(
    editorStore.activeTab.path,
    contentToSave,
    frontmatterToSave
  );
};

watch(
  () => editorStore.activeTab,
  (newTab) => {
    if (newTab && editorStore.currentWorkspace) {
      setEditorTitle(
        newTab.name,
        newTab.extension,
        editorStore.currentWorkspace.name
      );
    } else if (editorStore.currentWorkspace) {
      setEditorTitle('', '', editorStore.currentWorkspace.name);
    } else {
      resetTitle();
    }
  },
  { immediate: true, deep: true }
);

onMounted(async () => {
  window.addEventListener('keydown', handleKeyDown);
  // Get workspace path from query parameter
  const workspacePathParam = route.query.path;

  if (!workspacePathParam || typeof workspacePathParam !== 'string') {
    errorMessage.value = 'No workspace path provided';
    isLoadingWorkspace.value = false;
    return;
  }

  workspacePath.value = workspacePathParam;

  try {
    // Open the workspace
    await editorStore.openWorkspace(workspacePathParam);
    isLoadingWorkspace.value = false;
  } catch (err) {
    errorMessage.value = `Failed to open workspace: ${err}`;
    isLoadingWorkspace.value = false;
  }
});

// Cleanup on component unmount
onBeforeUnmount(async () => {
  window.removeEventListener('keydown', handleKeyDown);
  await resetTitle();
  // Clean up workspace resources
  await editorStore.closeWorkspace();
});

const goToWizard = () => {
  router.push({ name: 'workspaces' });
};
</script>

<template>
  <div class="h-[calc(100vh-36px)] w-full flex flex-col">
    <!-- Loading state -->
    <div v-if="isLoadingWorkspace" class="flex-1 flex flex-col items-center justify-center">
      <div class="w-72 space-y-4 text-center">
        <h2 class="text-xl font-semibold">Opening Workspace</h2>
        <div class="space-y-2">
          <Progress :model-value="indexProgress" class="w-full" />
          <p class="text-muted-foreground text-sm">
            {{ editorStore.indexingProgress?.current_file || 'Initializing...' }}
          </p>
        </div>
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="errorMessage" class="flex-1 flex flex-col items-center justify-center">
      <div class="w-96 space-y-4 text-center">
        <h2 class="text-xl font-semibold text-destructive">Error</h2>
        <p>{{ errorMessage }}</p>
        <Button @click="goToWizard">Return to Workspaces</Button>
      </div>
    </div>

    <!-- Editor layout -->
    <EditorLayout v-else />
  </div>
</template>
