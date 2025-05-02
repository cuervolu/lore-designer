<script setup lang="ts">
import {error as logError} from "@tauri-apps/plugin-log";
import {ref, computed} from 'vue';
import {
  FileText, User, MapPin, BookOpen, PenTool, type LucideIcon
} from 'lucide-vue-next';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription
} from '@/components/ui/dialog';
import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {ScrollArea} from '@/components/ui/scroll-area';
import {useEditorStore} from '@editor/stores/editor.store';
import {toast} from 'vue-sonner';

interface FileTypeOption {
  id: string; // Should match backend FileType enum name for templates
  name: string;
  // Extension is now often derived or handled by backend template command
  // Keep it for display/suggestion if needed
  suggestedExtension: string;
  icon: LucideIcon;
  description: string;
  isTemplate: boolean; // Flag template types
  initialContent?: string; // Only for non-template types
}

const props = defineProps<{
  isOpen: boolean;
  parentPath: string;
}>();

const emit = defineEmits<{
  'update:isOpen': [value: boolean];
}>();

const editorStore = useEditorStore();
const fileName = ref('');
const selectedTypeId = ref<string>('Markdown');
const errorMessage = ref('');
const isCreating = ref(false);

// Define the available file types - IDs match backend FileType enum names
const fileTypes: FileTypeOption[] = [
  {
    id: 'Markdown',
    name: 'Markdown Document',
    suggestedExtension: '.md',
    icon: FileText,
    description: 'General purpose document (notes, story chapters).',
    isTemplate: false,
    initialContent: '# Untitled Document\n\n'
  },
  {
    id: 'Character',
    name: 'Character',
    suggestedExtension: '.character.md',
    icon: User,
    description: 'Character sheet using frontmatter template.',
    isTemplate: true,
  },
  {
    id: 'Location',
    name: 'Location',
    suggestedExtension: '.location.md',
    icon: MapPin,
    description: 'Location details using frontmatter template.',
    isTemplate: true,
  },
  {
    id: 'Lore',
    name: 'Lore Entry',
    suggestedExtension: '.lore.md',
    icon: BookOpen,
    description: 'World-building element using frontmatter template.',
    isTemplate: true,
  },
  {
    id: 'Canvas',
    name: 'Canvas',
    suggestedExtension: '.canvas.json',
    icon: PenTool,
    description: 'Visual workspace for mind mapping (JSON format).',
    isTemplate: false,
    initialContent: JSON.stringify({nodes: [], edges: []}, null, 2)
  },
  {
    id: 'Dialogue',
    name: 'Dialogue (Simple)',
    suggestedExtension: '.dialogue.md',
    icon: FileText,
    description: 'Simple linear dialogue script (Markdown).',
    isTemplate: false,
    initialContent: '# Dialogue\n\nCharacter A: Hello!\nCharacter B: Hi there.\n'
  },
];

const selectedType = computed(() => fileTypes.find(t => t.id === selectedTypeId.value));

const suggestedFileName = computed(() => {
  if (!fileName.value.trim() || !selectedType.value) {
    return `filename${selectedType.value?.suggestedExtension || ''}`;
  }
  let name = fileName.value.trim();
  const ext = selectedType.value.suggestedExtension;

  if (ext && name.toLowerCase().endsWith(ext.toLowerCase())) {
    name = name.slice(0, -(ext.length));
  }
  return name + ext;
});


const closeModal = () => {
  if (isCreating.value) return;
  fileName.value = '';
  selectedTypeId.value = 'Markdown';
  errorMessage.value = '';
  emit('update:isOpen', false);
};

const createFile = async () => {
  if (isCreating.value) return;
  errorMessage.value = '';

  const name = fileName.value.trim();
  if (!name) {
    errorMessage.value = 'Please enter a file name.';
    return;
  }

  const typeInfo = selectedType.value;
  if (!typeInfo) {
    errorMessage.value = 'Invalid file type selected.'; // Should not happen
    return;
  }

  // Basic filename validation (allow spaces, hyphens, underscores, letters, numbers)
  // Prevent path traversal or invalid chars
  if (!/^[a-zA-Z0-9_\-\s]+$/.test(name)) {
    errorMessage.value = 'File name contains invalid characters.';
    return;
  }

  isCreating.value = true;
  let createdPath: string | null = null;

  try {
    if (typeInfo.isTemplate) {
      createdPath = await editorStore.createFromTemplate(
        props.parentPath,
        name,
        typeInfo.id
      );
    } else {
      const fullFileName = name + typeInfo.suggestedExtension;
      createdPath = await editorStore.createNewFile(
        props.parentPath,
        fullFileName,
        typeInfo.initialContent || ''
      );
    }

    if (createdPath) {
      closeModal();
    } else {
      errorMessage.value = 'Failed to create file. Check logs for details.';
    }

  } catch (error: any) {
    await logError(`Error in CreateFileModal createFile: ${error}`);
    errorMessage.value = `Error: ${error.message || 'Failed to create file.'}`;
    toast.error("File Creation Failed", {description: errorMessage.value});
  } finally {
    isCreating.value = false;
  }
};

const getPathDisplay = (path: string) => {
  if (!path || path === '/' || path === '.') return 'Workspace Root';
  // Remove leading/trailing slashes for display
  return path.replace(/^[\\/]+|[\\/]+$/g, '');
};
</script>

<template>
  <Dialog :open="isOpen" @update:open="closeModal">
    <DialogContent class="sm:max-w-lg">
      <DialogHeader>
        <DialogTitle>Create New File</DialogTitle>
        <DialogDescription>
          Select a file type and enter a name. Files will be created in:
          <span class="font-mono text-xs bg-muted/50 px-1 py-0.5 rounded">{{
              getPathDisplay(parentPath)
            }}</span>
        </DialogDescription>
      </DialogHeader>

      <div class="py-4 grid gap-4">
        <!-- File Name Input -->
        <div>
          <label for="fileName" class="text-sm font-medium mb-1 block">File Name</label>
          <Input
            id="fileName"
            v-model="fileName"
            placeholder="Enter file name (without extension)"
            class="mb-1"
            @keydown.enter="createFile"
            :disabled="isCreating"
          />
          <p class="text-xs text-muted-foreground h-4">
            Will be created as: <span class="font-mono">{{ suggestedFileName }}</span>
          </p>
        </div>

        <!-- File Type Selection -->
        <div>
          <label class="text-sm font-medium mb-2 block">File Type</label>
          <ScrollArea class="h-60 rounded-md border">
            <div class="p-1 space-y-1">
              <div
                v-for="type in fileTypes"
                :key="type.id"
                class="flex items-start p-2 rounded-md cursor-pointer hover:bg-accent gap-3 border"
                :class="{
                    'bg-accent border-accent-foreground/30': selectedTypeId === type.id,
                    'border-transparent': selectedTypeId !== type.id
                 }"
                @click="selectedTypeId = type.id"
              >
                <div class="bg-muted/40 p-1.5 rounded-md mt-0.5">
                  <component :is="type.icon" class="h-4 w-4"/>
                </div>
                <div class="flex-1">
                  <h4 class="font-medium text-sm leading-tight">{{ type.name }}</h4>
                  <p class="text-xs text-muted-foreground leading-snug">{{ type.description }}</p>
                </div>
              </div>
            </div>
          </ScrollArea>
        </div>

        <p v-if="errorMessage" class="text-sm text-destructive">
          {{ errorMessage }}
        </p>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="closeModal" :disabled="isCreating">Cancel</Button>
        <Button @click="createFile" :disabled="isCreating || !fileName.trim()">
          <span v-if="isCreating">Creating...</span>
          <span v-else>Create File</span>
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
