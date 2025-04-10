<script setup lang="ts">
import { Folder } from 'lucide-vue-next';

defineProps<{
  workspace: {
    id: number;
    name: string;
    path: string;
  }
}>();
</script>

<template>
  <div class="flex items-center p-4 hover:bg-accent hover:text-accent-foreground rounded-md cursor-pointer">
    <div class="mr-4 bg-muted w-10 h-10 rounded-md flex items-center justify-center">
      <Folder class="w-6 h-6 text-muted-foreground" />
    </div>
    <div class="flex-1">
      <h3 class="font-medium">{{ workspace.name }}</h3>
      <p class="text-sm text-muted-foreground">{{ workspace.path }}</p>
    </div>
    <div class="flex">
      <button class="text-muted-foreground hover:text-foreground">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-5 h-5"><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/></svg>
      </button>
    </div>
  </div>
</template>
