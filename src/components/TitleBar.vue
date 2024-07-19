<script setup lang="ts">
import { Window } from '@tauri-apps/api/window'
import { X, Square, Minus } from 'lucide-vue-next'

interface Props {
  title: string
  bgColor?: string,
}

const { title, bgColor } = withDefaults(defineProps<Props>(), {
  title: 'main',
  bgColor: 'bg-background/90'
})

const appWindow = new Window(title)

function minimize() {
  appWindow.minimize()
}

function toggleMaximize() {
  appWindow.toggleMaximize()
}

function close() {
  appWindow.close()
}
</script>

<template>
  <div
      data-tauri-drag-region
      :class="[
      'titlebar z-10 flex h-10 items-center justify-end bg-opacity-50 p-1 select-none border-b overflow-hidden shadow-sm w-full',
      bgColor
    ]"
  >
    <div
        id="titlebar-minimize"
        class="titlebar-button flex items-center justify-center w-8 h-8 mx-1 p-0 rounded hover:bg-background/90"
        @click="minimize"
    >
      <Minus width="20" height="20" class="text-foreground" />
    </div>
    <div
        id="titlebar-maximize"
        class="titlebar-button flex items-center justify-center w-8 h-8 mx-1 p-0 rounded hover:bg-background/90"
        @click="toggleMaximize"
    >
      <Square width="20" height="20" class="text-foreground" />
    </div>
    <div
        id="titlebar-close"
        class="titlebar-button flex items-center justify-center w-8 h-8 mx-1 p-0 rounded hover:bg-red-500"
        @click="close"
    >
      <X width="20" height="20" class="text-foreground" />
    </div>
  </div>
</template>

<style scoped>
.titlebar-button:last-child {
  margin-right: 6px;
}
</style>