<script setup lang="ts">
import type {Features} from "~/data/features";


const props = defineProps<Features>();
</script>

<template>
  <div class="xl:w-1/3 md:w-1/2 p-4">
    <div class="border border-gray-700 border-opacity-75 p-6 rounded-lg">
      <div
          class="w-10 h-10 inline-flex items-center justify-center rounded-full bg-gray-800 text-red-400 mb-4"
      >
        <component :is="props.icon" />
      </div>
      <h2 class="text-lg text-white font-medium title-font mb-2">{{ props.title }}</h2>
      <p class="leading-relaxed text-base">
        {{ props.description }}
      </p>
    </div>
  </div>
</template>

<style scoped>
</style>