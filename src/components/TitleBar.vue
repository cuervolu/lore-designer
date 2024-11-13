<script setup lang="ts">
import { getCurrentWindow } from '@tauri-apps/api/window';
import { Minus, Maximize, X } from 'lucide-vue-next';

const appWindow = getCurrentWindow();

</script>

<template>
  <header
      data-tauri-drag-region
      class="flex justify-between items-center h-10 px-3"
  >
    <nav class="flex items-center ml-5">
      <img
          data-tauri-drag-region
          src="/src/assets/icon.svg"
          alt="Logo"
          class="w-5 h-5 mr-2"
      >
    </nav>
    <h1
        class="text-base font-semibold tracking-widest text-foreground" data-tauri-drag-region
    >
      Lore Designer
    </h1>
    <div class="flex items-center h-full mr-5">
      <Button
          size="sm"
          variant="ghost"
          class="titlebar-button flex items-center justify-center w-8 h-8 mx-1 p-0 rounded hover:bg-slate-50/20"
          @click="appWindow.minimize()"
      >
        <Minus class="w-4 h-4 text-foreground"  />
      </Button>
      <Button
          size="sm"
          variant="ghost"
          class="titlebar-button flex items-center justify-center w-8 h-8 mx-1 p-0 rounded hover:bg-slate-50/20"
          @click="appWindow.toggleMaximize()"
      >
        <Maximize width="20" height="20" class="text-foreground"  />
      </Button>
      <Button
          size="sm"
          variant="ghost"
          class="titlebar-button flex items-center justify-center w-8 h-8 mx-1 p-0 rounded hover:bg-red-500/80"
          @click="appWindow.close()"
      >
        <X width="20" height="20" class="text-foreground"  />
      </Button>
    </div>
  </header>
</template>

<style scoped>
.titlebar-button:last-child {
  margin-right: 6px;
}
</style>