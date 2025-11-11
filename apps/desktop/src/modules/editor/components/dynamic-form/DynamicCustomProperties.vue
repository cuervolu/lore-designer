<script setup lang="ts">
import { computed } from 'vue'
import type { FieldDefinition } from '@/modules/editor/types/form.type'
import { Plus, Trash2 } from 'lucide-vue-next'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Button } from '@/components/ui/button'

type CustomProp = { key: string; value: string }

const props = defineProps<{
  fieldDefinition: FieldDefinition
  modelValue: any
}>()

const emit = defineEmits(['update:modelValue'])

const field = computed(() => props.fieldDefinition)
const value = computed<CustomProp[]>({
  get: () => Array.isArray(props.modelValue) ? props.modelValue : [],
  set: (newValue) => {
    emit('update:modelValue', newValue)
  },
})

function addCustomProperty() {
  value.value = [...value.value, { key: '', value: '' }]
}

function removeCustomProperty(index: number) {
  const newArray = [...value.value]
  newArray.splice(index, 1)
  value.value = newArray
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <button
        @click="addCustomProperty"
        class="text-sm text-muted-foreground hover:text-foreground flex items-center gap-1"
      >
        <Plus class="h-4 w-4" />
        {{ field.label || 'Add Property' }}
      </button>
    </div>

    <div v-if="value.length > 0" class="space-y-3">
      <div
        v-for="(prop, index) in value"
        :key="index"
        class="flex gap-3 items-start group"
      >
        <Input
          v-model="prop.key"
          placeholder="Property name"
          class="w-40 h-9"
        />
        <Textarea
          v-model="prop.value"
          placeholder="Value"
          class="flex-1 min-h-[36px] resize-none"
          :rows="1"
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
</template>
