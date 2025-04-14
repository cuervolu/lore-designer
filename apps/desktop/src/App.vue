<script setup lang="ts">
import { onMounted } from 'vue';
import { usePreferencesStore } from '@common/stores/preferences.store';

const preferencesStore = usePreferencesStore();

onMounted(async () => {
  await preferencesStore.loadPreferences();
});
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <router-view />
  </div>
</template>
