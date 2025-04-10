<script setup lang="ts">
import { useColorMode } from '@vueuse/core';

useColorMode({
  attribute: 'class',
  storageKey: 'lore-designer-theme',
  modes: {
    light: '',
    dark: 'dark',
  },
});
</script>

<template>
  <div class="min-h-screen bg-background text-foreground">
    <router-view />
  </div>
</template>
