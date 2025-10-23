<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import {useI18n} from "vue-i18n";
import { Toaster } from '@/components/ui/sonner'
import 'vue-sonner/style.css'
import { usePreferencesStore } from '@common/stores/preferences.store'
import { useAppTitle } from '@common/composables/useAppTitle.ts'
import AppTitlebar from '@common/components/AppTitlebar.vue'
import logo from '@/assets/app_icon.webp';

const preferencesStore = usePreferencesStore()
const route = useRoute()
const { t } = useI18n()
const { title, setWizardPageTitle, resetTitle } = useAppTitle()

const routeTitleKeys: Record<string, string> = {
  'workspaces': 'welcome.routeTitles.welcome',
  'new-workspace': 'welcome.routeTitles.newWorkspace',
  'plugins': 'welcome.routeTitles.plugins',
  'settings': 'welcome.routeTitles.settings',
  'learn': 'welcome.routeTitles.learn'
};

watch(() => route.name, (newRouteName) => {
  if (newRouteName && typeof newRouteName === 'string' && newRouteName in routeTitleKeys) {
    // Use the wizard-specific function which doesn't update the native window title
    const titleKey = routeTitleKeys[newRouteName];
    setWizardPageTitle(t(titleKey));
  } else {
    // Not in a recognized wizard route - reset the title
    // but don't update the native window title if we're in the editor
    const inEditor = newRouteName === 'editor';
    resetTitle(!inEditor);
  }
}, { immediate: true });

onMounted(async () => {
  await preferencesStore.loadPreferences()
})
</script>

<template>
  <AppTitlebar :title="title" class="fixed top-0 left-0 right-0 z-50">
    <template #logo>
      <img
        :src="logo"
        alt="Logo"
        class="h-5 w-5 object-contain"
      />
    </template>
  </AppTitlebar>

  <main class="h-screen w-screen pt-9 bg-background text-foreground">
  <Toaster position="bottom-right" rich-colors />
    <router-view />
  </main>

</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(128, 128, 128, 0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(128, 128, 128, 0.5);
}
</style>
