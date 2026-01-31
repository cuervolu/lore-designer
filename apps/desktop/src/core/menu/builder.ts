import {
  Menu,
  MenuItem,
  PredefinedMenuItem,
  Submenu,
} from '@tauri-apps/api/menu';
import { error as logError } from '@fltsci/tauri-plugin-tracing';
import type { MenuBuilderContext } from './types';
import { createAppMenu } from './items/app';
import { createFileMenu } from './items/file';
import { createEditMenu } from './items/edit';
import { createWorkspaceMenu } from './items/workspace';
import { createViewMenu } from './items/view';
import { createHelpMenu } from './items/help';

export async function buildEditorMenu(ctx: MenuBuilderContext): Promise<void> {
  try {
    const menuItems = [];

    // macOS gets app menu first
    if (ctx.platform.platform === 'macos') {
      menuItems.push(await createAppMenu(ctx));
    }

    menuItems.push(
      await createFileMenu(ctx),
      await createEditMenu(ctx),
      await createWorkspaceMenu(ctx),
      await createViewMenu(ctx),
      await createHelpMenu(ctx),
    );

    const menu = await Menu.new({ items: menuItems });
    await menu.setAsAppMenu();
  } catch (e) {
    await logError(`Failed to set editor app menu: ${e}`);
  }
}

export async function buildWizardMenu(ctx: MenuBuilderContext): Promise<void> {
  try {
    const { platform, handlers } = ctx;
    const { cmdKey, plusSeparator } = platform;

    const fileMenu = await Submenu.new({
      text: 'File',
      items: [
        await MenuItem.new({
          id: 'newWorkspace',
          text: 'New Workspace',
          accelerator: `${cmdKey}${plusSeparator}N`,
          action: handlers.onNewWorkspace,
        }),
        ...(platform.platform === 'macos'
          ? []
          : [
              await PredefinedMenuItem.new({ item: 'Separator' }),
              await PredefinedMenuItem.new({ item: 'Quit', text: 'Exit' }),
            ]),
      ],
    });

    const helpMenu = await MenuItem.new({
      id: 'about',
      text: 'About',
      action: handlers.onAbout,
    });

    const menuItems = [];

    if (platform.platform === 'macos') {
      menuItems.push(await createAppMenu(ctx));
    }

    menuItems.push(fileMenu, helpMenu);

    const menu = await Menu.new({ items: menuItems });
    await menu.setAsAppMenu();
  } catch (e) {
    await logError(`Failed to set wizard app menu: ${e}`);
  }
}
