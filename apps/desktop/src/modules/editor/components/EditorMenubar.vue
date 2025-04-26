<script setup lang="ts">
import {
  Menubar,
  MenubarCheckboxItem,
  MenubarContent,
  MenubarItem,
  MenubarMenu,
  MenubarSeparator,
  MenubarShortcut,
  MenubarSub,
  MenubarSubContent,
  MenubarSubTrigger,
  MenubarTrigger,
} from '@/components/ui/menubar';

const emit = defineEmits(['toggleConsole']);

const handleToggleConsole = () => {
  emit('toggleConsole');
};
</script>

<template>
  <Menubar class="rounded-none border-t-0 border-x-0">
    <MenubarMenu>
      <MenubarTrigger>File</MenubarTrigger>
      <MenubarContent>
        <MenubarItem>
          New File
          <MenubarShortcut>⌘N</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Open File
          <MenubarShortcut>⌘O</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Save
          <MenubarShortcut>⌘S</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Save As
          <MenubarShortcut>⇧⌘S</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Close File
          <MenubarShortcut>⌘W</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Exit
          <MenubarShortcut>Alt+F4</MenubarShortcut>
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>Edit</MenubarTrigger>
      <MenubarContent>
        <MenubarItem>
          Undo
          <MenubarShortcut>⌘Z</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Redo
          <MenubarShortcut>⇧⌘Z</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Cut
          <MenubarShortcut>⌘X</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Copy
          <MenubarShortcut>⌘C</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Paste
          <MenubarShortcut>⌘V</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarSub>
          <MenubarSubTrigger>Find</MenubarSubTrigger>
          <MenubarSubContent>
            <MenubarItem>
              Find
              <MenubarShortcut>⌘F</MenubarShortcut>
            </MenubarItem>
            <MenubarItem>
              Replace
              <MenubarShortcut>⌘H</MenubarShortcut>
            </MenubarItem>
            <MenubarItem>
              Find in Files
              <MenubarShortcut>⇧⌘F</MenubarShortcut>
            </MenubarItem>
          </MenubarSubContent>
        </MenubarSub>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>Workspace</MenubarTrigger>
      <MenubarContent>
        <MenubarItem>
          Open Workspace
          <MenubarShortcut>⌘O</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          New Workspace
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Add Folder to Workspace
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Workspace Settings
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>View</MenubarTrigger>
      <MenubarContent>
        <MenubarCheckboxItem @click="handleToggleConsole">
          Show Console
          <MenubarShortcut>⌘J</MenubarShortcut>
        </MenubarCheckboxItem>
        <MenubarCheckboxItem>
          Show Explorer
          <MenubarShortcut>⌘E</MenubarShortcut>
        </MenubarCheckboxItem>
        <MenubarCheckboxItem>
          Show Inspector
          <MenubarShortcut>⌘I</MenubarShortcut>
        </MenubarCheckboxItem>
        <MenubarSeparator/>
        <MenubarItem>
          Zoom In
          <MenubarShortcut>⌘+</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Zoom Out
          <MenubarShortcut>⌘-</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Reset Zoom
          <MenubarShortcut>⌘0</MenubarShortcut>
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>Help</MenubarTrigger>
      <MenubarContent>
        <MenubarItem>
          Documentation
        </MenubarItem>
        <MenubarItem>
          Keyboard Shortcuts
          <MenubarShortcut>⌘K ⌘S</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Check for Updates
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          About Lore Designer
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>
  </Menubar>
</template>
