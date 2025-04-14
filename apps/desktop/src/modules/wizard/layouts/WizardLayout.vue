<script setup lang="ts">
import { SidebarProvider } from '@/components/ui/sidebar';
import WizardSidebar from '../components/WizardSidebar.vue';
</script>

<template>
  <SidebarProvider>
    <main class="flex h-screen w-screen overflow-hidden">
      <WizardSidebar />
      <div class="flex-1 overflow-auto">
        <router-view />
      </div>
    </main>
  </SidebarProvider>
</template>
