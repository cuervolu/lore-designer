import { ref, readonly, onMounted } from 'vue';
import { error as logError } from '@fltsci/tauri-plugin-tracing';
import type { PlatformInfo } from '@lore/shared';
import { detectPlatform } from './detector';

export function usePlatform() {
  const platformInfo = ref<PlatformInfo>({
    platform: 'unknown',
    cmdKey: 'Ctrl',
    shiftKey: 'Shift',
    plusSeparator: '+',
    symbols: {
      cmd: 'Ctrl',
      shift: 'Shift',
      alt: 'Alt',
      ctrl: 'Ctrl',
    },
  });

  async function initialize() {
    try {
      platformInfo.value = await detectPlatform();
    } catch (error) {
      await logError(`Failed to detect platform: ${error}`);
    }
  }

  onMounted(initialize);

  return {
    platform: readonly(platformInfo),
  };
}
