<script setup lang="ts">
import { Card, CardContent } from '@/components/ui/card'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const languages = [
  {
    value: 'en',
    label: t('settings.appearance.languageOptions.en'),
    flag: '🇺🇸',
    nativeName: 'English'
  },
  {
    value: 'es',
    label: t('settings.appearance.languageOptions.es'),
    flag: '🇪🇸',
    nativeName: 'Español'
  }
]

const handleSelect = (value: string) => {
  emit('update:modelValue', value)
}
</script>

<template>
  <div class="grid grid-cols-2 gap-4">
    <Card
      v-for="lang in languages"
      :key="lang.value"
      class="cursor-pointer transition-all duration-300 hover:shadow-md"
      :class="[
        modelValue === lang.value
          ? 'ring-2 ring-primary shadow-sm'
          : 'hover:border-primary/50'
      ]"
      @click="handleSelect(lang.value)"
    >
      <CardContent class="p-4 flex items-center gap-3">
        <span class="text-3xl">{{ lang.flag }}</span>
        <div class="flex-1">
          <p class="font-medium text-sm">{{ lang.nativeName }}</p>
          <p class="text-xs text-muted-foreground">{{ lang.label }}</p>
        </div>
      </CardContent>
    </Card>
  </div>
</template>
