<script setup lang="ts">
import { Settings } from "lucide-vue-next";
import AppPreferencesForm from "@common/components/AppPreferencesForm.vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>

<template>
  <div class="h-full overflow-y-auto">
    <div class="container mx-auto p-8 max-w-4xl">
      <header class="mb-8">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center">
            <Settings class="w-6 h-6 text-primary" />
          </div>
          <div>
            <h1 class="text-3xl font-bold">{{ $t("settings.title") }}</h1>
            <p class="text-muted-foreground">{{ $t("settings.subtitle") }}</p>
          </div>
        </div>
      </header>

      <AppPreferencesForm />
    </div>
  </div>
</template>
