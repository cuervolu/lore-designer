<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDefinition } from '@/modules/editor/types/form.type'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

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
  <Select v-model="value" :required="field.required">
    <SelectTrigger :id="field.key">
      <SelectValue :placeholder="field.placeholder || 'Select an option'" />
    </SelectTrigger>
    <SelectContent>
      <SelectItem
        v-for="option in field.options || []"
        :key="option"
        :value="option"
      >
        {{ option }}
      </SelectItem>
    </SelectContent>
  </Select>
</template>
