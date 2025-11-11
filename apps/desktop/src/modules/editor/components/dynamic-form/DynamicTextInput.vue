<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDefinition } from '@/modules/editor/types/form.type'
import { Input } from '@/components/ui/input'

const props = defineProps<{
  fieldDefinition: FieldDefinition
  modelValue: any
}>()

const emit = defineEmits(['update:modelValue'])

const field = computed(() => props.fieldDefinition)

const value = computed({
  get: () => props.modelValue,
  set: (newValue) => {
    emit('update:modelValue', newValue)
  },
})
</script>

<template>
  <Input
    :id="field.key"
    v-model="value"
    :type="field.type === 'number' ? 'number' : 'text'"
    :placeholder="field.placeholder"
    :required="field.required"
  />
</template>
