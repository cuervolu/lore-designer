import { MenuItem, Submenu, PredefinedMenuItem } from '@tauri-apps/api/menu';
import type { MenuBuilderContext } from '../types';

export async function createHelpMenu(ctx: MenuBuilderContext) {
  const { platform, handlers } = ctx;
  const { cmdKey, plusSeparator } = platform;

  return await Submenu.new({
    text: 'Help',
    items: [
      await MenuItem.new({
        id: 'documentation',
        text: 'Documentation',
        action: handlers.onDocumentation,
      }),
      await MenuItem.new({
        id: 'keyboardShortcuts',
        text: 'Keyboard Shortcuts',
        accelerator: `${cmdKey}${plusSeparator}K${plusSeparator}S`,
        action: handlers.onKeyboardShortcuts,
      }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await MenuItem.new({
        id: 'checkUpdates',
        text: 'Check for Updates',
        action: handlers.onCheckUpdates,
      }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await MenuItem.new({
        id: 'about',
        text: 'About',
        action: handlers.onAbout,
      }),
    ],
  });
}
