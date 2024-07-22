<script setup lang="ts">
import TitleBar from "~/components/TitleBar.vue";
import SideBar from "~/components/SideBar.vue";
import SettingsNav from "~/components/SettingsNav.vue";
import CustomBreadcrumb from "~/components/CustomBreadcrumb.vue";

const {t} = useI18n()

</script>

<template>
  <div class="flex h-screen">
    <SideBar class="w-14 flex-shrink-0"/>
    <div class="flex flex-col flex-grow">
      <TitleBar title="main" class="flex-shrink-0"/>
      <main
          class="flex min-h-[calc(100vh_-_theme(spacing.16))] flex-1 flex-col gap-4 bg-muted/40 p-4 md:gap-8 md:p-10">
        <custom-breadcrumb new-route="settings" from="home" to="settings.title" old-route="/"/>
        <div class="mx-auto grid w-full max-w-6xl gap-2">
          <h1 class="text-3xl font-semibold">
            {{ t('settings.title') }}
          </h1>
        </div>
        <div
            class="mx-auto grid w-full max-w-6xl items-start gap-6 md:grid-cols-[180px_1fr] lg:grid-cols-[250px_1fr]">
          <settings-nav/>

          <slot/>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
</style>