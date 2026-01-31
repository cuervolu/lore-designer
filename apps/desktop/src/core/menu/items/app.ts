import { PredefinedMenuItem, Submenu, MenuItem } from '@tauri-apps/api/menu';
import type { MenuBuilderContext } from '../types';

// macOS-specific app menu
export async function createAppMenu(ctx: MenuBuilderContext) {
  const { handlers } = ctx;

  return await Submenu.new({
    text: 'Lore Designer',
    items: [
      await MenuItem.new({
        id: 'about',
        text: 'About Lore Designer',
        action: handlers.onAbout,
      }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await PredefinedMenuItem.new({ item: 'Services' }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await PredefinedMenuItem.new({ item: 'Hide' }),
      await PredefinedMenuItem.new({ item: 'HideOthers' }),
      await PredefinedMenuItem.new({ item: 'ShowAll' }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await PredefinedMenuItem.new({
        item: 'Quit',
        text: 'Quit Lore Designer',
      }),
    ],
  });
}
