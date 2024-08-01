import { check } from '@tauri-apps/plugin-updater'
import { relaunch } from '@tauri-apps/plugin-process'
import { error, info } from "@tauri-apps/plugin-log";
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUpdaterStore = defineStore('updater', () => {
  const isUpdateAvailable = ref(false)
  const updateVersion = ref('')
  const updateNotes = ref('')
  const isUpdating = ref(false)
  const updateError = ref<string | null>(null)

  const checkForUpdates = async () => {
    try {
      await info("Checking for updates");
      const update = await check()
      if (update?.available) {
        await info(`Update available: version ${update.version}`);
        isUpdateAvailable.value = true
        updateVersion.value = update.version
        updateNotes.value = update.body || ''
      } else {
        await info("No updates available");
      }
    } catch (e) {
      await error(`Error checking for updates: ${e}`);
      updateError.value = 'Failed to check for updates'
    }
  }

  const installUpdate = async () => {
    if (!isUpdateAvailable.value) {
      await info("No update available to install");
      return;
    }

    try {
      isUpdating.value = true
      await info(`Starting update installation to version ${updateVersion.value}`);
      const update = await check()
      if (update?.available) {
        await info("Downloading and installing update");
        await update.downloadAndInstall()
        await info("Update installed, relaunching application");
        await relaunch()
      } else {
        await info("Update no longer available");
      }
    } catch (e) {
      await error(`Error installing update: ${e}`);
      updateError.value = 'Failed to install update'
    } finally {
      isUpdating.value = false
    }
  }

  return {
    isUpdateAvailable,
    updateVersion,
    updateNotes,
    isUpdating,
    updateError,
    checkForUpdates,
    installUpdate
  }
})