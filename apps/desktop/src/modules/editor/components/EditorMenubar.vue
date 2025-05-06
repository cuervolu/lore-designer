<script setup lang="ts">
import {open} from '@tauri-apps/plugin-dialog';
import {toast} from 'vue-sonner';
import {ref} from 'vue';
import {useRouter} from 'vue-router';
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
import {useEditorStore} from '@editor/stores/editor.store';
import {usePlatform} from '@common/composables/usePlatform';
import CreateFileModal from './CreateFileModal.vue';
import AboutModal from '@common/components/AboutModal.vue';

const emit = defineEmits(['toggleConsole']);
const router = useRouter();
const editorStore = useEditorStore();
const { cmdKey, shiftKey, plusSeparator } = usePlatform();

// State for create file modal
const isCreateModalOpen = ref(false);
const selectedParentPath = ref('');
const isAboutModalOpen = ref(false);

// Toggle console panel
const handleToggleConsole = () => {
  emit('toggleConsole');
};

// Toggle inspector panel
const handleToggleInspector = () => {
  editorStore.toggleInspector();
};

// Navigate back to workspaces
const goToWorkspaces = () => {
  router.push({name: 'workspaces'});
};

const openAboutModal = () => {
  isAboutModalOpen.value = true;
};

// Create a new file
const handleNewFile = async () => {
  if (!editorStore.currentWorkspace) return;

  try {
    // Open folder selection dialog
    const folderPath = await open({
      directory: true,
      title: 'Select location for new file',
      defaultPath: editorStore.currentWorkspace.path,
    });

    if (!folderPath) return;
    selectedParentPath.value = folderPath.replace(`${editorStore.currentWorkspace.path}/`, '');
    isCreateModalOpen.value = true;
  } catch (err) {
    console.error('Failed to select directory:', err);
    toast.error('Failed to select directory');
  }
};

// Handle create file from modal
const handleCreateFile = async (fileName: string, extension: string, initialContent: string, parentPath: string) => {
  try {
    const filePath = await editorStore.createNewFile(parentPath, fileName, initialContent);
    if (filePath) {
      toast.success(`Created ${extension} file successfully`);
      // Auto-open the file
      await editorStore.openFile(filePath);
    }
  } catch (err) {
    console.error('Failed to create file:', err);
    toast.error('Failed to create file');
  }
};

// Open a file
const handleOpenFile = async () => {
  if (!editorStore.currentWorkspace) return;

  try {
    const filePath = await open({
      directory: false,
      multiple: false,
      title: 'Open File',
      defaultPath: editorStore.currentWorkspace.path,
    });

    if (!filePath) return;

    // Get relative path within workspace
    const relativePath = filePath.replace(`${editorStore.currentWorkspace.path}/`, '');

    // Open the file
    await editorStore.openFile(relativePath);
  } catch (err) {
    console.error('Failed to open file:', err);
    toast.error('Failed to open file');
  }
};

// Save active file
const handleSave = async () => {
  if (!editorStore.activeTab) {
    toast.error('No active file to save');
    return;
  }

  // The actual save is handled in the EditorContent component
  // This just triggers a save action that will be implemented later
  toast.info('Save action triggered', {
    description: 'Save functionality will be implemented in the editor component'
  });
};

// Reload file tree
const handleReloadFileTree = async () => {
  await editorStore.loadFileTree();
  toast.success('File tree refreshed');
};
</script>

<template>
  <Menubar class="rounded-none border-t-0 border-x-0 bg-[--menubar-background]">
    <MenubarMenu>
      <MenubarTrigger>File</MenubarTrigger>
      <MenubarContent>
        <MenubarItem @click="handleNewFile">
          New File
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}N</MenubarShortcut>
        </MenubarItem>
        <MenubarItem @click="handleOpenFile">
          Open File
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}O</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="handleSave">
          Save
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}S</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Save As
          <MenubarShortcut>{{ shiftKey }}{{ plusSeparator }}{{ cmdKey }}{{ plusSeparator }}S</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Close File
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}W</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="goToWorkspaces">
          Exit to Workspaces
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>Edit</MenubarTrigger>
      <MenubarContent>
        <MenubarItem>
          Undo
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}Z</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Redo
          <MenubarShortcut>{{ shiftKey }}{{ plusSeparator }}{{ cmdKey }}{{ plusSeparator }}Z</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Cut
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}X</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Copy
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}C</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Paste
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}V</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarSub>
          <MenubarSubTrigger>Find</MenubarSubTrigger>
          <MenubarSubContent>
            <MenubarItem>
              Find
              <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}F</MenubarShortcut>
            </MenubarItem>
            <MenubarItem>
              Replace
              <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}H</MenubarShortcut>
            </MenubarItem>
            <MenubarItem>
              Find in Files
              <MenubarShortcut>{{ shiftKey }}{{ plusSeparator }}{{ cmdKey }}{{ plusSeparator }}F</MenubarShortcut>
            </MenubarItem>
          </MenubarSubContent>
        </MenubarSub>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>Workspace</MenubarTrigger>
      <MenubarContent>
        <MenubarItem @click="goToWorkspaces">
          Workspaces Home
        </MenubarItem>
        <MenubarItem>
          New Workspace
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="handleReloadFileTree">
          Refresh File Tree
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
        <MenubarCheckboxItem
          :checked="editorStore.showConsole"
          @click="handleToggleConsole"
        >
          Show Console
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}J</MenubarShortcut>
        </MenubarCheckboxItem>
        <MenubarCheckboxItem
          :checked="editorStore.showInspector"
          @click="handleToggleInspector"
        >
          Show Inspector
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}I</MenubarShortcut>
        </MenubarCheckboxItem>
        <MenubarSeparator/>
        <MenubarItem>
          Zoom In
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}+</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Zoom Out
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}-</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          Reset Zoom
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}0</MenubarShortcut>
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
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}K {{ cmdKey }}{{ plusSeparator }}S</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          Check for Updates
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="openAboutModal">
          About Lore Designer
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>
  </Menubar>

  <!-- Create File Modal -->
  <CreateFileModal
    v-model:is-open="isCreateModalOpen"
    :parent-path="selectedParentPath"
    @create="handleCreateFile"
  />
  <AboutModal
    v-model:is-open="isAboutModalOpen"
  />
</template>
