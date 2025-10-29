<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n';
import { usePreferencesStore } from '@common/stores/preferences.store';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';

const preferencesStore = usePreferencesStore();
const { t, locale } = useI18n();

const themeOptions = computed(() => [
  { value: 'auto', label: t('settings.appearance.themeOptions.system') },
  { value: 'light', label: t('settings.appearance.themeOptions.light') },
  { value: 'dark', label: t('settings.appearance.themeOptions.dark') },
]);

const languageOptions = computed(() => [
  { value: 'en', label: t('settings.appearance.languageOptions.en') },
  { value: 'es', label: t('settings.appearance.languageOptions.es') },
]);


const selectedLanguage = computed({
  get() {
    return preferencesStore.language;
  },
  set(value: string) {
    preferencesStore.setLanguage(value);
  }
});




const fontSizes = ref([preferencesStore.font_size]);
const autoSaveIntervals = ref([preferencesStore.auto_save_interval_seconds]);

const handleThemeChange = (value: string) => {
  preferencesStore.setTheme(value as 'dark' | 'light' | 'auto');
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
      <h2 class="text-xl font-medium mb-4">{{ $t('settings.appearance.title') }}</h2>

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <Label for="theme">{{ $t('settings.appearance.theme') }}</Label>
          <div class="w-[180px]">
            <Select
              :model-value="preferencesStore.theme"
              @update:model-value="handleThemeChange"
            >
              <SelectTrigger id="theme">
                <SelectValue :placeholder="$t('settings.appearance.selectTheme')" />
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
          <div>
            <Label for="language-select" class="font-medium">{{ $t('settings.appearance.language') }}</Label>
            <p class="text-sm text-muted-foreground">{{ $t('settings.appearance.languageDescription') }}</p>
          </div>
          <Select
            id="language-select"
            v-model="selectedLanguage"
          >
            <SelectTrigger class="w-[180px]">
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

        <div class="flex items-center justify-between">
          <Label for="font-size">{{ $t('settings.appearance.fontSize', { size: preferencesStore.font_size }) }}</Label>
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
      <h2 class="text-xl font-medium mb-4">{{ $t('settings.behavior.title') }}</h2>

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-medium">{{ $t('settings.behavior.autoSave') }}</h3>
            <p class="text-sm text-muted-foreground">{{ $t('settings.behavior.autoSaveDescription') }}</p>
          </div>
          <Switch
            :model-value="preferencesStore.auto_save"
            @update:model-value="handleAutoSaveChange"
          />
        </div>

        <div v-if="preferencesStore.auto_save" class="flex items-center justify-between">
          <Label for="save-interval">
            {{ $t('settings.behavior.saveInterval', { seconds: preferencesStore.auto_save_interval_seconds }) }}
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
