<script setup lang="ts">
import {
  User,
  Home,
  PenTool,
  Book,
  Map as Mapp,
  FileText,
  Image,
  File,
  X,
  type LucideIcon
} from 'lucide-vue-next';
import type {EditorFile} from '@editor/types/editor.types';
import {useEditorStore} from "@editor/stores/editor.store.ts";

const props = defineProps<{
  files: EditorFile[];
  activeTab: string | null;
}>();

const emit = defineEmits<{
  'close-tab': [id: string];
}>();

const iconMap: Record<string, LucideIcon> = {
  'user': User,
  'character': User,
  'home': Home,
  'location': Home,
  'penTool': PenTool,
  'canvas': PenTool,
  'book': Book,
  'lore': Book,
  'map': Mapp,
  'fileText': FileText,
  'markdown': FileText,
  'image': Image,
  'file': File
};

const editorStore = useEditorStore();

const getIconComponent = (iconName: string) => {
  const key = iconName.toLowerCase();
  return iconMap[key] || FileText;
};

const selectTab = (tabId: string) => {
  if (editorStore.activeTabId !== tabId) {
    editorStore.activeTabId = tabId;
  }
};

const closeTab = (event: Event, tabId: string) => {
  event.stopPropagation(); // Prevent tab activation when closing
  emit('close-tab', tabId);
};
</script>

<template>
  <div class="flex border-b overflow-x-auto">
    <div
      v-for="file in files"
      :key="file.id"
      class="flex items-center px-3 py-2 cursor-pointer gap-2 border-r min-w-24 max-w-48 transition-colors select-none"
      :class="[
        activeTab === file.id
          ? 'text-foreground border-b-2 border-b-primary bg-muted/40'
          : 'text-muted-foreground hover:text-foreground hover:bg-muted/20'
      ]"
      @click="selectTab(file.id)"
    >
      <component :is="getIconComponent(file.icon)" class="h-4 w-4 flex-shrink-0"/>
      <span class="truncate">{{ file.name }}</span>
      <span v-if="file.hasUnsavedChanges" class="text-muted-foreground font-medium">*</span>
      <button
        class="flex-shrink-0 hover:bg-muted rounded-full p-0.5 opacity-60 hover:opacity-100"
        @click="(e) => closeTab(e, file.id)"
      >
        <X class="h-3.5 w-3.5"/>
      </button>
    </div>
  </div>
</template>
