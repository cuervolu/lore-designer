<script setup lang="ts">
import { ref } from 'vue';

import { usePreferencesStore } from '@common/stores/preferences.store';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';

const preferencesStore = usePreferencesStore();

const themeOptions = [
  { value: 'auto', label: 'System Default' },
  { value: 'light', label: 'Light' },
  { value: 'dark', label: 'Dark' },
];

const languageOptions = [
  { value: 'en', label: 'English' },
  { value: 'es', label: 'Español' },
];

const fontSizes = ref([preferencesStore.font_size]);
const autoSaveIntervals = ref([preferencesStore.auto_save_interval_seconds]);

const handleThemeChange = (value: string) => {
  preferencesStore.setTheme(value as 'dark' | 'light' | 'auto');
};

const handleLanguageChange = (value: string) => {
  preferencesStore.setLanguage(value);
};

const handleFontSizeChange = (event: Event) => {
  const value = parseInt((event.target as HTMLInputElement).value);
  preferencesStore.updateFontSize(value);
};

const handleAutoSaveChange = (checked: boolean) => {
  preferencesStore.updateAutoSave(checked);
};

const handleIntervalChange = (event: Event) => {
  const value = parseInt((event.target as HTMLInputElement).value);
  preferencesStore.updateAutoSave(preferencesStore.auto_save, value);
};
</script>

<template>
  <div class="space-y-6">
    <div class="bg-card text-card-foreground p-6 rounded-md shadow-sm border">
      <h2 class="text-xl font-medium mb-4">Appearance</h2>

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <Label for="theme">Theme</Label>
          <div class="w-[180px]">
            <Select
              :model-value="preferencesStore.theme"
              @update:model-value="handleThemeChange"
            >
              <SelectTrigger id="theme">
                <SelectValue placeholder="Select theme" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in themeOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <Label for="language">Language</Label>
          <div class="w-[180px]">
            <Select
              :model-value="preferencesStore.language"
              @update:model-value="handleLanguageChange"
            >
              <SelectTrigger id="language">
                <SelectValue placeholder="Select language" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in languageOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <Label for="font-size">Font Size: {{ preferencesStore.font_size }}px</Label>
          <div class="w-[180px]">
            <Input
              id="font-size"
              type="range"
              :min="10"
              :max="20"
              :step="1"
              :value="preferencesStore.font_size"
              @input="handleFontSizeChange"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="bg-card text-card-foreground p-6 rounded-md shadow-sm border">
      <h2 class="text-xl font-medium mb-4">Application Behavior</h2>

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-medium">Auto Save</h3>
            <p class="text-sm text-muted-foreground">Automatically save changes</p>
          </div>
          <Switch
            :model-value="preferencesStore.auto_save"
            @update:model-value="handleAutoSaveChange"
          />
        </div>

        <div v-if="preferencesStore.auto_save" class="flex items-center justify-between">
          <Label for="save-interval">
            Save Interval: {{ preferencesStore.auto_save_interval_seconds }}s
          </Label>
          <div class="w-[180px]">
            <Input
              id="save-interval"
              type="range"
              :min="10"
              :max="300"
              :step="10"
              :value="preferencesStore.auto_save_interval_seconds"
              @input="handleIntervalChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
