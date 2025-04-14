<script setup lang="ts">
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'

import {
  Minus,
  Square,
  X,
  Pen,
  Save,
  FolderOpen,
  FileEdit,
  Settings,
  HelpCircle
} from 'lucide-vue-next'

// This function would be implemented to handle window actions via Tauri
const handleWindowAction = (action: string) => {
  console.log(`Window action: ${action}`)
  // Actual implementation would use Tauri's window API
}

// Menu structures for the main menu bar
const fileMenuItems = [
  { label: 'New Project', icon: FileEdit, action: 'new' },
  { label: 'Open Project', icon: FolderOpen, action: 'open' },
  { label: 'Save', icon: Save, action: 'save' },
  { label: 'Save As...', icon: Save, action: 'saveAs' }
]

const editMenuItems = [
  { label: 'Undo', shortcut: 'Ctrl+Z', action: 'undo' },
  { label: 'Redo', shortcut: 'Ctrl+Y', action: 'redo' },
  { label: 'Cut', shortcut: 'Ctrl+X', action: 'cut' },
  { label: 'Copy', shortcut: 'Ctrl+C', action: 'copy' },
  { label: 'Paste', shortcut: 'Ctrl+V', action: 'paste' }
]

const helpMenuItems = [
  { label: 'Documentation', icon: FileEdit, action: 'docs' },
  { label: 'Keyboard Shortcuts', icon: Settings, action: 'shortcuts' },
  { label: 'About Lore Designer', icon: HelpCircle, action: 'about' }
]
</script>

<template>
  <div
    class="flex h-9 items-center border-b bg-background px-2"
    data-tauri-drag-region
  >
    <!-- App logo and title -->
    <div class="flex items-center mr-2" data-tauri-drag-region>
      <Pen class="h-4 w-4 mr-2" />
      <span class="text-sm font-medium" data-tauri-drag-region
      >Lore Designer</span
      >
    </div>

    <!-- Menu Bar -->
    <div class="flex items-center space-x-1">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" class="h-7 px-2"> File</Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem v-for="item in fileMenuItems" :key="item.action">
            <component :is="item.icon" class="mr-2 h-4 w-4" />
            <span>{{ item.label }}</span>
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem>
            <X class="mr-2 h-4 w-4" />
            <span>Exit</span>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" class="h-7 px-2"> Edit</Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem v-for="item in editMenuItems" :key="item.action">
            <span>{{ item.label }}</span>
            <span class="ml-auto text-xs text-muted-foreground">{{
                item.shortcut
              }}</span>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" class="h-7 px-2"> Help</Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem v-for="item in helpMenuItems" :key="item.action">
            <component :is="item.icon" class="mr-2 h-4 w-4" />
            <span>{{ item.label }}</span>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>

    <!-- Spacer that also serves as a drag region -->
    <div class="flex-1" data-tauri-drag-region></div>

    <!-- Window Controls -->
    <div class="flex items-center">
      <Button
        variant="ghost"
        size="icon"
        class="h-7 w-7"
        @click="handleWindowAction('minimize')"
      >
        <Minus class="h-4 w-4" />
      </Button>
      <Button
        variant="ghost"
        size="icon"
        class="h-7 w-7"
        @click="handleWindowAction('maximize')"
      >
        <Square class="h-4 w-4" />
      </Button>
      <Button
        variant="ghost"
        size="icon"
        class="h-7 w-7 hover:bg-destructive hover:text-destructive-foreground"
        @click="handleWindowAction('close')"
      >
        <X class="h-4 w-4" />
      </Button>
    </div>
  </div>
</template>

