<script setup lang="ts">
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
} from '@/components/ui/sidebar';

const router = useRouter();
const route = useRoute();
const logoPath = new URL('@/assets/logo.webp', import.meta.url).href;

const menuItems = [
  { name: 'Workspaces', route: { name: 'workspaces' } },
  { name: 'Plugins', route: { name: 'plugins' } },
  { name: 'Settings', route: { name: 'settings' } },
  { name: 'Learn', route: { name: 'learn' } },
];

const currentRouteName = computed(() => route.name);

const navigateTo = (routeName: string) => {
  router.push({ name: routeName });
};
</script>

<template>
  <Sidebar>
    <SidebarHeader class="p-4">
      <div class="flex items-center space-x-2">
        <div class="w-10 h-10 rounded bg-gray-200 flex items-center justify-center">
          <img :src="logoPath" alt="Logo" class="w-8 h-8" />
        </div>
        <div>
          <h1 class="text-lg font-semibold">Lore Designer</h1>
          <p class="text-sm text-gray-500">v1.0</p>
        </div>
      </div>
    </SidebarHeader>
    <SidebarContent>
      <SidebarMenu>
        <SidebarMenuItem v-for="item in menuItems" :key="item.name">
          <SidebarMenuButton
            :isActive="currentRouteName === item.route.name"
            @click="navigateTo(item.route.name)"
          >
            {{ item.name }}
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarContent>
  </Sidebar>
</template>
