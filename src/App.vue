<script lang="ts" setup>
import TitleBar from '@/components/TitleBar.vue';
</script>

<template>
  <div class="w-screen h-screen flex flex-col">
    <div class="flex-1 flex flex-col overflow-hidden">
      <TitleBar
          class="z-50"
      />
      <div class="flex-1 flex gap-2.5 p-5 overflow-hidden">
        <main class="grow shrink basis-0 self-stretch bg-background flex-col justify-start items-start gap-2.5 inline-flex overflow-auto scrollable-content">
          <slot />
        </main>
      </div>
    </div>
  </div>
</template>

<style>
</style>