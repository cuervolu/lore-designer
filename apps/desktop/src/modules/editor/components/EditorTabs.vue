<script setup lang="ts">
import {User, Home, PenTool, X, type LucideIcon} from 'lucide-vue-next';

const props = defineProps<{
  files: {
    id: string;
    name: string;
    extension: string;
    path: string;
    icon: string;
  }[];
  activeTab: string;
}>();

// Icon mapping
const iconMap: Record<string, LucideIcon> = {
  'User': User,
  'Home': Home,
  'PenTool': PenTool
};

// Get icon component based on the icon name
const getIconComponent = (iconName: string) => {
  return iconMap[iconName] || User;
};
</script>

<template>
  <div class="flex border-b">
    <div
      v-for="file in files"
      :key="file.id"
      class="flex items-center px-4 py-2 cursor-pointer gap-2 border-r max-w-40"
      :class="[
        activeTab === file.id
          ? 'text-foreground border-b-2 border-b-primary'
          : 'text-muted-foreground hover:text-foreground'
      ]"
    >
      <component :is="getIconComponent(file.icon)" class="h-4 w-4" />
      <span class="truncate">{{ file.name }}</span>
      <button class="hover:bg-muted rounded-full p-0.5">
        <X class="h-3.5 w-3.5" />
      </button>
    </div>
  </div>
</template>
