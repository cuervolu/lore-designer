import { MenuItem, PredefinedMenuItem, Submenu } from '@tauri-apps/api/menu';
import type { MenuBuilderContext } from '../types';

async function createItem(
  text: string,
  id: string,
  accelerator?: string,
  action?: () => void,
) {
  return await MenuItem.new({
    id,
    text,
    accelerator,
    action,
  });
}

export async function createFileMenu(ctx: MenuBuilderContext) {
  const { platform, handlers } = ctx;
  const { cmdKey, shiftKey, plusSeparator } = platform;

  const items = [
    await createItem(
      'New File',
      'newFile',
      `${cmdKey}${plusSeparator}N`,
      handlers.onNewFile,
    ),
    await createItem(
      'Open File',
      'openFile',
      `${cmdKey}${plusSeparator}O`,
      handlers.onOpenFile,
    ),
    await PredefinedMenuItem.new({ item: 'Separator' }),
    await createItem(
      'Save',
      'save',
      `${cmdKey}${plusSeparator}S`,
      handlers.onSave,
    ),
    await createItem(
      'Save As...',
      'saveAs',
      `${cmdKey}${shiftKey}${plusSeparator}S`,
      handlers.onSaveAs,
    ),
    await createItem(
      'Close File',
      'closeFile',
      `${cmdKey}${plusSeparator}W`,
      handlers.onCloseFile,
    ),
    await PredefinedMenuItem.new({ item: 'Separator' }),
    await createItem(
      'Exit to Workspaces',
      'exitToWorkspaces',
      undefined,
      handlers.onExitToWorkspaces,
    ),
  ];

  if (platform.platform !== 'macos') {
    items.push(
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await PredefinedMenuItem.new({ item: 'Quit', text: 'Exit' }),
    );
  }

  return await Submenu.new({ text: 'File', items });
}
