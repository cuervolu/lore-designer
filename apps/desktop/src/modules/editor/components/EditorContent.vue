<!-- apps/desktop/src/modules/editor/components/EditorContent.vue -->
<script setup lang="ts">
import {ref, watch} from 'vue';
import {User, Home, PenTool, FileText, Image, File, type LucideIcon, Save} from 'lucide-vue-next';
import {Button} from '@/components/ui/button';
import {useEditorStore} from '@editor/stores/editor.store';
import {toast} from 'vue-sonner';
import type {EditorFile} from "@editor/types/editor.types.ts";

const props = defineProps<{
  file: EditorFile;
}>();

const editorStore = useEditorStore();
const content = ref<string>('');
const isLoading = ref(true);
const fileType = ref<string>('text');
const hasUnsavedChanges = ref(false);

// Load file content when component mounts or file changes
async function loadFileContent() {
  if (!props.file) return;

  isLoading.value = true;

  try {
    const fileContent = await editorStore.getFileContent(props.file.path);

    // Handle different file types
    if (fileContent.type === 'Markdown') {
      content.value = fileContent.data.content || '';
      fileType.value = 'markdown';
    } else if (fileContent.type === 'Canvas') {
      content.value = fileContent.data.data || '{}';
      fileType.value = 'canvas';
    } else if (fileContent.type === 'Character') {
      content.value = fileContent.data.data || '{}';
      fileType.value = 'character';
    } else if (fileContent.type === 'Image') {
      fileType.value = 'image';
      // Image path is handled in template
    } else {
      content.value = fileContent.data.content || '';
      fileType.value = 'text';
    }

    // Reset unsaved changes flag
    hasUnsavedChanges.value = false;

  } catch (err) {
    console.error('Error loading file content:', err);

    // Show error toast
    toast.error('Failed to load file content', {
      description: String(err)
    });

    // Set default content for error state
    content.value = '# Error Loading File\n\nThere was an error loading this file.';
    fileType.value = 'text';
  } finally {
    isLoading.value = false;
  }
}

// Save file content
async function saveContent() {
  if (!props.file) return;

  try {
    const isJson = ['canvas', 'character'].includes(fileType.value);
    await editorStore.saveFileContent(props.file.path, content.value, isJson);
    hasUnsavedChanges.value = false;
  } catch (err) {
    console.error('Error saving file:', err);
  }
}

// Handle content change
function handleContentChange(e: Event) {
  const target = e.target as HTMLTextAreaElement;
  content.value = target.value;
  hasUnsavedChanges.value = true;

  // Update file status in store
  props.file.hasUnsavedChanges = true;
}

// Watch for file changes
watch(() => props.file, async (newFile) => {
  if (newFile) {
    await loadFileContent();
  }
}, {immediate: true});

// Get icon component for file
function getIconComponent(iconName: string): LucideIcon {
  const iconMap: Record<string, LucideIcon> = {
    'user': User,
    'character': User,
    'home': Home,
    'penTool': PenTool,
    'canvas': PenTool,
    'fileText': FileText,
    'markdown': FileText,
    'image': Image,
    'file': File
  };

  return iconMap[iconName] || FileText;
}

// Render markdown (simple implementation)
function renderMarkdown(markdown: string | null): string {
  if (!markdown) return "";

  return markdown
  .split("\n\n")
  .map((p) => {
    if (p.startsWith("## ")) {
      return `<h2 class="text-xl font-semibold mt-6 mb-2">${p.substring(3)}</h2>`;
    }
    if (p.startsWith("# ")) {
      return `<h1 class="text-2xl font-bold mt-8 mb-4">${p.substring(2)}</h1>`;
    }
    return `<p class="mb-4">${p}</p>`;
  })
  .join("");
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- Header section with file info -->
    <div class="w-full h-48 bg-muted/30 flex-shrink-0 relative group">
      <div class="absolute inset-0 flex items-center justify-center">
        <div class="w-full h-full bg-gradient-to-b from-transparent to-muted/30"></div>
      </div>

      <div
        class="absolute bottom-0 left-10 transform translate-y-1/2 flex flex-col items-start z-10">
        <div
          class="w-16 h-16 rounded-lg bg-background/95 backdrop-blur-sm border shadow-md flex items-center justify-center p-2">
          <component
            :is="getIconComponent(file.icon)"
            class="h-full w-full text-foreground"
          />
        </div>
        <h1 class="mt-2 text-xl font-bold text-foreground">
          {{ file.name }}
          <span class="text-muted-foreground font-normal">.{{ file.extension }}</span>
        </h1>
      </div>

      <!-- Save button -->
      <Button
        v-if="hasUnsavedChanges"
        variant="outline"
        class="absolute bottom-4 right-4 opacity-90 group-hover:opacity-100 transition-opacity"
        @click="saveContent"
      >
        <Save class="h-4 w-4 mr-2"/>
        Save
      </Button>
    </div>

    <!-- Content area -->
    <div class="flex-1 overflow-y-auto pt-16 px-6 pb-6">
      <div class="max-w-5xl mx-auto">
        <!-- Loading state -->
        <div v-if="isLoading" class="flex items-center justify-center h-48">
          <div class="text-muted-foreground">Loading content...</div>
        </div>

        <!-- Canvas content -->
        <div
          v-else-if="fileType === 'canvas'"
          class="border rounded-md p-4 min-h-[400px] flex items-center justify-center bg-muted/10"
        >
          <div v-if="content" class="flex items-center gap-8">
            <div class="w-32 h-32 bg-foreground rounded-full"></div>
            <div
              class="w-40 h-40 bg-foreground"
              style="clip-path: polygon(50% 0%, 0% 100%, 100% 100%)"
            ></div>
          </div>
          <div v-else class="text-muted-foreground">
            Canvas is empty. Start adding content.
          </div>
        </div>

        <!-- Image content -->
        <div
          v-else-if="fileType === 'image'"
          class="flex items-center justify-center"
        >
          <img :src="file.path" alt="Image" class="max-w-full max-h-[80vh] object-contain"/>
        </div>

        <!-- Editable text content -->
        <div v-else-if="['text', 'markdown'].includes(fileType)" class="flex flex-col gap-4">
          <!-- Preview mode (disabled by default) -->
          <div v-if="false" class="prose dark:prose-invert lg:prose-xl max-w-none"
               v-html="renderMarkdown(content)"></div>

          <!-- Edit mode -->
          <textarea
            v-model="content"
            class="w-full h-[calc(100vh-300px)] bg-background border border-input rounded-md p-4 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-ring"
            @input="handleContentChange"
          ></textarea>
        </div>

        <!-- Character content -->
        <div
          v-else-if="fileType === 'character'"
          class="border rounded-md p-4 min-h-[400px] bg-muted/10"
        >
          <textarea
            v-model="content"
            class="w-full h-[calc(100vh-300px)] bg-background border border-input rounded-md p-4 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-ring"
            @input="handleContentChange"
          ></textarea>
        </div>

        <!-- Unknown content type -->
        <div v-else class="text-center py-10 text-muted-foreground">
          <p>Unknown file type.</p>
        </div>
      </div>
    </div>
  </div>
</template>
