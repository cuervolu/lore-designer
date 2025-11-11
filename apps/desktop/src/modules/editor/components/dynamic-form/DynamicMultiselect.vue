<script setup lang="ts">
import { computed, ref } from 'vue'
import type { FieldDefinition } from '@/modules/editor/types/form.type'
import { User, X } from 'lucide-vue-next'
import { Input } from '@/components/ui/input'

const props = defineProps<{
  fieldDefinition: FieldDefinition
  modelValue: any
}>()

const emit = defineEmits(['update:modelValue'])

const field = computed(() => props.fieldDefinition)
const value = computed<string[]>({
  get: () => Array.isArray(props.modelValue) ? props.modelValue : [],
  set: (newValue) => {
    emit('update:modelValue', newValue)
  },
})

const newAlias = ref('')

function addAlias() {
  if (newAlias.value.trim()) {
    value.value = [...value.value, newAlias.value.trim()]
    newAlias.value = ''
  }
}

function removeAlias(index: number) {
  const newArray = [...value.value]
  newArray.splice(index, 1)
  value.value = newArray
}
</script>

<template>
  <div class="flex items-center gap-2 flex-wrap mt-2">
    <User class="h-4 w-4 text-muted-foreground" />
    <span class="text-sm text-muted-foreground">{{ field.label }}:</span>
    <button
      v-for="(alias, index) in value"
      :key="index"
      @click="removeAlias(index)"
      class="px-3 py-1 text-sm bg-muted hover:bg-muted/70 rounded-full flex items-center gap-1 group"
    >
      {{ alias }}
      <X class="h-3 w-3 opacity-0 group-hover:opacity-100 transition-opacity" />
    </button>
    <Input
      :id="field.key"
      v-model="newAlias"
      @keydown.enter.prevent="addAlias"
      :placeholder="field.placeholder || '+ Add'"
      class="w-32 h-7 text-sm border-dashed"
    />
  </div>
</template>
