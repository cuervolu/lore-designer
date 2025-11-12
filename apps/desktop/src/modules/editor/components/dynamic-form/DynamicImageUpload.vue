<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDefinition } from '@/modules/editor/types/form.type'
import { Upload, User, Trash2 } from 'lucide-vue-next'
import { error as logError } from 'tauri-plugin-tracing'

const props = defineProps<{
  fieldDefinition: FieldDefinition
  modelValue: any
}>()

const emit = defineEmits(['update:modelValue'])

const field = computed(() => props.fieldDefinition)
const value = computed<string | null>({
  get: () => props.modelValue,
  set: (newValue) => {
    emit('update:modelValue', newValue)
  },
})

function handleAvatarUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      value.value = e.target?.result as string
    }
    reader.onerror = async (e) => {
      await logError(`FileReader error: ${e}`)
    }
    reader.readAsDataURL(file)
  }
}

function removeAvatar() {
  value.value = null
}
</script>

<template>
  <div
    class="relative w-64 h-80 rounded-lg border-2 border-dashed border-muted-foreground/30 flex items-center justify-center overflow-hidden bg-muted/10 group"
  >
    <img
      v-if="value"
      :src="value"
      :alt="field.label"
      class="w-full h-full object-cover"
    />
    <div v-else class="text-center p-4">
      <User class="h-16 w-16 text-muted-foreground/50 mx-auto mb-2" />
      <p class="text-sm text-muted-foreground">{{ field.placeholder || 'Character Portrait' }}</p>
    </div>

    <button
      v-if="value"
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
            {{ value ? 'Change' : 'Upload' }}
          </span>
        </div>
      </div>
    </label>
  </div>
</template>
