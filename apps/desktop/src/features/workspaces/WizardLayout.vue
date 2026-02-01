<script setup lang="ts">
import { SidebarProvider, SidebarInset } from '@/components/ui/sidebar'
import AppSidebar from '@/widgets/AppSidebar.vue'
</script>

<template>
  <SidebarProvider storage-key="lore-designer-sidebar">
    <AppSidebar />
    <SidebarInset>
      <router-view />
    </SidebarInset>
  </SidebarProvider>
</template>