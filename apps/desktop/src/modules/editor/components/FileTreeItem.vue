<script setup lang="ts">
import { computed } from 'vue'
import {
  ChevronDown,
  ChevronRight,
  FileText,
  User,
  MapIcon,
  Book,
  PenTool,
  File,
  Folder,
  Image,
  Plus,
  type LucideIcon,
} from 'lucide-vue-next'
import {
  SidebarMenuItem,
  SidebarMenuButton,
  SidebarMenuAction,
} from '@/components/ui/sidebar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import type { FileTree } from '@editor/types/editor.types'
import {Button} from "@/components/ui/button";

const props = defineProps<{
  item: FileTree
  depth: number
  isExpanded?: boolean
}>()

const emit = defineEmits<{
  'toggle-folder': [path: string]
  'file-click': [path: string]
  'create-file': [parentPath: string]
}>()

const PADDING_UNIT_REM = 1
const FILE_ICON_OFFSET_REM = 1.25

const itemPadding = computed(() => {
  const basePadding = props.depth * PADDING_UNIT_REM
  if (props.item.is_directory) {
    return `${basePadding}rem`
  }
    return `${basePadding + FILE_ICON_OFFSET_REM}rem`
})


// Icon mapping
const iconMap: Record<string, LucideIcon> = {
  characters: User,
  locations: MapIcon,
  location: MapIcon,
  lore: Book,
  story: FileText,
  stories: FileText,
  notes: FileText,
  markdown: FileText,
  canvas: PenTool,
  character: User,
  image: Image,
  default: File,
}

// Get appropriate icon component
const getIcon = () => {
  if (props.item.is_directory) {
    return iconMap[props.item.icon || 'default'] || Folder
  }
  return iconMap[props.item.icon || 'default'] || FileText
}

const IconComponent = computed(getIcon) // Make icon component computed

const toggleFolder = () => {
  emit('toggle-folder', props.item.path)
}

const handleFileClick = () => {
  emit('file-click', props.item.path)
}

const handleCreateFile = () => {
  emit('create-file', props.item.path)
}
</script>

<template>
  <SidebarMenuItem class="relative group">
    <template v-if="item.is_directory">
      <!-- Directory Row -->
      <div class="flex items-center w-full">
        <SidebarMenuButton
          @click="toggleFolder"
          :style="{ paddingLeft: itemPadding }"
          class="flex-1 flex items-center gap-1 w-full justify-start group-hover:bg-muted/50"
          :aria-expanded="isExpanded"
        >
          <component
            :is="isExpanded ? ChevronDown : ChevronRight"
            class="h-4 w-4 text-muted-foreground flex-shrink-0"
            aria-hidden="true"
          />
          <component :is="IconComponent" class="h-4 w-4 flex-shrink-0" />
          <span class="truncate">{{ item.name }}</span>
        </SidebarMenuButton>

        <SidebarMenuAction
          class="absolute right-1 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                class="h-6 w-6 text-muted-foreground hover:text-foreground"
              >
                <Plus class="h-3.5 w-3.5" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              <DropdownMenuItem @click="handleCreateFile">
                <FileText class="h-4 w-4 mr-2" />
                New File
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuAction>
      </div>

      <div v-if="isExpanded && item.children && item.children.length > 0" class="mt-1 space-y-1">
        <FileTreeItem
          v-for="child in item.children"
          :key="child.path"
          :item="child"
          :depth="depth + 1"
          :is-expanded="isExpanded"
          @toggle-folder="emit('toggle-folder', $event)"
          @file-click="emit('file-click', $event)"
          @create-file="emit('create-file', $event)"
        />
      </div>
    </template>

    <template v-else>
      <div class="flex items-center w-full">
        <SidebarMenuButton
          @click="handleFileClick"
          :style="{ paddingLeft: itemPadding }"
          class="flex-1 flex items-center gap-1 w-full justify-start group-hover:bg-muted/50"
        >
          <component :is="IconComponent" class="h-4 w-4 flex-shrink-0" />
          <span class="truncate">{{ item.name }}</span>
          <!-- Truncate long names -->
        </SidebarMenuButton>
      </div>
    </template>
  </SidebarMenuItem>
</template>
