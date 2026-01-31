import { PredefinedMenuItem, Submenu, MenuItem } from '@tauri-apps/api/menu';
import type { MenuBuilderContext } from '../types';

export async function createEditMenu(ctx: MenuBuilderContext) {
  const { platform } = ctx;
  const { cmdKey, shiftKey, plusSeparator } = platform;

  const findSubMenu = await Submenu.new({
    text: 'Find',
    items: [
      await MenuItem.new({
        id: 'find',
        text: 'Find',
        accelerator: `${cmdKey}${plusSeparator}F`,
      }),
      await MenuItem.new({
        id: 'replace',
        text: 'Replace',
        accelerator: `${cmdKey}${plusSeparator}H`,
      }),
      await MenuItem.new({
        id: 'findInFiles',
        text: 'Find in Files',
        accelerator: `${cmdKey}${shiftKey}${plusSeparator}F`,
      }),
    ],
  });

  return await Submenu.new({
    text: 'Edit',
    items: [
      await PredefinedMenuItem.new({ item: 'Undo', text: 'Undo' }),
      await PredefinedMenuItem.new({ item: 'Redo', text: 'Redo' }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await PredefinedMenuItem.new({ item: 'Cut', text: 'Cut' }),
      await PredefinedMenuItem.new({ item: 'Copy', text: 'Copy' }),
      await PredefinedMenuItem.new({ item: 'Paste', text: 'Paste' }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      findSubMenu,
    ],
  });
}
