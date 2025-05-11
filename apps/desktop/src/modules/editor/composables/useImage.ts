import {convertFileSrc} from "@tauri-apps/api/core";
import {
  basename,
  extname,
  join,
} from "@tauri-apps/api/path";
import {error as logError, warn} from "@tauri-apps/plugin-log";
import {open as openDialog} from "@tauri-apps/plugin-dialog";
import {
  copyFile,
  create,
  exists,
} from "@tauri-apps/plugin-fs";
import {ref} from "vue";

import {useEditorStore} from "@editor/stores/editor.store";
import type {
  ManagedImage,
  ImageManagerError,
} from "@editor/types/image.types";

export function useImage() {
  const editorStore = useEditorStore();
  const isProcessing = ref(false);
  const error = ref<ImageManagerError | null>(null);

  const ASSETS_SUBDIR = ".lore/assets/images";

  /**
   * Sanitizes a filename by replacing non-alphanumeric characters (excluding dots and hyphens) with underscores.
   * @param filename - The original filename.
   * @returns The sanitized filename.
   */
  const sanitizeFilename = (filename: string): string => {
    // Keep the extension separate if present
    const ext = filename.includes(".") ? filename.substring(filename.lastIndexOf(".")) : "";
    const nameWithoutExt = ext ? filename.substring(0, filename.length - ext.length) : filename;
    const sanitizedName = nameWithoutExt.replace(/[^a-zA-Z0-9_-]/g, "_");
    return sanitizedName + ext;
  };


  /**
   * Prompts the user to select an image, copies it to the workspace assets,
   * and returns paths for display and storage.
   * @returns A Promise resolving to ManagedImage or null if cancelled/error.
   */
  const selectAndCopyImageToWorkspace = async (): Promise<ManagedImage | null> => {
    isProcessing.value = true;
    error.value = null;

    if (!editorStore.currentWorkspace?.path) {
      error.value = {
        type: "NoWorkspace",
        message: "No active workspace to save the image.",
      };
      isProcessing.value = false;
      return null;
    }

    try {
      const selected = await openDialog({
        multiple: false,
        directory: false,
        filters: [
          {
            name: "Images",
            extensions: ["png", "jpg", "jpeg", "gif", "webp", "svg"],
          },
        ],
      });

      if (!selected) {
        // User cancelled the dialog
        error.value = {type: "DialogCancelled", message: "Image selection was cancelled."};
        isProcessing.value = false;
        return null;
      }

      const selectedPath = selected;
      const originalFileNameWithExt = await basename(selectedPath);
      const ext = await extname(selectedPath);
      const originalFileNameWithoutExt = originalFileNameWithExt.substring(
        0,
        originalFileNameWithExt.length - ext.length
      );

      const sanitizedBaseName = sanitizeFilename(originalFileNameWithoutExt);
      // Ensure unique name to prevent overwrites and caching issues
      const uniqueFileName = `${Date.now()}-${sanitizedBaseName}${ext}`;

      const workspaceAssetsDir = await join(
        editorStore.currentWorkspace.path,
        ASSETS_SUBDIR
      );

      // Ensure the assets directory exists
      if (!(await exists(workspaceAssetsDir))) {
        await create(workspaceAssetsDir);
      }

      const newImageAbsoluteDiskPath = await join(workspaceAssetsDir, uniqueFileName);

      await copyFile(selectedPath, newImageAbsoluteDiskPath);

      const displaySrc = convertFileSrc(newImageAbsoluteDiskPath);
      const relativeStoragePath = `${ASSETS_SUBDIR}/${uniqueFileName}`.replace(/\\/g, '/');

      isProcessing.value = false;
      return {
        displaySrc,
        relativeStoragePath,
        absoluteDiskPath: newImageAbsoluteDiskPath,
        altText: sanitizedBaseName,
        originalFileName: originalFileNameWithExt,
      };
    // biome-ignore lint/suspicious/noExplicitAny: Generic error handling
    } catch (err: any) {
      await logError(`Error copying image: ${err}`);
      error.value = {
        type: "Unknown", // Or more specific if possible
        message: err.message || "An unknown error occurred while processing the image.",
        details: err,
      };
      if (err.message?.toLowerCase().includes('copy')) {
        error.value.type = "CopyFailed";
      }
      isProcessing.value = false;
      return null;
    }
  };

  /**
   * Converts a workspace-relative image path to an asset:// URL for display.
   * @param relativePath - Path relative to the workspace root (e.g., ".lore/assets/images/foo.png").
   * @returns The asset:// URL or null if workspace path is not set.
   */
  const getDisplaySrcFromRelativePath = async (relativePath: string): Promise<string | null> => {
    if (!editorStore.currentWorkspace?.path) {
      await logError("No active workspace path to resolve image.");
      return null;
    }
    try {
      const absolutePath = await join(editorStore.currentWorkspace.path, relativePath);
      return convertFileSrc(absolutePath);
    } catch (e) {
      await logError(`Error converting relative path (${relativePath}) to display src: ${e}`);
      return null;
    }
  };

  /**
   * Converts an asset:// display URL back to a workspace-relative path.
   * This is a bit more complex due to URL encoding and the asset protocol.
   * @param assetUrl - The asset:// URL (e.g., from an <img> src).
   * @returns The workspace-relative path or null if conversion fails.
   */
  const getRelativePathFromDisplaySrc = async (assetUrl: string): Promise<string | null> => {
    if (!editorStore.currentWorkspace?.path) {
      await logError("No active workspace path to resolve asset URL.");
      return null;
    }
    if (!assetUrl.startsWith("asset:")) {
      return null;
    }

    try {
      let decodedPath = decodeURIComponent(assetUrl);

      // Remove "asset://localhost/" or "asset:/" prefix
      if (decodedPath.startsWith("asset://localhost/")) {
        decodedPath = decodedPath.substring("asset://localhost/".length);
      } else if (decodedPath.startsWith("asset:/")) {
        decodedPath = decodedPath.substring("asset:/".length);
      }
      // On Windows, paths might start with an extra slash after decoding if they were C:/
      // e.g. /C:/Users/... -> C:/Users/...
      if (decodedPath.match(/^\/[A-Za-z]:/)) {
        decodedPath = decodedPath.substring(1);
      }


      const workspacePath = editorStore.currentWorkspace.path;
      // To correctly get a relative path, we need to ensure both are "resolved" or "normalized"
      // However, `resolve` might not work as expected if `decodedPath` isn't a "full" path in the OS sense.
      // A safer bet is string manipulation if we know `decodedPath` starts with `workspacePath`.

      // Normalize separators for comparison
      const normalizedDecodedPath = decodedPath.replace(/\\/g, '/');
      const normalizedWorkspacePath = workspacePath.replace(/\\/g, '/');

      if (normalizedDecodedPath.startsWith(normalizedWorkspacePath)) {
        let relativePath = normalizedDecodedPath.substring(normalizedWorkspacePath.length);
        if (relativePath.startsWith('/')) {
          relativePath = relativePath.substring(1);
        }
        return relativePath;
      }
      await warn(`Decoded path does not seem to be within the current workspace: ${decodedPath}`);
      return null;
    } catch (e) {
      await logError(`Error converting display src (${assetUrl}) to relative path: ${e}`);
      return null;
    }
  };


  return {
    isProcessing,
    error,
    selectAndCopyImageToWorkspace,
    getDisplaySrcFromRelativePath,
    getRelativePathFromDisplaySrc,
    ASSETS_SUBDIR,
  };
}
