<script setup lang="ts">
import {
  ChevronDown,
  ChevronRight,
  FileText,
  User,
  Map,
  Book,
  PenTool,
  File,
  Folder,
  Image,
  Plus, type LucideIcon
} from 'lucide-vue-next';
import {SidebarMenuItem, SidebarMenuButton, SidebarMenuAction} from '@/components/ui/sidebar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import type {FileTree} from '@editor/types/editor.types';

const props = defineProps<{
  item: FileTree;
  depth: number;
  isExpanded: boolean;
}>();

const emit = defineEmits<{
  'toggle-folder': [path: string];
  'file-click': [path: string];
  'create-file': [parentPath: string];
}>();

// Icon mapping
const iconMap: Record<string, LucideIcon> = {
  // Folder icons
  'characters': User,
  'locations': Map,
  'location': Map,
  'lore': Book,
  'story': FileText,
  'stories': FileText,
  'notes': FileText,
  // File type icons
  'markdown': FileText,
  'canvas': PenTool,
  'character': User,
  'image': Image,
  'default': File
};

// Get appropriate icon component
const getIcon = () => {
  if (props.item.is_directory) {
    if (props.item.icon) {
      return iconMap[props.item.icon] || Folder;
    }
    return Folder;
  }
  if (props.item.icon) {
    return iconMap[props.item.icon] || File;
  }
  return FileText;
};

const IconComponent = getIcon();

const toggleFolder = () => {
  emit('toggle-folder', props.item.path);
};

const handleClick = () => {
  if (props.item.is_directory) {
    toggleFolder();
  } else {
    emit('file-click', props.item.path);
  }
};

const handleCreateFile = () => {
  emit('create-file', props.item.path);
};
</script>

<template>
  <SidebarMenuItem>
    <template v-if="item.is_directory">
      <div class="space-y-1 w-full">
        <SidebarMenuButton
          @click="toggleFolder"
          :class="`flex items-center gap-1 w-full justify-start ${depth > 0 ? 'pl-' + depth * 2 : ''}`"
        >
          <component
            :is="isExpanded ? ChevronDown : ChevronRight"
            class="h-4 w-4 text-muted-foreground"
          />
          <component :is="IconComponent" class="h-4 w-4"/>
          <span>{{ item.name }}</span>
        </SidebarMenuButton>

        <SidebarMenuAction showOnHover>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Plus class="h-3.5 w-3.5"/>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuItem @click="handleCreateFile">
                <FileText class="h-4 w-4 mr-2"/>
                New File
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuAction>

        <!-- Children - recursively render child items using slot -->
        <div v-if="isExpanded && item.children.length > 0" class="pl-2 space-y-1">
          <slot name="children"/>
        </div>
      </div>
    </template>
    <template v-else>
      <SidebarMenuButton
        @click="emit('file-click', item.path)"
        :class="`flex items-center gap-1 w-full justify-start ${depth > 0 ? 'pl-' + (depth * 2 + 1) : 'ml-5'}`"
      >
        <component :is="IconComponent" class="h-4 w-4"/>
        <span>{{ item.name }}</span>
      </SidebarMenuButton>
    </template>
  </SidebarMenuItem>
</template>
