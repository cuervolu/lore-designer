<script setup lang="ts">
import {useI18n} from "vue-i18n";

const {t} = useI18n()
</script>

<template>
 
  <nav class="grid gap-4 text-sm text-muted-foreground">
    <a href="#" class="font-semibold text-primary">
      {{ t('settings.options.general') }}
    </a>
    <a href="#">
      Security
    </a>
    <a href="#">
      Integrations
    </a>
    <a href="#">
      Support
    </a>
    <a href="#">
      Organizations
    </a>
    <a href="#">
      Advanced
    </a>
  </nav>
</template>

<style scoped>

</style>