<script setup lang="ts">
import { getVersion } from "@tauri-apps/api/app";
import { useRoute } from 'vue-router'
import { Home, Settings } from 'lucide-vue-next'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'
import logo from '@/assets/app_icon.webp'
import { onMounted, ref } from "vue";
import { error } from "@fltsci/tauri-plugin-tracing";

const route = useRoute()
const appVersion = ref("0.1.0");

const menuItems = [
  {
    title: 'Workspaces',
    icon: Home,
    path: '/wizard',
  },
  {
    title: 'Settings',
    icon: Settings,
    path: '/settings',
  },
]

onMounted(async () => {
  try {
    appVersion.value = await getVersion();
  } catch (err) {
    error(`Failed to get app version: ${err}`);
  }
});

const isActive = (path: string) => route.path === path
</script>

<template>
  <Sidebar>
    <SidebarHeader>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton size="lg" class="cursor-default hover:bg-transparent">
            <div class="flex aspect-square size-8 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
              <img 
                :src="logo"
                alt="Lore Designer"
                class="size-6"
              />
            </div>
            <div class="flex flex-col gap-0.5 leading-none">
              <span class="font-semibold">Lore Designer</span>
              <span class="text-xs text-muted-foreground">v{{ appVersion }}</span>
            </div>
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>

    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem v-for="item in menuItems" :key="item.path">
              <SidebarMenuButton
                :is-active="isActive(item.path)"
                as-child
              >
                <router-link :to="item.path">
                  <component :is="item.icon" />
                  <span>{{ item.title }}</span>
                </router-link>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>
  </Sidebar>
</template>