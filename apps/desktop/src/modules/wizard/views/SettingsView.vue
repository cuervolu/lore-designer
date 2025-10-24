<script setup lang="ts">
import AppPreferencesForm from '@common/components/AppPreferencesForm.vue';
import {useI18n} from "vue-i18n";


const { t } = useI18n()
</script>

<template>
  <div class="container mx-auto p-8">
    <h1 class="text-2xl font-bold mb-6">{{ $t('settings.title') }}</h1>

    <AppPreferencesForm />
  </div>
</template>
