<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Palette, Settings2, Save } from 'lucide-vue-next'
import { usePreferencesStore } from '@common/stores/preferences.store'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Slider } from '@/components/ui/slider'
import { toast } from 'vue-sonner'
import SettingsSection from '@common/components/SettingsSection.vue'
import ThemeSelector from '@common/components/ThemeSelector.vue'
import LanguageSelector from '@common/components/LanguageSelector.vue'

const preferencesStore = usePreferencesStore()
const { t } = useI18n()

const selectedTheme = computed({
  get() {
    return preferencesStore.theme
  },
  set(value: 'dark' | 'light' | 'auto') {
    preferencesStore.setTheme(value)
    toast.success(t('settings.notifications.themeChanged'))
  }
})

const selectedLanguage = computed({
  get() {
    return preferencesStore.language
  },
  set(value: string) {
    preferencesStore.setLanguage(value)
    toast.success(t('settings.notifications.languageChanged'))
  }
})

const fontSizes = ref([preferencesStore.font_size])
const autoSaveIntervals = ref([preferencesStore.auto_save_interval_seconds])

const handleFontSizeChange = (values: number[] | undefined) => {
  if (!values || values.length === 0 || values[0] === undefined) return

  const size = values[0]
  fontSizes.value = values
  preferencesStore.updateFontSize(size)
  toast.success(t('settings.notifications.fontSizeChanged'))
}

const handleAutoSaveChange = (checked: boolean) => {
  preferencesStore.updateAutoSave(checked)
  toast.success(
    checked
      ? t('settings.notifications.autoSaveEnabled')
      : t('settings.notifications.autoSaveDisabled')
  )
}

const handleIntervalChange = (values: number[] | undefined) => {
  if (!values || values.length === 0) return

  autoSaveIntervals.value = values
  preferencesStore.updateAutoSave(preferencesStore.auto_save, values[0])
}
</script>

<template>
  <div class="space-y-6">
    <SettingsSection
      :title="$t('settings.appearance.title')"
      :description="$t('settings.appearance.description')"
      :icon="Palette"
    >
      <div class="space-y-6">
        <div>
          <Label class="text-base font-semibold mb-3 block">
            {{ $t('settings.appearance.theme') }}
          </Label>
          <ThemeSelector v-model="selectedTheme" />
        </div>

        <div class="pt-4 border-t">
          <Label class="text-base font-semibold mb-3 block">
            {{ $t('settings.appearance.language') }}
          </Label>
          <p class="text-sm text-muted-foreground mb-3">
            {{ $t('settings.appearance.languageDescription') }}
          </p>
          <LanguageSelector v-model="selectedLanguage" />
        </div>

        <div class="pt-4 border-t">
          <div class="flex items-center justify-between mb-3">
            <div>
              <Label class="text-base font-semibold">
                {{ $t('settings.appearance.fontSizeLabel') }}
              </Label>
              <p class="text-sm text-muted-foreground">
                {{ $t('settings.appearance.fontSizeDescription') }}
              </p>
            </div>
            <span class="text-sm font-medium text-muted-foreground">
              {{ fontSizes[0] }}px
            </span>
          </div>
          <Slider
            :model-value="fontSizes"
            @update:model-value="handleFontSizeChange"
            :min="12"
            :max="20"
            :step="1"
            class="w-full"
          />
          <div class="flex justify-between text-xs text-muted-foreground mt-2">
            <span>12px</span>
            <span>16px</span>
            <span>20px</span>
          </div>
        </div>
      </div>
    </SettingsSection>

    <SettingsSection
      :title="$t('settings.behavior.title')"
      :description="$t('settings.behavior.description')"
      :icon="Settings2"
    >
      <div class="space-y-6">
        <div class="flex items-center justify-between">
          <div class="flex-1 mr-4">
            <div class="flex items-center gap-2 mb-1">
              <Save class="w-4 h-4 text-primary" />
              <Label class="text-base font-semibold">
                {{ $t('settings.behavior.autoSave') }}
              </Label>
            </div>
            <p class="text-sm text-muted-foreground">
              {{ $t('settings.behavior.autoSaveDescription') }}
            </p>
          </div>
          <Switch
            :checked="preferencesStore.auto_save"
            @update:checked="handleAutoSaveChange"
          />
        </div>

        <div
          v-if="preferencesStore.auto_save"
          class="pt-4 border-t transition-all duration-300"
        >
          <div class="flex items-center justify-between mb-3">
            <div>
              <Label class="text-base font-semibold">
                {{ $t('settings.behavior.saveIntervalLabel') }}
              </Label>
              <p class="text-sm text-muted-foreground">
                {{ $t('settings.behavior.saveIntervalDescription') }}
              </p>
            </div>
            <span class="text-sm font-medium text-muted-foreground">
              {{ autoSaveIntervals[0] }}s
            </span>
          </div>
          <Slider
            :model-value="autoSaveIntervals"
            @update:model-value="handleIntervalChange"
            :min="10"
            :max="300"
            :step="10"
            class="w-full"
          />
          <div class="flex justify-between text-xs text-muted-foreground mt-2">
            <span>10s</span>
            <span>1min</span>
            <span>5min</span>
          </div>
        </div>
      </div>
    </SettingsSection>
  </div>
</template>
