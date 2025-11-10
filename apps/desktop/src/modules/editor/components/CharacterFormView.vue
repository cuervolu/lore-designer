<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { toast } from 'vue-sonner';
import { Plus, Trash2, Upload, User, X } from 'lucide-vue-next';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import MilkdownEditor from '@editor/components/MilkdownEditor.vue';
import { useEditorStore } from '@editor/stores/editor.store';
import { useAutoSave } from '@editor/composables/useAutoSave';
import type { EditorFile } from '@editor/types/editor.types';
import { parse, stringify } from 'yaml';

const props = defineProps<{
  file: EditorFile;
}>();

const editorStore = useEditorStore();
const { saveNow, isSaving } = useAutoSave();


const bodyContent = ref('');

const avatarPreview = ref<string | null>(null);
const formData = ref({
  name: '',
  aliases: [] as string[],
  age: null as number | null,
  family: '',
  status: '',
  role: '',
  gender: '',
  species: '',
  occupation: '',
  customProperties: [] as Array<{ key: string; value: string }>,
});

const isUpdatingFromStore = ref(false);
let saveTimeout: ReturnType<typeof setTimeout> | null = null;

function parseFrontmatter() {
  const frontmatter = editorStore.activeFileFrontmatter;
  if (!frontmatter) {
    resetForm();
    return;
  }

  try {
    isUpdatingFromStore.value = true;
    const parsed = parse(frontmatter) as any;

    formData.value.name = parsed.name || '';
    formData.value.aliases = Array.isArray(parsed.aliases) ? parsed.aliases : [];
    formData.value.age = parsed.age || null;
    formData.value.family = parsed.family || '';
    formData.value.status = parsed.status || '';
    formData.value.role = parsed.role || '';
    formData.value.gender = parsed.gender || '';
    formData.value.species = parsed.species || '';
    formData.value.occupation = parsed.occupation || '';

    if (parsed.avatar) {
      avatarPreview.value = parsed.avatar;
    }

    if (parsed.custom_properties && Array.isArray(parsed.custom_properties)) {
      formData.value.customProperties = parsed.custom_properties.map((prop: any) => ({
        key: prop.key || '',
        value: prop.value || '',
      }));
    }

    // Body content viene del activeFileContent
    bodyContent.value = editorStore.activeFileContent || '';
  } catch (error) {
    toast.error('Failed to parse character frontmatter');
  } finally {
    setTimeout(() => {
      isUpdatingFromStore.value = false;
    }, 100);
  }
}

function resetForm() {
  formData.value = {
    name: '',
    aliases: [],
    age: null,
    family: '',
    status: '',
    role: '',
    gender: '',
    species: '',
    occupation: '',
    customProperties: [],
  };
  bodyContent.value = '';
  avatarPreview.value = null;
}

function serializeToYaml(): string {
  const data: any = {
    name: formData.value.name || '',
    aliases: formData.value.aliases.filter((a) => a.trim()),
    age: formData.value.age,
    family: formData.value.family || '',
    status: formData.value.status || '',
    role: formData.value.role || '',
    gender: formData.value.gender || '',
    species: formData.value.species || '',
    occupation: formData.value.occupation || '',
  };

  if (avatarPreview.value) {
    data.avatar = avatarPreview.value;
  }

  if (formData.value.customProperties.length > 0) {
    data.custom_properties = formData.value.customProperties
      .filter((prop) => prop.key.trim())
      .map((prop) => ({
        key: prop.key,
        value: prop.value,
      }));
  }

  return stringify(data, { indent: 2 });
}

function saveChanges() {
  if (isUpdatingFromStore.value) return;

  try {
    editorStore.activeFileFrontmatter = serializeToYaml();
    editorStore.activeFileContent = bodyContent.value;
    editorStore.markTabAsChanged();
  } catch (error) {
    console.error('Error saving changes:', error);
    toast.error('Failed to update character');
  }
}

function saveChangesDebounced() {
  if (saveTimeout) {
    clearTimeout(saveTimeout);
  }
  saveTimeout = setTimeout(() => {
    saveChanges();
  }, 500);
}

function handleAvatarUpload(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];

  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      avatarPreview.value = e.target?.result as string;
      saveChanges();
    };
    reader.readAsDataURL(file);
  }
}

function removeAvatar() {
  avatarPreview.value = null;
  saveChanges();
}

const newAlias = ref('');

function addAlias() {
  if (newAlias.value.trim()) {
    formData.value.aliases.push(newAlias.value.trim());
    newAlias.value = '';
    saveChanges();
  }
}

function removeAlias(index: number) {
  formData.value.aliases.splice(index, 1);
  saveChanges();
}

function addCustomProperty() {
  formData.value.customProperties.push({ key: '', value: '' });
}

function removeCustomProperty(index: number) {
  formData.value.customProperties.splice(index, 1);
  saveChanges();
}

function handleBodyUpdate(text: string) {
  if (isUpdatingFromStore.value) return;
  bodyContent.value = text;
  saveChangesDebounced();
}

watch(
  () => props.file,
  () => {
    parseFrontmatter();
  },
  { immediate: true }
);

onMounted(() => {
  parseFrontmatter();
});
</script>

<template>
  <div class="h-full w-full overflow-y-auto bg-background">
    <div class="max-w-5xl mx-auto px-6 py-8">
      <!-- Hero Section con Avatar -->
      <div class="flex gap-8 items-start mb-8">
        <!-- Columna Izquierda: Campos -->
        <div class="flex-1 space-y-6">
          <!-- Nombre Grande -->
          <div>
            <Input
              v-model="formData.name"
              @input="saveChanges"
              placeholder="Character Name"
              class="text-4xl font-bold h-auto py-3 border-0 border-b-2 border-border rounded-none bg-transparent focus-visible:ring-0 focus-visible:border-primary"
            />
          </div>

          <!-- Aliases -->
          <div class="flex items-center gap-2 flex-wrap">
            <User class="h-4 w-4 text-muted-foreground" />
            <span class="text-sm text-muted-foreground">Aliases:</span>
            <button
              v-for="(alias, index) in formData.aliases"
              :key="index"
              @click="removeAlias(index)"
              class="px-3 py-1 text-sm bg-muted hover:bg-muted/70 rounded-full flex items-center gap-1 group"
            >
              {{ alias }}
              <X class="h-3 w-3 opacity-0 group-hover:opacity-100 transition-opacity" />
            </button>
            <Input
              v-model="newAlias"
              @keydown.enter="addAlias"
              placeholder="+ Add"
              class="w-32 h-7 text-sm border-dashed"
            />
          </div>

          <!-- Grid de Campos Básicos -->
          <div class="grid grid-cols-2 gap-4 pt-4">
            <div>
              <label class="text-xs text-muted-foreground mb-1 block"># Age:</label>
              <Input
                v-model.number="formData.age"
                @input="saveChanges"
                type="number"
                placeholder="15"
                class="h-9"
              />
            </div>

            <div>
              <label class="text-xs text-muted-foreground mb-1 block">† Family:</label>
              <Input
                v-model="formData.family"
                @input="saveChanges"
                placeholder="Select Family..."
                class="h-9"
              />
            </div>

            <div>
              <label class="text-xs text-muted-foreground mb-1 block">Role:</label>
              <Input
                v-model="formData.role"
                @input="saveChanges"
                placeholder="e.g., protagonist"
                class="h-9"
              />
            </div>

            <div>
              <label class="text-xs text-muted-foreground mb-1 block">Status:</label>
              <Select v-model="formData.status" @update:model-value="saveChanges">
                <SelectTrigger class="h-9">
                  <SelectValue placeholder="Select status..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="alive">Alive</SelectItem>
                  <SelectItem value="deceased">Deceased</SelectItem>
                  <SelectItem value="unknown">Unknown</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div>
              <label class="text-xs text-muted-foreground mb-1 block">Gender:</label>
              <Input
                v-model="formData.gender"
                @input="saveChanges"
                placeholder="Gender"
                class="h-9"
              />
            </div>

            <div>
              <label class="text-xs text-muted-foreground mb-1 block">Species:</label>
              <Input
                v-model="formData.species"
                @input="saveChanges"
                placeholder="e.g., human, elf"
                class="h-9"
              />
            </div>

            <div class="col-span-2">
              <label class="text-xs text-muted-foreground mb-1 block">Occupation:</label>
              <Input
                v-model="formData.occupation"
                @input="saveChanges"
                placeholder="Occupation"
                class="h-9"
              />
            </div>
          </div>
        </div>

        <!-- Columna Derecha: Avatar -->
        <div class="flex-shrink-0">
          <div
            class="relative w-64 h-80 rounded-lg border-2 border-dashed border-muted-foreground/30 flex items-center justify-center overflow-hidden bg-muted/10 group"
          >
            <img
              v-if="avatarPreview"
              :src="avatarPreview"
              alt="Character avatar"
              class="w-full h-full object-cover"
            />
            <div v-else class="text-center p-4">
              <User class="h-16 w-16 text-muted-foreground/50 mx-auto mb-2" />
              <p class="text-sm text-muted-foreground">Character Portrait</p>
            </div>

            <button
              v-if="avatarPreview"
              @click="removeAvatar"
              class="absolute top-2 right-2 p-1.5 bg-destructive text-destructive-foreground rounded-md opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <Trash2 class="h-4 w-4" />
            </button>

            <label
              class="absolute inset-0 cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <input
                type="file"
                accept="image/*"
                class="hidden"
                @change="handleAvatarUpload"
              />
              <div class="w-full h-full flex items-center justify-center bg-black/50">
                <div class="text-center">
                  <Upload class="h-8 w-8 text-white mx-auto mb-2" />
                  <span class="text-sm text-white">
                    {{ avatarPreview ? 'Change' : 'Upload' }}
                  </span>
                </div>
              </div>
            </label>
          </div>
        </div>
      </div>

      <!-- Content Section -->
      <div class="space-y-6 pt-6 border-t">
        <div>
          <h3 class="text-lg font-semibold mb-3">Content</h3>
          <div
            class="border-2 border-border rounded-lg overflow-hidden bg-muted/5 min-h-[400px]"
          >
            <MilkdownEditor
              v-model="bodyContent"
              @textUpdate="handleBodyUpdate"
              placeholder="Write your character's description, backstory, personality..."
              class="p-4"
            />
          </div>
        </div>
      </div>

      <!-- Custom Properties -->
      <div class="pt-6 border-t">
        <div class="flex items-center justify-between mb-4">
          <button
            @click="addCustomProperty"
            class="text-sm text-muted-foreground hover:text-foreground flex items-center gap-1"
          >
            <Plus class="h-4 w-4" />
            Add Property
          </button>
        </div>

        <div v-if="formData.customProperties.length > 0" class="space-y-3">
          <div
            v-for="(prop, index) in formData.customProperties"
            :key="index"
            class="flex gap-3 items-start group"
          >
            <Input
              v-model="prop.key"
              @input="saveChanges"
              placeholder="Property name"
              class="w-40 h-9"
            />
            <Textarea
              v-model="prop.value"
              @input="saveChanges"
              placeholder="Value"
              class="flex-1 min-h-[36px] resize-none"
            />
            <Button
              variant="ghost"
              size="sm"
              @click="removeCustomProperty(index)"
              class="opacity-0 group-hover:opacity-100 transition-opacity"
            >
              <Trash2 class="h-4 w-4 text-destructive" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
