<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue';
import type {FieldDefinition} from '@/modules/editor/types/form.type';
import {Upload, User, Trash2, Loader2} from 'lucide-vue-next';
import {error as logError} from 'tauri-plugin-tracing';
import {useImage} from '@editor/composables/useImage';
import {useEditorStore} from '@editor/stores/editor.store';
import MissingImagePlaceholder from '@common/components/MissingImagePlaceholder.vue';

const props = defineProps<{
  fieldDefinition: FieldDefinition;
  modelValue: any;
}>();

const emit = defineEmits(['update:modelValue']);

const editorStore = useEditorStore();
const {
  selectAndCopyImageToWorkspace,
  getDisplaySrcFromRelativePath,
  validateImage,
  isProcessing,
} = useImage();

const field = computed(() => props.fieldDefinition);
const displaySrc = ref<string | null>(null);
const imageExists = ref(true);
const isLoadingImage = ref(false);

const relativePath = computed<string | null>({
  get: () => props.modelValue,
  set: (newValue) => {
    emit('update:modelValue', newValue);
  },
});

// Validate and load image for display
const loadImageForDisplay = async (path: string | null) => {
  if (!path) {
    displaySrc.value = null;
    imageExists.value = true;
    return;
  }

  isLoadingImage.value = true;

  const exists = await validateImage(path);
  imageExists.value = exists;

  if (exists) {
    displaySrc.value = await getDisplaySrcFromRelativePath(path);
  } else {
    displaySrc.value = null;
  }

  isLoadingImage.value = false;
};

const handleImageUpload = async () => {
  const currentFilePath = editorStore.activeTab?.path;
  if (!currentFilePath) {
    await logError('No active file to associate image with');
    return;
  }

  const result = await selectAndCopyImageToWorkspace(currentFilePath);

  if (result) {
    relativePath.value = result.relativeStoragePath;
    displaySrc.value = result.displaySrc;
    imageExists.value = true;
  }
};

const removeImage = () => {
  relativePath.value = null;
  displaySrc.value = null;
  imageExists.value = true;
};

const handleRelocateImage = async () => {
  await handleImageUpload();
};

watch(
  () => props.modelValue,
  (newPath) => {
    loadImageForDisplay(newPath);
  }
);

onMounted(() => {
  loadImageForDisplay(props.modelValue);
});
</script>

<template>
  <div class="space-y-2">
    <!-- Loading state -->
    <div
      v-if="isLoadingImage || isProcessing"
      class="relative w-64 h-80 rounded-lg border-2 border-dashed border-muted-foreground/30 flex items-center justify-center bg-muted/10"
    >
      <div class="text-center">
        <Loader2 class="h-8 w-8 text-muted-foreground animate-spin mx-auto mb-2"/>
        <p class="text-sm text-muted-foreground">
          {{ isProcessing ? 'Processing image...' : 'Loading...' }}
        </p>
      </div>
    </div>

    <!-- Missing image placeholder -->
    <MissingImagePlaceholder
      v-else-if="relativePath && !imageExists"
      :relative-path="relativePath"
      @relocate="handleRelocateImage"
      @remove="removeImage"
    />

    <!-- Image display/upload -->
    <div
      v-else
      class="relative w-64 h-80 rounded-lg border-2 border-dashed border-muted-foreground/30 flex items-center justify-center overflow-hidden bg-muted/10 group"
    >
      <!-- Existing image -->
      <img
        v-if="displaySrc"
        :src="displaySrc"
        :alt="field.label"
        class="w-full h-full object-cover"
      />

      <!-- Empty state -->
      <div v-else class="text-center p-4">
        <User class="h-16 w-16 text-muted-foreground/50 mx-auto mb-2"/>
        <p class="text-sm text-muted-foreground">
          {{ field.placeholder || 'Character Portrait' }}
        </p>
      </div>

      <!-- Remove button (when image exists) -->
      <button
        v-if="displaySrc"
        @click="removeImage"
        class="absolute top-2 right-2 p-1.5 bg-destructive text-destructive-foreground rounded-md opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <Trash2 class="h-4 w-4"/>
      </button>

      <!-- Upload overlay -->
      <button
        @click="handleImageUpload"
        class="absolute inset-0 cursor-pointer opacity-0 group-hover:opacity-100 transition-opacity"
      >
        <div class="w-full h-full flex items-center justify-center bg-black/50">
          <div class="text-center">
            <Upload class="h-8 w-8 text-white mx-auto mb-2"/>
            <span class="text-sm text-white">
              {{ displaySrc ? 'Change' : 'Upload' }}
            </span>
          </div>
        </div>
      </button>
    </div>
  </div>
</template>