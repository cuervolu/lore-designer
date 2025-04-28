import { platform } from '@tauri-apps/plugin-os';
import { error as logError } from '@tauri-apps/plugin-log'
import { ref, onMounted, readonly } from 'vue';


export function usePlatform() {
  const platformValue = ref('unknown');
  const cmdKey = ref('⌘'); // Default to Mac
  const shiftKey = ref('⇧'); // Default to Mac
  const plusSeparator = ref('');  // No separator for Mac symbols

  async function detectPlatform() {
    try {
      const osType =  platform();
      platformValue.value = osType;

      // Update command key based on platform
      if (osType === 'windows' || osType === 'linux') {
        cmdKey.value = 'Ctrl';
        shiftKey.value = 'Shift';
        plusSeparator.value = '+';
      }
    } catch (error) {
      await logError(`Failed to detect platform: ${error}`);
      cmdKey.value = 'Ctrl';
      shiftKey.value = 'Shift';
      plusSeparator.value = '+';
    }
  }

  onMounted(async() => {
    await detectPlatform();
  });

  return {
    platform: readonly(platformValue),
    cmdKey: readonly(cmdKey),
    shiftKey: readonly(shiftKey),
    plusSeparator: readonly(plusSeparator),

    symbols: {
      cmd: readonly(cmdKey),
      shift: readonly(shiftKey),
      alt: ref(platformValue.value === 'macos' ? '⌥' : 'Alt'),
      ctrl: ref(platformValue.value === 'macos' ? '⌃' : 'Ctrl')
    }
  };
}
