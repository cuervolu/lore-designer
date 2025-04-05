<script setup lang="ts">
import { SidebarProvider } from '@/components/ui/sidebar';
import AppSidebar from '@common/components/AppSidebar.vue';
</script>

<template>
  <div class="h-screen flex flex-col bg-background text-foreground">
    <!-- <AppTitlebar /> -->

    <div class="flex flex-1 overflow-hidden">
      <SidebarProvider>
        <AppSidebar />
        <main class="flex-1 overflow-auto p-4">
          <div class="flex items-center justify-center h-full">
            <div class="text-center">
              <h1 class="text-3xl font-bold mb-4">Welcome to Lore Designer</h1>
              <p class="text-muted-foreground">
                Create, manage, and organize your stories and game designs.
              </p>
            </div>
          </div>
        </main>
      </SidebarProvider>
    </div>
  </div>
</template>
