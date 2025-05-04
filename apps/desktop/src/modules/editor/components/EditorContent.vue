<script setup lang="ts">
import {convertFileSrc} from '@tauri-apps/api/core';
import {error as logError, debug} from "@tauri-apps/plugin-log";
import {computed, ref, watch} from 'vue';
import {toast} from 'vue-sonner';
import { marked } from 'marked';
import {
  User,
  Home,
  PenTool,
  FileText,
  File,
  type LucideIcon,
  MapPin, BookOpen, ImageIcon
} from 'lucide-vue-next';
import {Textarea} from "@/components/ui/textarea";
import {useEditorStore} from '@editor/stores/editor.store';
import type {EditorFile, FileContent} from "@editor/types/editor.types.ts";
import TipTapEditor from './TipTapEditor.vue';

const props = defineProps<{
  file: EditorFile | null;
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
  // Treat Markdown as editable rich text now
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

function calculateAndUpdateStats(text: string | null) {
  if (text === null) {
    editorStore.updateFileStats(null);
    return;
  }

  const lines = text === '' ? 0 : text.split('\n').length;
  const words = text.trim() === '' ? 0 : text.trim().split(/\s+/).length;
  const characters = text.length;

  editorStore.updateFileStats({ lines, words, characters });
}


async function loadFileContent() {
  if (!props.file) {
    calculateAndUpdateStats(null);
    return;
  }

  isLoading.value = true;
  contentBody.value = ''; // Reset content
  frontmatterYaml.value = null;
  imagePath.value = null;

  try {
    const loadedContent = await editorStore.getFileContent(props.file.path);

    if (!loadedContent) {
      throw new Error('Received null content from store');
    }

    fileType.value = loadedContent.type;
    let textForInitialStats: string | null = null; // Text for immediate calculation
    let rawContentFromStore: string;

    switch (loadedContent.type) {
      case 'Markdown':
      case 'PlainText':
      case 'Character':
      case 'Location':
      case 'Lore':
        rawContentFromStore = editorStore.activeFileContent || '';
        frontmatterYaml.value = editorStore.activeFileFrontmatter;
        textForInitialStats = rawContentFromStore;
        break;
      case 'Canvas':
      case 'Dialogue':
        rawContentFromStore = editorStore.activeFileContent || '';
        frontmatterYaml.value = null;
        textForInitialStats = rawContentFromStore;
        break;
      case 'Image':
        imagePath.value = loadedContent.data.path || null;
        rawContentFromStore = '';
        frontmatterYaml.value = null;
        textForInitialStats = null;
        break;
      default:
        rawContentFromStore = '// Unsupported file type';
        fileType.value = 'PlainText';
        frontmatterYaml.value = null;
        editorStore.activeFileContent = rawContentFromStore;
        editorStore.activeFileFrontmatter = null;
        textForInitialStats = rawContentFromStore;
    }

    if (
      ['Markdown', 'Character', 'Location', 'Lore'].includes(
        loadedContent.type
      ) &&
      rawContentFromStore !== null
    ) {
      // Convert MD to HTML for TipTap display
      contentBody.value = marked.parse(rawContentFromStore) as string;
      await debug(
        `Converted Markdown to HTML for TipTap. Length: ${contentBody.value.length}`
      );
    } else if (rawContentFromStore !== null) {
      contentBody.value = rawContentFromStore;
    } else {
      contentBody.value = '';
    }

    calculateAndUpdateStats(textForInitialStats);

    await debug(
      `Loaded ${loadedContent.type}. Has frontmatter: ${!!frontmatterYaml.value}`
    );
  } catch (err) {
    console.error('Error loading file content:', err);
    toast.error('Failed to load file content', {
      description: String(err),
    });
    calculateAndUpdateStats(null);
    contentBody.value = marked.parse(
      `# Error Loading File\n\n\`\`\`\n${String(err)}\n\`\`\``
    ) as string;
    fileType.value = 'Markdown';
    frontmatterYaml.value = null;
    editorStore.activeFileContent = `# Error Loading File\n\n${String(err)}`;
    editorStore.activeFileFrontmatter = null;
  } finally {
    isLoading.value = false;
  }
}

function handleTextUpdate(plainText: string) {
  calculateAndUpdateStats(plainText);
  editorStore.activeFileContent = contentBody.value;
  editorStore.markTabAsChanged();
}

function handleJsonContentChange(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  const newContent = target.value;
  contentBody.value = newContent; // Update the ref bound to the textarea
  editorStore.activeFileContent = newContent; // Update store's raw content
  calculateAndUpdateStats(newContent); // Calculate stats for textarea content
  editorStore.markTabAsChanged();
}

watch(() => props.file, async (newFile) => {
    if (newFile) {
      await loadFileContent();
    } else {
      // Reset state when no file is selected
      contentBody.value = '';
      frontmatterYaml.value = null;
      imagePath.value = null;
      fileType.value = 'PlainText';
      isLoading.value = false;
      editorStore.activeFileContent = '';
      editorStore.activeFileFrontmatter = null;
      calculateAndUpdateStats(null);
    }
  },
  {immediate: true}
);

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
  return iconMap[iconName?.toLowerCase() || 'file'] || File;
}
</script>

<template>
  <div class="flex flex-col h-full bg-background">
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

    <div class="flex-1 overflow-y-auto pt-10 md:pt-12 px-4 pb-6">
      <div class="h-full">
        <div v-if="isLoading" class="flex items-center justify-center h-48">
          <div class="text-muted-foreground">Loading content...</div>
        </div>

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

        <div v-else-if="isEditableText" class="h-full">
          <TipTapEditor
            v-model="contentBody"
            @textUpdate="handleTextUpdate"
            class="h-full"
            placeholder="Start writing..."
            aria-label="File content editor"
          />
        </div>

        <div v-else-if="isJsonView" class="h-full">
            <Textarea
              :value="contentBody"
              @input="handleJsonContentChange"
              class="w-full h-full bg-muted/30 border border-input rounded-md p-4 font-mono text-sm focus:outline-none focus:ring-1 focus:ring-ring resize-none"
              placeholder="JSON data..."
              aria-label="JSON content viewer/editor"
            ></Textarea>
        </div>

        <div v-else class="text-center py-10 text-muted-foreground">
          <p>Cannot display content for this file type.</p>
        </div>
      </div>
    </div>
  </div>
</template>
