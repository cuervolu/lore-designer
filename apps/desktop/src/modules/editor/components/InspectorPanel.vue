<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { Search } from 'lucide-vue-next';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useEditorStore } from '@editor/stores/editor.store';
import { lstat } from '@tauri-apps/plugin-fs';
import { error as logError } from '@tauri-apps/plugin-log';
import type {EditorFile} from "@editor/types/editor.types.ts";

const props = defineProps<{
  file?: EditorFile | null;
}>();

const editorStore = useEditorStore();
const searchQuery = ref('');
const properties = ref<Array<{ name: string; value: string }>>([]);
const isLoading = ref(false);

// Load file properties when file changes
watch(() => props.file, async (newFile) => {
  if (newFile) {
    await loadFileProperties(newFile);
  } else {
    properties.value = [];
  }
}, { immediate: true });

// Load file properties from the system
async function loadFileProperties(file: EditorFile) {
  if (!editorStore.currentWorkspace) return;

  isLoading.value = true;
  properties.value = [];

  try {
    // Common properties - Fix: Display the name only, as it already includes the extension
    properties.value = [
      { name: 'Filename', value: file.name },
      { name: 'Path', value: file.path },
      { name: 'File Type', value: getFileTypeLabel(file) },
    ];

    // Add file metadata (use the Tauri fs plugin to get file info)
    const fullPath = `${editorStore.currentWorkspace.path}/${file.path}`;

    try {
      // Use lstat to get file metadata
      const metadata = await lstat(fullPath);

      if (metadata) {
        // Format creation time
        if (metadata.birthtime) {
          properties.value.push({
            name: 'Created',
            value: metadata.birthtime.toLocaleString()
          });
        }

        // Format modification time
        if (metadata.mtime) {
          properties.value.push({
            name: 'Modified',
            value: metadata.mtime.toLocaleString()
          });
        }

        // Last access time
        if (metadata.atime) {
          properties.value.push({
            name: 'Last Access',
            value: metadata.atime.toLocaleString()
          });
        }

        // File size
        properties.value.push({
          name: 'Size',
          value: formatFileSize(metadata.size)
        });

        // Is directory?
        properties.value.push({
          name: 'Type',
          value: metadata.isDirectory ? 'Directory' : metadata.isFile ? 'File' : 'Other'
        });

        // Readonly
        properties.value.push({
          name: 'Read Only',
          value: metadata.readonly ? 'Yes' : 'No'
        });
      }
    } catch (err) {
      await logError(`Failed to get file metadata: ${err}`);
    }

    // Add file type specific properties
    if (file.extension === 'md') {
      properties.value.push({ name: 'Type', value: 'Markdown Document' });
    } else if (file.extension === 'character') {
      properties.value.push({ name: 'Type', value: 'Character' });
    } else if (file.extension === 'canvas') {
      properties.value.push({ name: 'Type', value: 'Canvas' });
    }

  } catch (err) {
    await logError(`Failed to load file properties: ${err}`);
  } finally {
    isLoading.value = false;
  }
}

// Helper function to format file size
function formatFileSize(bytes: number): string {
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let size = bytes;
  let unitIndex = 0;

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024;
    unitIndex++;
  }

  return `${size.toFixed(1)} ${units[unitIndex]}`;
}

// Helper function to get file type label
function getFileTypeLabel(file: EditorFile): string {
  const extension = file.extension.toLowerCase();

  switch (extension) {
    case 'md':
      return 'Markdown';
    case 'character':
      return 'Character';
    case 'canvas':
      return 'Canvas';
    case 'json':
      return 'JSON';
    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'webp':
    case 'svg':
      return 'Image';
    default:
      return extension.toUpperCase();
  }
}

// Filter properties based on search
const filteredProperties = computed(() => {
  if (!searchQuery.value) return properties.value;

  const query = searchQuery.value.toLowerCase();
  return properties.value.filter(prop =>
    prop.name.toLowerCase().includes(query) ||
    prop.value.toLowerCase().includes(query)
  );
});
</script>

<template>
  <div class="w-64 border-l h-full flex flex-col">
    <!-- Inspector Header -->
    <div class="p-2 text-lg font-semibold border-b">
      Inspector
    </div>

    <!-- Search -->
    <div class="p-2 relative">
      <Input
        v-model="searchQuery"
        placeholder="Filter properties..."
        class="pl-8"
      />
      <Search class="h-4 w-4 absolute left-4 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
    </div>

    <!-- Properties -->
    <ScrollArea class="flex-1">
      <div class="p-2">
        <div v-if="isLoading" class="flex items-center justify-center h-20">
          <span class="text-muted-foreground">Loading properties...</span>
        </div>

        <div v-else-if="file && filteredProperties.length > 0">
          <div v-for="(property, index) in filteredProperties" :key="index" class="mb-3">
            <div class="text-xs text-muted-foreground">{{ property.name }}</div>
            <div>{{ property.value }}</div>
          </div>
        </div>

        <div v-else-if="file && searchQuery" class="flex items-center justify-center h-20 text-muted-foreground">
          No properties matching "{{ searchQuery }}"
        </div>

        <div v-else-if="!file" class="flex items-center justify-center h-full text-muted-foreground">
          Select a file to view properties
        </div>

        <div v-else class="flex items-center justify-center h-20 text-muted-foreground">
          No properties available for this file
        </div>
      </div>
    </ScrollArea>
  </div>
</template>
