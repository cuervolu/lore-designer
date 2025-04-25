<script setup lang="ts">
import { getCurrentWindow } from '@tauri-apps/api/window';
import { ref, onMounted, computed } from 'vue';
import { cn } from '@/lib/utils';
import { Minus, X, Square } from 'lucide-vue-next';
import WindowRestoreIcon from '@common/icons/WindowRestoreIcon.vue'

interface Props {
  /**
   * Main title to display in the titlebar
   */
  title?: string;

  /**
   * Current file name (for editor mode)
   */
  fileName?: string;

  /**
   * File extension (for editor mode)
   */
  fileExt?: string;

  /**
   * Current workspace name (for editor mode)
   */
  workspaceName?: string;

  /**
   * Custom CSS class for the titlebar
   */
  class?: string;
}

const props = withDefaults(defineProps<Props>(), {
  title: 'Lore Designer',
  fileName: undefined,
  fileExt: undefined,
  workspaceName: undefined,
  class: ''
});

// Window state
const isMaximized = ref(false);
const appWindow = ref();

// Initialize window reference
onMounted(async () => {
  appWindow.value = getCurrentWindow();

  // Listen for window changes to update maximize/restore button
  // This makes sure the button state matches the actual window state
  await appWindow.value.onResized(() => {
    updateMaximizeState();
  });

  await updateMaximizeState();
});

async function updateMaximizeState() {
  if (!appWindow.value) return;
  isMaximized.value = await appWindow.value.isMaximized();
}

const minimizeWindow = async () => {
  if (appWindow.value) {
    await appWindow.value.minimize();
  }
};

const toggleMaximize = async () => {
  if (appWindow.value) {
    await appWindow.value.toggleMaximize();
    isMaximized.value = await appWindow.value.isMaximized();
  }
};

const closeWindow = async () => {
  if (appWindow.value) {
    await appWindow.value.close();
  }
};

// Computed display title based on mode (welcome or editor)
const displayTitle = computed(() => {
  if (props.fileName && props.workspaceName) {
    const extension = props.fileExt ? `.${props.fileExt}` : '';
    return `${props.fileName}${extension} - ${props.workspaceName}`;
  }
  return props.title;
});
</script>

<template>
  <div
    class="flex h-9 items-center border-b bg-background px-3 select-none"
    :class="cn(props.class)"
    data-tauri-drag-region
  >
    <div
      class="flex items-center mr-2 flex-1 truncate"
      data-tauri-drag-region
    >
      <slot name="icon">
        <div class="w-4 h-4 mr-2 flex items-center justify-center">
          <slot name="logo" />
        </div>
      </slot>
      <span
        class="text-sm font-medium truncate"
        data-tauri-drag-region
      >
        {{ displayTitle }}
      </span>
    </div>

    <!-- Centered content (optional) -->
    <div class="flex-1 text-center" data-tauri-drag-region>
      <slot name="center" />
    </div>

    <!-- Window Controls -->
    <div class="flex items-center">
      <button
        class="inline-flex items-center justify-center h-8 w-8 rounded-md text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
        @click="minimizeWindow"
        title="Minimize"
      >
        <Minus class="h-4 w-4" />
      </button>

      <button
        class="inline-flex items-center justify-center h-8 w-8 rounded-md text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
        @click="toggleMaximize"
        title="Maximize"
      >
        <Square v-if="!isMaximized" class="h-4 w-4" />
        <WindowRestoreIcon v-else class="h-4 w-4" />
      </button>

      <button
        class="inline-flex items-center justify-center h-8 w-8 rounded-md text-muted-foreground hover:bg-destructive hover:text-destructive-foreground transition-colors"
        @click="closeWindow"
        title="Close"
      >
        <X class="h-4 w-4" />
      </button>
    </div>
  </div>
</template>
