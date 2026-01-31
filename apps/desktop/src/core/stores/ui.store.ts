import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUiStore = defineStore('ui', () => {
  const isAboutModalOpen = ref(false);
  const isCreateFileModalOpen = ref(false);
  const createFileModalParentPath = ref<string | undefined>();

  function openAboutModal() {
    isAboutModalOpen.value = true;
  }

  function closeAboutModal() {
    isAboutModalOpen.value = false;
  }

  function openCreateFileModal(parentPath?: string) {
    createFileModalParentPath.value = parentPath;
    isCreateFileModalOpen.value = true;
  }

  function closeCreateFileModal() {
    isCreateFileModalOpen.value = false;
  }

  return {
    // State
    isAboutModalOpen,
    isCreateFileModalOpen,
    createFileModalParentPath,

    // Actions
    openAboutModal,
    closeAboutModal,
    openCreateFileModal,
    closeCreateFileModal,
  };
});
