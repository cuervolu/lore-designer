<script setup lang="ts">
import { SidebarProvider } from '@/components/ui/sidebar';
import WizardSidebar from '../components/WizardSidebar.vue';
</script>

<template>
  <SidebarProvider>
    <!-- The h-[calc(100vh-36px)] subtracts the titlebar height from the full viewport height -->
    <main class="flex h-[calc(100vh-36px)] w-screen overflow-hidden">
      <WizardSidebar />
      <div class="flex-1 overflow-auto">
        <router-view />
      </div>
    </main>
  </SidebarProvider>
</template>
