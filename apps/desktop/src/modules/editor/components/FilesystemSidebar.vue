<script setup lang="ts">
import {computed, ref} from 'vue';
import {Input} from '@/components/ui/input';
import {
  Search,
  ChevronDown,
  ChevronRight,
  FileText,
  User,
  Home,
  Map,
  Book,
  PenTool,
  File,
  Folder,
  LayoutTemplate,
  Trash2
} from 'lucide-vue-next';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton
} from '@/components/ui/sidebar';
import { ScrollArea } from '@/components/ui/scroll-area';

// Mock filesystem data
const workspaceName = 'My Fantasy Novel';
const searchQuery = ref('');

const fileTree = ref([
  {
    id: 'characters',
    name: 'Characters',
    type: 'folder',
    icon: Folder,
    expanded: true,
    children: [
      {
        id: 'protagonist',
        name: 'Protagonist',
        type: 'file',
        icon: User,
        extension: 'md',
        active: true
      },
      {
        id: 'antagonist',
        name: 'Antagonist',
        type: 'file',
        icon: User,
        extension: 'md',
        active: false
      }
    ]
  },
  {
    id: 'lore',
    name: 'Lore',
    type: 'folder',
    icon: Folder,
    expanded: true,
    children: [
      {
        id: 'kingdom',
        name: 'Kingdom',
        type: 'file',
        icon: Home,
        extension: 'md',
        active: false
      },
      {
        id: 'magicSystem',
        name: 'Magic System',
        type: 'file',
        icon: Book,
        extension: 'md',
        active: false
      }
    ]
  },
  {
    id: 'places',
    name: 'Places',
    type: 'folder',
    icon: Folder,
    expanded: false,
    children: [
      {
        id: 'castle',
        name: 'Castle',
        type: 'file',
        icon: Map,
        extension: 'md',
        active: false
      }
    ]
  },
  {
    id: 'story',
    name: 'Story',
    type: 'folder',
    icon: Folder,
    expanded: false,
    children: [
      {
        id: 'chapter1',
        name: 'Chapter 1',
        type: 'file',
        icon: FileText,
        extension: 'md',
        active: false
      },
      {
        id: 'canvas',
        name: 'Canvas',
        type: 'file',
        icon: PenTool,
        extension: 'canvas',
        active: false
      }
    ]
  },
  {
    id: 'notes',
    name: 'Notes',
    type: 'folder',
    icon: Folder,
    expanded: false,
    children: [
      {
        id: 'brainstorming',
        name: 'Brainstorming',
        type: 'file',
        icon: FileText,
        extension: 'md',
        active: false
      }
    ]
  },
  {
    id: 'icon',
    name: 'icon.svg',
    type: 'file',
    icon: File,
    extension: 'svg',
    active: false
  }
]);

const toggleFolder = (folder: any) => {
  folder.expanded = !folder.expanded;
};

// Filter files based on search query
const filteredFileTree = computed(() => {
  if (!searchQuery.value) return fileTree.value;

  // Simple filtering logic - in a real app, this would be more sophisticated
  const query = searchQuery.value.toLowerCase();
  return fileTree.value.filter(item =>
    item.name.toLowerCase().includes(query) ||
    (item.children?.some(child => child.name.toLowerCase().includes(query)))
  );
});
</script>

<template>
  <Sidebar class="border-r h-full flex flex-col">
    <!-- Added pt-9 to push the header content below the menubar -->
    <SidebarHeader class="border-b px-2 py-1.5 pt-9">
      <h3 class="font-semibold">FileSystem</h3>
      <div class="relative mt-2">
        <Input
          v-model="searchQuery"
          placeholder="Search files..."
          class="pl-8 h-8"
        />
        <Search
          class="h-4 w-4 absolute left-2.5 top-1/2 transform -translate-y-1/2 text-muted-foreground"/>
      </div>
    </SidebarHeader>

    <!-- Wrapped SidebarContent in ScrollArea for better scrolling -->
    <SidebarContent class="flex-1">
      <ScrollArea class="h-full">
        <SidebarGroup>
          <SidebarGroupLabel>
            <Folder class="h-4 w-4 mr-2"/>
            {{ workspaceName }}
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <template v-for="item in filteredFileTree" :key="item.id">
                <!-- Folder -->
                <SidebarMenuItem v-if="item.type === 'folder'">
                  <div class="space-y-1 w-full">
                    <SidebarMenuButton
                      @click="toggleFolder(item)"
                      class="flex items-center gap-1 w-full justify-start"
                    >
                      <component
                        :is="item.expanded ? ChevronDown : ChevronRight"
                        class="h-4 w-4 text-muted-foreground"
                      />
                      <component :is="item.icon" class="h-4 w-4"/>
                      <span>{{ item.name }}</span>
                    </SidebarMenuButton>

                    <!-- Children -->
                    <div v-if="item.expanded" class="pl-4 space-y-1">
                      <SidebarMenuButton
                        v-for="child in item.children"
                        :key="child.id"
                        :isActive="child.active"
                        class="flex items-center gap-1 w-full justify-start"
                      >
                        <component :is="child.icon" class="h-4 w-4"/>
                        <span>{{ child.name }}</span>
                      </SidebarMenuButton>
                    </div>
                  </div>
                </SidebarMenuItem>

                <!-- File -->
                <SidebarMenuItem v-else>
                  <SidebarMenuButton
                    :isActive="item.active"
                    class="flex items-center gap-1 w-full justify-start ml-5"
                  >
                    <component :is="item.icon" class="h-4 w-4"/>
                    <span>{{ item.name }}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              </template>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </ScrollArea>
    </SidebarContent>

    <!-- Footer Buttons - Added padding-bottom and changed to horizontal layout -->
    <SidebarFooter class="border-t pb-8">
      <SidebarMenu>
        <div class="flex items-center justify-between px-2">
          <SidebarMenuItem class="flex-1">
            <SidebarMenuButton class="gap-1 justify-center">
              <LayoutTemplate class="h-4 w-4"/>
              <span>Templates</span>
            </SidebarMenuButton>
          </SidebarMenuItem>

          <div class="w-px h-6 bg-muted mx-1"></div>

          <SidebarMenuItem class="flex-1">
            <SidebarMenuButton class="gap-1 justify-center">
              <Trash2 class="h-4 w-4"/>
              <span>Trash</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </div>
      </SidebarMenu>
    </SidebarFooter>
  </Sidebar>
</template>
