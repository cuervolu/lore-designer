import { getCurrentWindow } from '@tauri-apps/api/window';
import { error as logError} from '@tauri-apps/plugin-log'
import { ref, readonly } from 'vue';

// Create a reactive title state that can be shared across components
const appTitle = ref('Lore Designer');

// Default window title
const DEFAULT_TITLE = 'Lore Designer';

/**
 * Simple composable for managing the application title across components
 */
export function useAppTitle() {
  /**
   * Set the application title
   *
   * @param newTitle The new title to set
   * @param updateNativeTitle Whether to update the native window title (defaults to false)
   */
  async function setTitle(newTitle: string, updateNativeTitle = false) {
    appTitle.value = newTitle;

    // Update the document title
    document.title = newTitle;

    // Only update the native window title if specified
    if (updateNativeTitle) {
      try {
        // Get the current Tauri window and update its native title
        const appWindow = getCurrentWindow();
        await appWindow.setTitle(newTitle);
      } catch (err) {
        await logError(`Failed to set window title: ${err}`);
      }
    }
  }

  /**
   * Set the title in editor mode format: [fileName] - [workspaceName]
   * In editor mode, we update both the visual title and the native window title
   *
   * @param fileName File name
   * @param fileExt File extension (without the dot)
   * @param workspaceName Workspace name
   * @param hasUnsavedChanges Indicates if the file has unsaved changes
   */
  async function setEditorTitle(
    fileName: string,
    fileExt?: string,
    workspaceName?: string,
    hasUnsavedChanges?: boolean
  ) {
    let baseFileName = fileName;
    if (fileExt && baseFileName.toLowerCase().endsWith(`.${fileExt.toLowerCase()}`)) {
      baseFileName = baseFileName.slice(0, -(fileExt.length + 1));
    }

    let displayFileName = baseFileName;
    if (fileExt) {
      displayFileName += `.${fileExt}`;
    }

    // Prepend indicator if there are unsaved changes
    const unsavedIndicator = hasUnsavedChanges ? '• ' : '';

    let finalTitle = `${unsavedIndicator}${displayFileName}`;

    if (workspaceName) {
      finalTitle += ` - ${workspaceName}`;
    } else {
      // Fallback if workspace name is missing, but still show unsaved status
      finalTitle = `${unsavedIndicator}${displayFileName} - Lore Designer`;
    }

    await setTitle(finalTitle, true);
  }

  /**
   * Reset the title to the default
   *
   * @param updateNativeTitle Whether to update the native window title (defaults to true)
   */
  async function resetTitle(updateNativeTitle = true) {
    await setTitle(DEFAULT_TITLE, updateNativeTitle);
  }

  /**
   * Set a page title in the wizard (only changes UI title, not native window title)
   *
   * @param pageTitle The page title to set
   */
  async function setWizardPageTitle(pageTitle: string) {
    // For wizard pages, we only update the UI title, not the native window title
    await setTitle(pageTitle, false);
  }

  return {
    title: readonly(appTitle),
    setTitle,
    setEditorTitle,
    resetTitle,
    setWizardPageTitle
  };
}
