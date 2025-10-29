<script setup lang="ts">
import {getCurrentWindow} from '@tauri-apps/api/window';
import {ref, onMounted} from 'vue'; // Removed computed
import {cn} from '@/lib/utils';
import {Minus, X, Square} from 'lucide-vue-next';
import WindowRestoreIcon from '@common/icons/WindowRestoreIcon.vue';
import { useI18n } from 'vue-i18n'

interface Props {
  /**
   * The complete title string to display, managed by useAppTitle.
   * This will include unsaved indicators, workspace name, etc.
   */
  title?: string;

  /**
   * Custom CSS class for the titlebar
   */
  class?: string;
}

// Removed fileName, fileExt, workspaceName props
const props = withDefaults(defineProps<Props>(), {
  title: 'Lore Designer', // Default title
  class: ''
});

// Window state
const isMaximized = ref(false);
const appWindow = ref(); // Keep window ref
const { t } = useI18n();

// Initialize window reference and listeners
onMounted(async () => {
  appWindow.value = getCurrentWindow();
  await appWindow.value.onResized(updateMaximizeState); // Use shorthand
  await updateMaximizeState();
});

async function updateMaximizeState() {
  if (!appWindow.value) return;
  isMaximized.value = await appWindow.value.isMaximized();
}

const minimizeWindow = async () => {
  await appWindow.value?.minimize(); // Use optional chaining
};

const toggleMaximize = async () => {
  if (appWindow.value) {
    await appWindow.value.toggleMaximize();
    // No need to manually update isMaximized, onResized listener handles it
  }
};

const closeWindow = async () => {
  await appWindow.value?.close(); // Use optional chaining
};

// Removed displayTitle computed property
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
          <slot name="logo"/>
        </div>
      </slot>
      <span
        class="text-sm font-medium truncate"
        data-tauri-drag-region
      >
       {{ props.title }}
      </span>
    </div>

    <!-- Centered content (optional) -->
    <div class="flex-1 text-center" data-tauri-drag-region>
      <slot name="center"/>
    </div>

    <!-- Window Controls -->
    <div class="flex items-center">
      <button
        class="inline-flex items-center justify-center h-8 w-8 rounded-md text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
        @click="minimizeWindow"
        :title="t('app.minimize')"
      >
        <Minus class="h-4 w-4"/>
      </button>

      <button
        class="inline-flex items-center justify-center h-8 w-8 rounded-md text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
        @click="toggleMaximize"
        :title="isMaximized ? t('app.restore') : t('app.maximize')"
      >
        <Square v-if="!isMaximized" class="h-4 w-4"/>
        <WindowRestoreIcon v-else class="h-4 w-4"/>
      </button>

      <button
        class="inline-flex items-center justify-center h-8 w-8 rounded-md text-muted-foreground hover:bg-destructive hover:text-destructive-foreground transition-colors"
        @click="closeWindow"
        :title="t('app.close')"
      >
        <X class="h-4 w-4"/>
      </button>
    </div>
  </div>
</template>
