<script setup lang="ts">
import {convertFileSrc} from '@tauri-apps/api/core';
import {error as logError} from "@tauri-apps/plugin-log";
import {computed, onUnmounted, ref, watch} from 'vue';
import {toast} from 'vue-sonner';
import {
  User,
  Home,
  PenTool,
  FileText,
  File,
  type LucideIcon,
  MapPin, BookOpen, ImageIcon
} from 'lucide-vue-next';
import {useEditorStore} from '@editor/stores/editor.store';
import type {EditorFile, FileContent} from "@editor/types/editor.types.ts";

const props = defineProps<{
  file: EditorFile;
}>();

const editorStore = useEditorStore();

const contentBody = ref<string>('');
const frontmatterYaml = ref<string | null>(null); // Holds the raw frontmatter (read-only here)
const fileType = ref<FileContent['type']>('PlainText');
const imagePath = ref<string | null>(null);
const isLoading = ref(true);

const isEditableText = computed(() => {
  // Files with frontmatter have their content edited here
  // Plain Markdown and Text files are also edited here
  return ['Markdown', 'Character', 'Location', 'Lore', 'PlainText'].includes(
    fileType.value
  );
});

const isJsonView = computed(() => {
  // Canvas and potentially Dialogue (if JSON) might just display raw JSON here
  // Or have dedicated components later
  return ['Canvas', 'Dialogue'].includes(fileType.value);
});

const isImageView = computed(() => fileType.value === 'Image');

const imageUrl = computed(() => {
  if (isImageView.value && imagePath.value) {
    try {
      return convertFileSrc(imagePath.value);
    } catch (e) {
      logError(`Error converting file source: ${e}`);
      toast.error("Could not load image asset.");
      return null; // Return null or a placeholder on error
    }
  }
  return null;
});


async function loadFileContent() {
  if (!props.file) return;

  isLoading.value = true;
  contentBody.value = ''; // Reset content
  frontmatterYaml.value = null;
  imagePath.value = null;

  try {
    const loadedContent = await editorStore.getFileContent(props.file.path);

    if (!loadedContent) {
      throw new Error('Received null content from store');
    }

    fileType.value = loadedContent.type; // Store the type

    // Update store's temporary active file state
    editorStore.activeFileContent = '';
    editorStore.activeFileFrontmatter = null;

    // Extract data based on type
    switch (loadedContent.type) {
      case 'Markdown':
      case 'PlainText':
        contentBody.value = loadedContent.data.content || '';
        editorStore.activeFileContent = contentBody.value; // Update store
        break;
      case 'Character':
      case 'Location':
      case 'Lore':
        contentBody.value = loadedContent.data.content || '';
        frontmatterYaml.value = loadedContent.data.frontmatter; // Store FM (read-only)
        editorStore.activeFileContent = contentBody.value; // Update store
        editorStore.activeFileFrontmatter = frontmatterYaml.value; // Update store
        break;
      case 'Canvas':
      case 'Dialogue': // Assuming Dialogue might be JSON string for now
        contentBody.value = loadedContent.data.data || ''; // Store JSON string
        editorStore.activeFileContent = contentBody.value; // Update store (as JSON string)
        break;
      case 'Image':
        imagePath.value = loadedContent.data.path || null;
        // No text content or frontmatter for images
        break;
      default:
        contentBody.value = '// Unsupported file type';
        fileType.value = 'PlainText'; // Fallback
    }

    // Reset unsaved changes flag in the store *after* loading
    // Note: This might conflict if Inspector loads slower/faster.
    // The central save mechanism should be the source of truth for saving.
    // Let's rely on the initial load not marking as changed.
    // props.file.hasUnsavedChanges = false; // Let store handle this on save

  } catch (err) {
    console.error('Error loading file content:', err);
    toast.error('Failed to load file content', {description: String(err)});
    contentBody.value = `# Error Loading File\n\n${String(err)}`;
    fileType.value = 'PlainText'; // Fallback on error
    editorStore.activeFileContent = contentBody.value; // Update store with error message
    editorStore.activeFileFrontmatter = null;
  } finally {
    isLoading.value = false;
  }
}

function handleContentInput(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  contentBody.value = target.value;
  editorStore.activeFileContent = contentBody.value;
  editorStore.markTabAsChanged();
}

// Watch for file changes
watch(() => props.file, async (newFile) => {
  if (newFile) {
    await loadFileContent();
  }
}, {immediate: true});

function getIconComponent(iconName: string | undefined): LucideIcon {
  const iconMap: Record<string, LucideIcon> = {
    user: User,
    character: User,
    home: Home,
    location: MapPin,
    pentool: PenTool,
    canvas: PenTool,
    book: BookOpen,
    lore: BookOpen,
    map: MapPin,
    filetext: FileText,
    markdown: FileText,
    image: ImageIcon,
    file: File,
    dialogue: FileText,
  };
  // Use lowercase, handle undefined, provide default
  return iconMap[iconName?.toLowerCase() || 'file'] || File;
}

watch(() => props.file, async (newFile) => {
    if (newFile) {
      await loadFileContent();
    } else {
      contentBody.value = '';
      frontmatterYaml.value = null;
      imagePath.value = null;
      fileType.value = 'PlainText';
      isLoading.value = false;
      editorStore.activeFileContent = '';
      editorStore.activeFileFrontmatter = null;
    }
  },
  {immediate: true}
);

onUnmounted(() => {
  editorStore.activeFileContent = '';
  editorStore.activeFileFrontmatter = null;
});
</script>

<template>
  <div class="flex flex-col h-full bg-background">
    <!-- Header section (Simplified - Inspector shows more details) -->
    <div
      v-if="file"
      class="w-full h-24 md:h-32 bg-muted/30 flex-shrink-0 relative border-b"
    >
      <div
        class="absolute bottom-0 left-4 md:left-10 transform translate-y-1/2 flex items-center gap-4 z-10"
      >
        <div
          class="w-12 h-12 md:w-16 md:h-16 rounded-lg bg-background/95 backdrop-blur-sm border shadow-md flex items-center justify-center p-2 flex-shrink-0"
        >
          <component
            :is="getIconComponent(file.icon)"
            class="h-full w-full text-foreground"
            :stroke-width="1.5"
          />
        </div>
        <div class="pt-6 md:pt-8 min-w-0">
          <h1 class="text-lg md:text-xl font-bold text-foreground truncate" :title="file.name">
            {{ file.name }}
          </h1>
          <p class="text-xs text-muted-foreground truncate" :title="file.path">{{ file.path }}</p>
        </div>
      </div>
    </div>
    <div v-else class="h-24 md:h-32 bg-muted/30 flex-shrink-0 border-b">
      <div class="flex items-center justify-center h-full">
        <span class="text-muted-foreground">No file selected</span>
      </div>
    </div>

    <!-- Content area -->
    <div class="flex-1 overflow-y-auto pt-10 md:pt-12 px-4 pb-6">
      <div class="max-w-5xl mx-auto">
        <!-- Loading state -->
        <div v-if="isLoading" class="flex items-center justify-center h-48">
          <div class="text-muted-foreground">Loading content...</div>
        </div>

        <!-- Image View -->
        <div
          v-else-if="isImageView && imageUrl"
          class="flex items-center justify-center"
        >
          <img
            :src="imageUrl"
            :alt="file?.name || 'Image'"
            class="max-w-full max-h-[70vh] object-contain rounded border shadow-sm"
          />
        </div>

        <div v-else-if="isImageView && !imageUrl"
             class="text-center py-10 text-destructive-foreground">
          Could not load image. Check path or permissions.
        </div>

        <!-- Editable Text Area (Markdown, Character Content, Lore Content, etc.) -->
        <div v-else-if="isEditableText">
          <textarea
            :value="contentBody"
            @input="handleContentInput"
            class="w-full min-h-[calc(100vh-250px)] bg-transparent border-none focus:outline-none focus:ring-0 font-mono text-sm leading-relaxed resize-none p-2"
            placeholder="Start writing..."
            aria-label="File content editor"
          ></textarea>
        </div>

        <!-- JSON View (Canvas, potentially Dialogue) -->
        <div v-else-if="isJsonView">
           <textarea
             :value="contentBody"
             @input="handleContentInput"
             class="w-full min-h-[calc(100vh-250px)] bg-muted/30 border border-input rounded-md p-4 font-mono text-sm focus:outline-none focus:ring-1 focus:ring-ring resize-none"
             placeholder="JSON data..."
             aria-label="JSON content viewer/editor"
           ></textarea>
          <!-- <pre class="text-xs p-4 bg-muted/50 rounded overflow-auto">{{ contentBody }}</pre> -->
          <!-- TODO: Add a proper JSON editor/viewer later -->
        </div>

        <!-- Fallback for unknown/unsupported types -->
        <div v-else class="text-center py-10 text-muted-foreground">
          <p>Cannot display content for this file type.</p>
        </div>
      </div>
    </div>
  </div>
</template>
