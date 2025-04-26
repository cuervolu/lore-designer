<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Toaster } from '@/components/ui/sonner'
import { usePreferencesStore } from '@common/stores/preferences.store'
import { useAppTitle } from '@common/composables/useAppTitle.ts'
import AppTitlebar from '@common/components/AppTitlebar.vue'
import logo from '@/assets/app_icon.webp';

const preferencesStore = usePreferencesStore()
const route = useRoute()
const { title, setTitle } = useAppTitle()

// Define mapping for route names to page titles
const routeTitles: Record<string, string> = {
  'workspaces': 'Welcome to Lore Designer',
  'new-workspace': 'Create New Workspace',
  'plugins': 'Plugins',
  'settings': 'Settings',
  'learn': 'Learn'
};

// Update title when route changes
watch(() => route.name, (newRouteName) => {
  if (newRouteName && typeof newRouteName === 'string' && newRouteName in routeTitles) {
    setTitle(routeTitles[newRouteName]);
  } else {
    setTitle('Lore Designer');
  }
}, { immediate: true });

onMounted(async () => {
  await preferencesStore.loadPreferences()
})
</script>

<template>
  <div class="fixed top-0 left-0 right-0 z-50">
    <AppTitlebar :title="title">
      <template #logo>
        <img
          :src="logo"
          alt="Logo"
          class="h-5 w-5 object-contain"
        />
      </template>
    </AppTitlebar>
  </div>

  <div class="h-screen w-screen pt-9">
    <Toaster />
    <div class="h-full bg-background text-foreground">
      <router-view />
    </div>
  </div>
</template>

<style>
html, body {
  overflow: hidden;
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
