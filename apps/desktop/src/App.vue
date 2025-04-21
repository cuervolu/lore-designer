<script setup lang="ts">
import { onMounted } from 'vue'
import { usePreferencesStore } from '@common/stores/preferences.store'

const preferencesStore = usePreferencesStore()

onMounted(async () => {
  await preferencesStore.loadPreferences()
})
</script>

<template>
  <div class="app-container">
    <div class="main-content min-h-screen bg-background text-foreground hide-scrollbar">
      <router-view />
    </div>
  </div>
</template>
