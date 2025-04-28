<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Input } from '@/components/ui/input';
import {
  Search,
  Folder,
  LayoutTemplate,
  Trash2,
  RefreshCw
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
import { useEditorStore } from '@editor/stores/editor.store';
import { toast } from 'vue-sonner';
import FileTreeItem from './FileTreeItem.vue';
import CreateFileModal from './CreateFileModal.vue';
import type { FileTree } from "@editor/types/editor.types.ts";
import { Button } from '@/components/ui/button'

const editorStore = useEditorStore();
const searchQuery = ref('');
const expandedFolders = ref<Record<string, boolean>>({});
const isCreateModalOpen = ref(false);
const selectedParentPath = ref('');

// Computed property to filter file tree based on search query
const filteredFileTree = computed(() => {
  if (!searchQuery.value.trim()) {
    return editorStore.fileTree;
  }

  const query = searchQuery.value.toLowerCase();

  // Helper function to search recursively
  const searchInTree = (items: FileTree[]): FileTree[] => {
    return items.filter(item => {
      // Check if this item matches
      const nameMatches = item.name.toLowerCase().includes(query);

      // For directories, also check children
      if (item.is_directory && item.children.length > 0) {
        // Search in children
        const matchingChildren = searchInTree(item.children);

        // If children match, include this directory and its matching children
        if (matchingChildren.length > 0) {
          // Make a copy with only matching children
          return {
            ...item,
            children: matchingChildren
          };
        }
      }

      return nameMatches;
    });
  };

  return searchInTree(editorStore.fileTree);
});

// Refresh file tree
const refreshFileTree = async () => {
  await editorStore.loadFileTree();
  toast.success('File tree refreshed');
};

// Handle file click to open it
const handleFileClick = async (path: string) => {
  await editorStore.openFile(path);
};

// Open create file modal
const openCreateFileModal = (parentPath: string) => {
  selectedParentPath.value = parentPath;
  isCreateModalOpen.value = true;
};

// Create new file with data from modal
const handleCreateFile = async (fileName: string, extension: string, initialContent: string, parentPath: string) => {
  try {
    const filePath = await editorStore.createNewFile(parentPath, fileName, initialContent);
    if (filePath) {
      // Ensure parent folder is expanded
      expandedFolders.value[parentPath] = true;
      toast.success(`Created ${extension} file successfully`);
    }
  } catch (err) {
    console.error('Failed to create file:', err);
    toast.error('Failed to create file');
  }
};

// Toggle folder expansion
const toggleFolder = (folderPath: string) => {
  expandedFolders.value[folderPath] = !expandedFolders.value[folderPath];
};

// Initialize expanded folders when component mounts
onMounted(() => {
  // Auto-expand the standard folders
  const standardFolders = ['Characters', 'Lore', 'Story', 'Notes'];

  for (const item of editorStore.fileTree) {
    if (item.is_directory && standardFolders.includes(item.name)) {
      expandedFolders.value[item.path] = true;
    }
  }
});
</script>

<template>
  <Sidebar class="border-r h-full flex flex-col">
    <!-- Header with search -->
    <SidebarHeader class="border-b px-2 py-1.5">
      <div class="flex items-center justify-between">
        <h3 class="font-semibold">Explorer</h3>
        <Button variant="ghost"
          @click="refreshFileTree"
          class="text-muted-foreground hover:text-foreground p-1 rounded-md hover:bg-muted"
          title="Refresh file tree"
        >
          <RefreshCw class="h-4 w-4" />
        </Button>
      </div>
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

    <!-- Workspace content -->
    <SidebarContent class="flex-1">
      <ScrollArea class="h-full">
        <SidebarGroup>
          <SidebarGroupLabel v-if="editorStore.currentWorkspace">
            <Folder class="h-4 w-4 mr-2"/>
            {{ editorStore.currentWorkspace.name }}
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <template v-if="filteredFileTree.length > 0">
                <FileTreeItem
                  v-for="item in filteredFileTree"
                  :key="item.path"
                  :item="item"
                  :depth="0"
                  :is-expanded="expandedFolders[item.path]"
                  @toggle-folder="toggleFolder"
                  @file-click="handleFileClick"
                  @create-file="openCreateFileModal"
                >
                  <template v-if="item.is_directory && expandedFolders[item.path] && item.children.length > 0" #children>
                    <FileTreeItem
                      v-for="child in item.children"
                      :key="child.path"
                      :item="child"
                      :depth="1"
                      :is-expanded="expandedFolders[child.path]"
                      @toggle-folder="toggleFolder"
                      @file-click="handleFileClick"
                      @create-file="openCreateFileModal"
                    >
                      <template v-if="child.is_directory && expandedFolders[child.path] && child.children.length > 0" #children>
                        <FileTreeItem
                          v-for="grandchild in child.children"
                          :key="grandchild.path"
                          :item="grandchild"
                          :depth="2"
                          :is-expanded="expandedFolders[grandchild.path]"
                          @toggle-folder="toggleFolder"
                          @file-click="handleFileClick"
                          @create-file="openCreateFileModal"
                        >
                          <!-- Deeper nesting levels could be added if needed -->
                        </FileTreeItem>
                      </template>
                    </FileTreeItem>
                  </template>
                </FileTreeItem>
              </template>
              <div
                v-else-if="searchQuery"
                class="px-2 py-4 text-sm text-muted-foreground text-center"
              >
                No files matching "{{ searchQuery }}"
              </div>
              <div
                v-else
                class="px-2 py-4 text-sm text-muted-foreground text-center"
              >
                Loading files...
              </div>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </ScrollArea>
    </SidebarContent>

    <!-- Footer Buttons -->
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

    <!-- Create File Modal -->
    <CreateFileModal
      v-model:is-open="isCreateModalOpen"
      :parent-path="selectedParentPath"
      @create="handleCreateFile"
    />
  </Sidebar>
</template>
