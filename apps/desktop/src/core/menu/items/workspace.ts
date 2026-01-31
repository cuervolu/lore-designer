import { MenuItem, Submenu, PredefinedMenuItem } from '@tauri-apps/api/menu';
import type { MenuBuilderContext } from '../types';

export async function createWorkspaceMenu(ctx: MenuBuilderContext) {
  const { platform, handlers } = ctx;
  const { cmdKey, plusSeparator } = platform;

  return await Submenu.new({
    text: 'Workspace',
    items: [
      await MenuItem.new({
        id: 'workspacesHome',
        text: 'Workspaces Home',
        action: handlers.onWorkspacesHome,
      }),
      await MenuItem.new({
        id: 'newWorkspace',
        text: 'New Workspace',
        action: handlers.onNewWorkspace,
      }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await MenuItem.new({
        id: 'refreshFileTree',
        text: 'Refresh File Tree',
        accelerator: `${cmdKey}${plusSeparator}R`,
        action: handlers.onRefreshFileTree,
      }),
      await MenuItem.new({
        id: 'workspaceSettings',
        text: 'Workspace Settings',
        action: handlers.onWorkspaceSettings,
      }),
    ],
  });
}
