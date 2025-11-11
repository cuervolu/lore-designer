<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDefinition } from '@/modules/editor/types/form.type'
import { Label } from '@/components/ui/label'
import DynamicTextInput from './DynamicTextInput.vue'
import DynamicTextarea from './DynamicTextarea.vue'
import DynamicSelect from './DynamicSelect.vue'

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
  <div class="grid w-full items-center gap-1.5">
    <Label :for="field.key">{{ field.label }}</Label>

    <DynamicTextInput
      v-if="field.type === 'text' || field.type === 'number'"
      v-model="value"
      :field-definition="field"
    />
    <DynamicTextarea
      v-else-if="field.type === 'textarea'"
      v-model="value"
      :field-definition="field"
    />
    <DynamicSelect
      v-else-if="field.type === 'select'"
      v-model="value"
      :field-definition="field"
    />
  </div>
</template>
