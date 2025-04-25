import { ref, readonly } from 'vue';

// Create a reactive title state that can be shared across components
const appTitle = ref('Lore Designer');

/**
 * Simple composable for managing the application title across components
 */
export function useAppTitle() {
  /**
   * Set the application title
   *
   * @param newTitle The new title to set
   */
  function setTitle(newTitle: string) {
    appTitle.value = newTitle;
    // Also update the document title
    document.title = newTitle;
  }

  /**
   * Set the title in editor mode format: [fileName].[fileExt] - [workspaceName]
   *
   * @param fileName File name
   * @param fileExt File extension (without the dot)
   * @param workspaceName Workspace name
   */
  function setEditorTitle(fileName: string, fileExt?: string, workspaceName?: string) {
    const extension = fileExt ? `.${fileExt}` : '';
    let title = `${fileName}${extension}`;

    if (workspaceName) {
      title += ` - ${workspaceName}`;
    }

    setTitle(title);
  }

  /**
   * Reset the title to the default
   */
  function resetTitle() {
    setTitle('Lore Designer');
  }

  return {
    title: readonly(appTitle),
    setTitle,
    setEditorTitle,
    resetTitle
  };
}
