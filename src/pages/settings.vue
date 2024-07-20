<script setup lang="ts">
import FontSelector from "~/components/FontSelector.vue";

definePageMeta({
  layout: 'settings-default'
})

</script>

<template>

  <div class="grid gap-6">
    <font-selector/>
    <lang-switcher/>
  </div>

</template>

<style scoped>

</style>