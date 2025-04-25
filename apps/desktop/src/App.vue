<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { Pen } from 'lucide-vue-next'
import { useRoute } from 'vue-router'
import { usePreferencesStore } from '@common/stores/preferences.store'
import { useAppTitle } from '@common/composables/useAppTitle.ts'
import AppTitlebar from '@common/components/AppTitlebar.vue'

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
  <AppTitlebar :title="title">
    <template #logo>
      <Pen class="h-4 w-4" />
    </template>
  </AppTitlebar>

  <div class="app-container pt-9">
    <div class="main-content min-h-[calc(100vh-36px)] bg-background text-foreground hide-scrollbar">
      <router-view />
    </div>
  </div>
</template>
