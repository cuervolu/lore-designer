<script lang="ts">
// Supports weights 100-900
import '@fontsource-variable/inter';
import Toaster from '@/components/ui/toast/Toaster.vue'

</script>

<template>
  <div>
    <Toaster />
    <NuxtLayout>
      <NuxtPage/>
    </NuxtLayout>
  </div>
</template>
