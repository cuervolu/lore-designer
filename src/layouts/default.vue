<script setup lang="ts">
import TitleBar from "~/components/TitleBar.vue";
import SideBar from "~/components/SideBar.vue";

</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <SideBar class="w-14 flex-shrink-0"/>
    <div class="flex flex-col flex-grow overflow-hidden">
      <TitleBar title="main" class="flex-shrink-0"/>
      <main class="flex-grow overflow-auto bg-muted/40">
        <slot/>
      </main>
    </div>
  </div>
</template>

<style scoped>
:global(body) {
  overflow: hidden;
}
</style>