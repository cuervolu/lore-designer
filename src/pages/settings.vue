<script setup lang="ts">
import FontSelector from "~/components/FontSelector.vue";
import {invoke} from "@tauri-apps/api/core";

definePageMeta({
  layout: 'settings-default'
})

const cleanCache = async () => {
  await invoke('clean_cache').catch(console.error);
}

</script>

<template>

  <div class="grid gap-6">

    <font-selector/>
    <lang-switcher/>

    <Button @click="cleanCache">
      Clean cache
    </Button>
  </div>

</template>

<style scoped>

</style>