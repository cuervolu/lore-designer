<script setup lang="ts">
import { getVersion } from "@tauri-apps/api/app";
import { error } from "@tauri-apps/plugin-log";
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useI18n } from "vue-i18n";
import { FolderOpen, Settings } from "lucide-vue-next";
import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
} from "@/components/ui/sidebar";

const router = useRouter();
const route = useRoute();
const { t } = useI18n();
const logoPath = new URL("@/assets/logo.webp", import.meta.url).href;
const menuItems = computed(() => [
  {
    name: t("wizard.sidebar.workspaces"),
    route: { name: "workspaces" },
    icon: FolderOpen,
  },
  {
    name: t("welcome.routeTitles.settings"),
    route: { name: "settings" },
    icon: Settings,
  },
]);

const currentRouteName = computed(() => route.name);
const appVersion = ref("0.1.0");

onMounted(async () => {
  try {
    appVersion.value = await getVersion();
  } catch (err) {
    await error(`Failed to get app version: ${err}`);
  }
});

const navigateTo = (routeName: string) => {
  router.push({ name: routeName });
};
</script>

<template>
  <Sidebar
    :style="{
      '--sidebar-top-offset': '36px',
    }"
  >
    <SidebarHeader class="p-6">
      <div class="flex items-center space-x-3">
        <div class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center shadow-sm">
          <img :src="logoPath" :alt="$t('wizard.sidebar.logoAlt')" class="w-7 h-7" />
        </div>
        <div>
          <h1 class="text-lg font-semibold">{{ $t("app.title") }}</h1>
          <p class="text-xs text-muted-foreground">v{{ appVersion }}</p>
        </div>
      </div>
    </SidebarHeader>

    <SidebarContent class="px-3">
      <SidebarMenu class="space-y-1.5">
        <SidebarMenuItem v-for="item in menuItems" :key="item.name" class="transition-colors">
          <SidebarMenuButton
            :isActive="currentRouteName === item.route.name"
            @click="navigateTo(item.route.name)"
            class="px-4 py-2.5 font-medium rounded-md transition-all"
            :class="[
              currentRouteName === item.route.name
                ? 'bg-primary text-primary-foreground'
                : 'hover:bg-muted',
            ]"
          >
            <component :is="item.icon" class="w-4 h-4 mr-2" />
            {{ item.name }}
          </SidebarMenuButton>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarContent>
  </Sidebar>
</template>
