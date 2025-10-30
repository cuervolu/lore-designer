<script setup lang="ts">
import {open} from '@tauri-apps/plugin-dialog';
import {toast} from 'vue-sonner';
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import { useI18n } from 'vue-i18n';
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
const { t } = useI18n();


const isCreateModalOpen = ref(false);
const selectedParentPath = ref('');
const isAboutModalOpen = ref(false);


const handleToggleConsole = () => {
  emit('toggleConsole');
};


const handleToggleInspector = () => {
  editorStore.toggleInspector();
};


const goToWorkspaces = () => {
  router.push({name: 'workspaces'});
};

const openAboutModal = () => {
  isAboutModalOpen.value = true;
};

const handleNewFile = async () => {
  if (!editorStore.currentWorkspace) return;

  try {
    // Open folder selection dialog
    const folderPath = await open({
      directory: true,
      title: t('editor.messages.selectNewFileLocation'),
      defaultPath: editorStore.currentWorkspace.path,
    });

    if (!folderPath) return;
    selectedParentPath.value = folderPath.replace(`${editorStore.currentWorkspace.path}/`, '');
    isCreateModalOpen.value = true;
  } catch (err) {
    console.error('Failed to select directory:', err);
    toast.error(t('editor.messages.failedToSelectDir'));
  }
};

const handleCreateFile = async (fileName: string, extension: string, initialContent: string, parentPath: string) => {
  try {
    const filePath = await editorStore.createNewFile(parentPath, fileName, initialContent);
    if (filePath) {
      toast.success(t('editor.messages.fileCreatedSuccess', { extension }));
      await editorStore.openFile(filePath);
    }
  } catch (err) {
    console.error('Failed to create file:', err);
    toast.error(t('editor.messages.failedToCreateFile'));
  }
};

const handleOpenFile = async () => {
  if (!editorStore.currentWorkspace) return;

  try {
    const filePath = await open({
      directory: false,
      multiple: false,
      title: t('editor.messages.openFileTitle'),
      defaultPath: editorStore.currentWorkspace.path,
    });

    if (!filePath) return;

    // Get relative path within workspace
    const relativePath = filePath.replace(`${editorStore.currentWorkspace.path}/`, '');

    // Open the file
    await editorStore.openFile(relativePath);
  } catch (err) {
    console.error('Failed to open file:', err);
    toast.error(t('editor.messages.failedToOpenFile'));
  }
};

// Save active file
const handleSave = async () => {
  if (!editorStore.activeTab) {
    toast.error(t('editor.messages.noActiveFileToSave'));
    return;
  }

  // The actual save is handled in the EditorContent component
  // This just triggers a save action that will be implemented later
  toast.info(t('editor.messages.saveTriggered'), {
    description: t('editor.messages.saveWillBeImplemented')
  });
};

// Reload file tree
const handleReloadFileTree = async () => {
  await editorStore.loadFileTree();
  toast.success(t('editor.messages.fileTreeRefreshed'));
};
</script>

<template>
  <Menubar class="rounded-none border-t-0 border-x-0 bg-[--menubar-background]">
    <MenubarMenu>
      <MenubarTrigger>{{ t('menu.file') }}</MenubarTrigger>
      <MenubarContent>
        <MenubarItem @click="handleNewFile">
          {{ t('menu.newFile') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}N</MenubarShortcut>
        </MenubarItem>
        <MenubarItem @click="handleOpenFile">
          {{ t('menu.openFile') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}O</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="handleSave">
          {{ t('menu.save') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}S</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.saveAs') }}
          <MenubarShortcut>{{ shiftKey }}{{ plusSeparator }}{{ cmdKey }}{{ plusSeparator }}S</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          {{ t('menu.closeFile') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}W</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="goToWorkspaces">
          {{ t('menu.exitToWorkspaces') }}
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>{{ t('menu.edit') }}</MenubarTrigger>
      <MenubarContent>
        <MenubarItem>
          {{ t('menu.undo') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}Z</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.redo') }}
          <MenubarShortcut>{{ shiftKey }}{{ plusSeparator }}{{ cmdKey }}{{ plusSeparator }}Z</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          {{ t('menu.cut') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}X</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.copy') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}C</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.paste') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}V</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarSub>
          <MenubarSubTrigger>{{ t('menu.findSubMenu') }}</MenubarSubTrigger>
          <MenubarSubContent>
            <MenubarItem>
              {{ t('menu.find') }}
              <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}F</MenubarShortcut>
            </MenubarItem>
            <MenubarItem>
              {{ t('menu.replace') }}
              <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}H</MenubarShortcut>
            </MenubarItem>
            <MenubarItem>
              {{ t('menu.findInFiles') }}
              <MenubarShortcut>{{ shiftKey }}{{ plusSeparator }}{{ cmdKey }}{{ plusSeparator }}F</MenubarShortcut>
            </MenubarItem>
          </MenubarSubContent>
        </MenubarSub>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>{{ t('menu.workspace') }}</MenubarTrigger>
      <MenubarContent>
        <MenubarItem @click="goToWorkspaces">
          {{ t('menu.workspacesHome') }}
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.newWorkspace') }}
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="handleReloadFileTree">
          {{ t('menu.refreshFileTree') }}
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          {{ t('menu.workspaceSettings') }}
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>{{ t('menu.view') }}</MenubarTrigger>
      <MenubarContent>
        <MenubarCheckboxItem
          :checked="editorStore.showConsole"
          @click="handleToggleConsole"
        >
          {{ t('menu.showConsole') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}J</MenubarShortcut>
        </MenubarCheckboxItem>
        <MenubarCheckboxItem
          :checked="editorStore.showInspector"
          @click="handleToggleInspector"
        >
          {{ t('menu.showInspector') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}I</MenubarShortcut>
        </MenubarCheckboxItem>
        <MenubarSeparator/>
        <MenubarItem>
          {{ t('menu.zoomIn') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}+</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.zoomOut') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}-</MenubarShortcut>
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.resetZoom') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}0</MenubarShortcut>
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>

    <MenubarMenu>
      <MenubarTrigger>{{ t('menu.help') }}</MenubarTrigger>
      <MenubarContent>
        <MenubarItem>
          {{ t('menu.documentation') }}
        </MenubarItem>
        <MenubarItem>
          {{ t('menu.keyboardShortcuts') }}
          <MenubarShortcut>{{ cmdKey }}{{ plusSeparator }}K {{ cmdKey }}{{ plusSeparator }}S</MenubarShortcut>
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem>
          {{ t('menu.checkUpdates') }}
        </MenubarItem>
        <MenubarSeparator/>
        <MenubarItem @click="openAboutModal">
          {{ t('menu.about') }}
        </MenubarItem>
      </MenubarContent>
    </MenubarMenu>
  </Menubar>

  <CreateFileModal
    v-model:is-open="isCreateModalOpen"
    :parent-path="selectedParentPath"
    @create="handleCreateFile"
  />
  <AboutModal
    v-model:is-open="isAboutModalOpen"
  />
</template>
