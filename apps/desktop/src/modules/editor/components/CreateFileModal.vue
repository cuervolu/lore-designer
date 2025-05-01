<script setup lang="ts">
import {ref} from 'vue';
import {FileText, User, MapIcon, Book, PenTool, File, type LucideIcon} from 'lucide-vue-next';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {ScrollArea} from '@/components/ui/scroll-area';
import {useEditorStore} from '@editor/stores/editor.store';

interface FileTypeOption {
  id: string;
  name: string;
  extension: string;
  icon: LucideIcon;
  description: string;
  initialContent: string;
}

const props = defineProps<{
  isOpen: boolean;
  parentPath: string;
}>();

const emit = defineEmits<{
  'update:isOpen': [value: boolean];
  'create': [fileName: string, extension: string, initialContent: string, parentPath: string];
}>();

const editorStore = useEditorStore();
const fileName = ref('');
const selectedType = ref<string>('markdown');
const errorMessage = ref('');
const isCreating = ref(false);

// Define the available file types
const fileTypes: FileTypeOption[] = [
  {
    id: 'markdown',
    name: 'Markdown',
    extension: 'md',
    icon: FileText,
    description: 'General purpose document with rich text formatting',
    initialContent: '# Untitled Document\n\nWrite your content here...'
  },
  {
    id: 'character',
    name: 'Character',
    extension: 'character.md',
    icon: User,
    description: 'Character sheet with details, background, and relationships',
    initialContent: '# Character Sheet\n\n## Name\n\n## Description\n\n## Background\n\n## Relationships\n'
  },
  {
    id: 'location',
    name: 'Location',
    extension: 'location.json',
    icon: MapIcon,
    description: 'Place or setting with details and connections',
    initialContent: JSON.stringify({
      name: '',
      description: '',
      features: [],
      connections: []
    }, null, 2)
  },
  {
    id: 'lore',
    name: 'Lore Entry',
    extension: 'lore.md',
    icon: Book,
    description: 'World-building element like history, culture, or technology',
    initialContent: '# Lore Entry\n\n## Overview\n\n## Details\n\n## Significance\n'
  },
  {
    id: 'canvas',
    name: 'Canvas',
    extension: 'canvas.json',
    icon: PenTool,
    description: 'Visual workspace for mind mapping and relationship diagrams',
    initialContent: JSON.stringify({
      nodes: [],
      edges: [],
      metadata: {
        name: '',
        description: '',
        created: new Date().toISOString()
      }
    }, null, 2)
  },
  {
    id: 'custom',
    name: 'Custom File',
    extension: '',
    icon: File,
    description: 'Create a file with a custom extension',
    initialContent: ''
  }
];

const closeModal = () => {
  fileName.value = '';
  selectedType.value = 'markdown';
  errorMessage.value = '';
  emit('update:isOpen', false);
};

const createFile = async () => {
  if (isCreating.value) return;

  if (!fileName.value.trim()) {
    errorMessage.value = 'Please enter a file name.';
    return;
  }
  const fileType = fileTypes.find(t => t.id === selectedType.value);
  if (!fileType) {
    errorMessage.value = 'Please select a file type.';
    return;
  }

  let finalFileName = fileName.value;
  let extension = fileType.extension;

  // For custom file, get extension from the file name if provided
  if (fileType.id === 'custom') {
    const parts = fileName.value.split('.');

    if (parts.length > 1) {
      // The user included an extension, so use it
      extension = parts.slice(1).join('.');
      finalFileName = parts[0];
    } else {
      errorMessage.value = 'Please include a file extension for custom files.';
      return;
    }
  } else {
    // Make sure the file name doesn't already have the extension
    if (finalFileName.endsWith(`.${extension}`)) {
      finalFileName = finalFileName.slice(0, -(extension.length + 1));
    }
  }

  // Validate filename (no special characters)
  if (!/^[a-zA-Z0-9_\- ]+$/.test(finalFileName)) {
    errorMessage.value = 'File name can only contain letters, numbers, underscores, hyphens, and spaces.';
    return;
  }

  // Create the full file name
  const fullFileName = fileType.id === 'custom'
    ? `${finalFileName}.${extension}`
    : `${finalFileName}.${extension}`;

  try {
    isCreating.value = true;
    emit('create', fullFileName, extension, fileType.initialContent, props.parentPath);

    // Wait a bit to ensure file is created, then refresh the file tree
    setTimeout(async () => {
      await editorStore.refreshFileTree();
      isCreating.value = false;
    }, 500);

    closeModal();
  } catch (error) {
    isCreating.value = false;
    errorMessage.value = `Error creating file: ${error}`;
  }
};

// Get a displayable path name
const getPathDisplay = (path: string) => {
  if (!path || path === '/') return 'Root';
  return path;
};
</script>

<template>
  <Dialog :open="isOpen" @update:open="emit('update:isOpen', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>Create New File</DialogTitle>
      </DialogHeader>

      <div class="py-4">
        <div class="mb-4">
          <label class="text-sm font-medium mb-1 block">Location</label>
          <div class="bg-muted/30 p-2 rounded text-sm">
            {{ getPathDisplay(parentPath) }}
          </div>
        </div>

        <div class="mb-4">
          <label for="fileName" class="text-sm font-medium mb-1 block">File Name</label>
          <Input
            id="fileName"
            v-model="fileName"
            placeholder="Enter file name"
            class="mb-1"
            @keydown.enter="createFile"
          />
          <p class="text-xs text-muted-foreground">
            File will be saved as: <span class="font-mono">{{
              fileName || 'filename'
            }}{{
              selectedType !== 'custom' ? '.' + fileTypes.find(t => t.id === selectedType)?.extension : ''
            }}</span>
          </p>
        </div>

        <div class="mb-4">
          <label class="text-sm font-medium mb-2 block">File Type</label>
          <ScrollArea class="h-60 rounded-md border">
            <div class="p-1">
              <div
                v-for="type in fileTypes"
                :key="type.id"
                class="flex items-start p-2 rounded-md cursor-pointer hover:bg-accent gap-3"
                :class="{ 'bg-accent': selectedType === type.id }"
                @click="selectedType = type.id"
              >
                <div class="bg-muted/40 p-2 rounded-md">
                  <component :is="type.icon" class="h-5 w-5"/>
                </div>
                <div>
                  <h4 class="font-medium">{{ type.name }}</h4>
                  <p class="text-xs text-muted-foreground">{{ type.description }}</p>
                </div>
              </div>
            </div>
          </ScrollArea>
        </div>

        <p v-if="errorMessage" class="text-sm text-destructive mb-4">
          {{ errorMessage }}
        </p>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="closeModal" :disabled="isCreating">Cancel</Button>
        <Button @click="createFile" :disabled="isCreating">
          <span v-if="isCreating">Creating...</span>
          <span v-else>Create</span>
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
