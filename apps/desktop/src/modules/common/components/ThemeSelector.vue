<script setup lang="ts">
import { Sun, Moon, Monitor } from 'lucide-vue-next'
import { Card, CardContent } from '@/components/ui/card'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  modelValue: 'dark' | 'light' | 'auto'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: 'dark' | 'light' | 'auto']
}>()

const themes = [
  {
    value: 'light' as const,
    label: t('settings.appearance.themeOptions.light'),
    icon: Sun,
    preview: 'bg-gradient-to-br from-slate-50 to-slate-100'
  },
  {
    value: 'dark' as const,
    label: t('settings.appearance.themeOptions.dark'),
    icon: Moon,
    preview: 'bg-gradient-to-br from-slate-900 to-slate-800'
  },
  {
    value: 'auto' as const,
    label: t('settings.appearance.themeOptions.system'),
    icon: Monitor,
    preview: 'bg-gradient-to-br from-slate-400 to-slate-600'
  }
]

const handleSelect = (value: 'dark' | 'light' | 'auto') => {
  emit('update:modelValue', value)
}
</script>

<template>
  <div class="grid grid-cols-3 gap-4">
    <Card
      v-for="theme in themes"
      :key="theme.value"
      class="cursor-pointer transition-all duration-300 hover:shadow-lg"
      :class="[
        modelValue === theme.value
          ? 'ring-2 ring-primary shadow-md'
          : 'hover:border-primary/50'
      ]"
      @click="handleSelect(theme.value)"
    >
      <CardContent class="p-4">
        <div
          class="h-24 rounded-md mb-3 flex items-center justify-center transition-transform duration-300"
          :class="[
            theme.preview,
            modelValue === theme.value ? 'scale-105' : 'group-hover:scale-105'
          ]"
        >
          <component
            :is="theme.icon"
            class="w-8 h-8 transition-colors duration-300"
            :class="theme.value === 'light' ? 'text-slate-700' : 'text-slate-100'"
          />
        </div>
        <p class="text-center font-medium text-sm">{{ theme.label }}</p>
      </CardContent>
    </Card>
  </div>
</template>
