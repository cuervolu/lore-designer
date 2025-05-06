<script setup lang="ts">
import {computed} from 'vue'
import {
  ChevronDown, ChevronRight, FileText, User, MapIcon, Book, PenTool, File, Folder, Image,
  PlusCircle, FolderPlus, Edit3, Trash, type LucideIcon
} from 'lucide-vue-next'
import {
  SidebarMenuItem,
  SidebarMenuButton,
} from '@/components/ui/sidebar'
import {
  ContextMenu,
  ContextMenuContent,
  ContextMenuItem,
  ContextMenuSeparator,
  ContextMenuTrigger,
} from '@/components/ui/context-menu'
import type {FileTree} from '@editor/types/editor.types'

const props = defineProps<{
  item: FileTree
  depth: number
  isExpanded?: boolean
}>()

const emit = defineEmits<{
  'toggle-folder': [path: string]
  'file-click': [path: string]
  'create-file': [parentPath: string]
  'create-folder': [parentPath: string]
  'rename-item': [itemPath: string, isDirectory: boolean]
  'delete-item': [itemPath: string, isDirectory: boolean]
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

const handleNewFileInFolder = () => {
  // Reutiliza la lógica existente para abrir el modal,
  // asegurándose de que el parentPath sea el de esta carpeta (item.path)
  emit('create-file', props.item.path)
}

const handleNewFolderInFolder = () => {
  emit('create-folder', props.item.path);
  // TODO: Implementar lógica para crear carpeta (modal, etc.)
}

const handleRename = () => {
  emit('rename-item', props.item.path, props.item.is_directory);
}

const handleDelete = () => {
  emit('delete-item', props.item.path, props.item.is_directory);
}
</script>

<template>
  <ContextMenu>
    <ContextMenuTrigger>
      <SidebarMenuItem class="relative">
        <template v-if="item.is_directory">
          <div class="flex items-center w-full">
            <SidebarMenuButton
              @click="toggleFolder"
              :style="{ paddingLeft: itemPadding }"
              class="flex-1 flex items-center gap-1 w-full justify-start hover:bg-muted/50"
              :aria-expanded="isExpanded"
            >
              <component
                :is="isExpanded ? ChevronDown : ChevronRight"
                class="h-4 w-4 text-muted-foreground flex-shrink-0"
                aria-hidden="true"
              />
              <component :is="IconComponent" class="h-4 w-4 flex-shrink-0"/>
              <span class="truncate">{{ item.name }}</span>
            </SidebarMenuButton>

          </div>

          <div v-if="isExpanded && item.children && item.children.length > 0"
               class="mt-1 space-y-1">
            <FileTreeItem
              v-for="child in item.children"
              :key="child.path"
              :item="child"
              :depth="depth + 1"
              :is-expanded="isExpanded"
              @toggle-folder="emit('toggle-folder', $event)"
              @file-click="emit('file-click', $event)"
              @create-file="emit('create-file', $event)"
              @create-folder="emit('create-folder', $event)"
              @rename-item="emit('rename-item', $event)"
              @delete-item="emit('delete-item', $event)"
            />
          </div>
        </template>

        <template v-else>
          <div class="flex items-center w-full">
            <SidebarMenuButton
              @click="handleFileClick"
              :style="{ paddingLeft: itemPadding }"
              class="flex-1 flex items-center gap-1 w-full justify-start hover:bg-muted/50"
            >
              <component :is="IconComponent" class="h-4 w-4 flex-shrink-0"/>
              <span class="truncate">{{ item.name }}</span>
            </SidebarMenuButton>
          </div>
        </template>
      </SidebarMenuItem>
    </ContextMenuTrigger>

    <ContextMenuContent class="w-48">
      <template v-if="item.is_directory">
        <ContextMenuItem @click="handleNewFileInFolder">
          <PlusCircle class="h-4 w-4 mr-2"/>
          New File
        </ContextMenuItem>
        <ContextMenuItem @click="handleNewFolderInFolder">
          <FolderPlus class="h-4 w-4 mr-2"/>
          New Folder
        </ContextMenuItem>
        <ContextMenuSeparator/>
      </template>
      <ContextMenuItem @click="handleRename">
        <Edit3 class="h-4 w-4 mr-2"/>
        Rename
      </ContextMenuItem>
      <ContextMenuItem @click="handleDelete"
                       class="text-destructive focus:text-destructive focus:bg-destructive/10">
        <Trash class="h-4 w-4 mr-2"/>
        Delete
      </ContextMenuItem>
    </ContextMenuContent>
  </ContextMenu>
</template>
