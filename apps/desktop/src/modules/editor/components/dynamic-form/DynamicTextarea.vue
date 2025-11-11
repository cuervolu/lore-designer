<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDefinition } from '@/modules/editor/types/form.type'
import { Textarea } from '@/components/ui/textarea'

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
  <Textarea
    :id="field.key"
    v-model="value"
    :placeholder="field.placeholder"
    :required="field.required"
    :rows="field.rows || 3"
  />
</template>
