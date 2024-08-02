<script setup lang="ts">
import {useNuxtApp} from '#app'
import '@fontsource-variable/inter'
import Toaster from '@/components/ui/toast/Toaster.vue'
import UpdateNotification from '@/components/UpdateNotification.vue'
import GlobalCommand from "~/components/GlobalCommand.vue";

const {$initI18n, $initFonts} = useNuxtApp()

// Call the initI18n function to set up the language
await $initI18n()

// Call the initFonts function to set up the fonts
await $initFonts()
</script>

<template>
  <div>
    <Toaster/>
    <UpdateNotification/>
    <GlobalCommand/>
    <NuxtLayout>
      <NuxtPage/>
    </NuxtLayout>
  </div>
</template>