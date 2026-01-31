import { platform } from '@tauri-apps/plugin-os';
import type { OSPlatform, PlatformInfo } from '@lore/shared';

export async function detectPlatform(): Promise<PlatformInfo> {
  const osType = platform() as OSPlatform;
  const isMac = osType === 'macos';

  return {
    platform: osType,
    cmdKey: isMac ? '⌘' : 'Ctrl',
    shiftKey: isMac ? '⇧' : 'Shift',
    plusSeparator: isMac ? '' : '+',
    symbols: {
      cmd: isMac ? '⌘' : 'Ctrl',
      shift: isMac ? '⇧' : 'Shift',
      alt: isMac ? '⌥' : 'Alt',
      ctrl: isMac ? '⌃' : 'Ctrl',
    },
  };
}
