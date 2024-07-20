<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { features } from "~/data/features";

const { t } = useI18n()
</script>

<template>
  <main class="text-foreground body-font">
    <div class="container px-5 py-24 mx-auto">
      <div class="flex flex-wrap w-full mb-20 flex-col items-center text-center">
        <img src="~/assets/img/lore-designer.svg" alt="Lore Designer Logo" class="w-24 h-24 mb-4"/>
        <h1 class="sm:text-3xl text-2xl font-medium title-font mb-2 text-white">
          {{ t('welcome') }} <span class="text-primary font-bold">{{ t('loreDeisgner') }}</span>
        </h1>
        <p class="lg:w-1/2 w-full leading-relaxed text-opacity-80">
          {{ t('description') }}
        </p>
      </div>
      <div class="flex flex-wrap -m-4">
        <feature-card
            v-for="feat in features"
            :key="feat.title"
            :title="t(`features.${feat.title}.title`)"
            :description="t(`features.${feat.title}.description`)"
            :icon="feat.icon"
        />
      </div>
     <create-project-dialog />
      
    </div>
  </main>
</template>