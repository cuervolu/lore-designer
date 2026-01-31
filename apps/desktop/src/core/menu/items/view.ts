import {
  CheckMenuItem,
  MenuItem,
  PredefinedMenuItem,
  Submenu,
} from '@tauri-apps/api/menu';
import type { MenuBuilderContext } from '../types';

export async function createViewMenu(ctx: MenuBuilderContext) {
  const { platform, handlers } = ctx;
  const { cmdKey, shiftKey, plusSeparator } = platform;

  return await Submenu.new({
    text: 'View',
    items: [
      await CheckMenuItem.new({
        id: 'showConsole',
        text: 'Show Console',
        checked: handlers.getConsoleOpen?.() ?? false,
        accelerator: `${cmdKey}${shiftKey}${plusSeparator}C`,
        action: handlers.onToggleConsole,
      }),
      await CheckMenuItem.new({
        id: 'showInspector',
        text: 'Show Inspector',
        checked: handlers.getInspectorOpen?.() ?? false,
        accelerator: `${cmdKey}${plusSeparator}I`,
        action: handlers.onToggleInspector,
      }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await MenuItem.new({
        id: 'zoomIn',
        text: 'Zoom In',
        accelerator: `${cmdKey}${plusSeparator}+`,
        action: handlers.onZoomIn,
      }),
      await MenuItem.new({
        id: 'zoomOut',
        text: 'Zoom Out',
        accelerator: `${cmdKey}${plusSeparator}-`,
        action: handlers.onZoomOut,
      }),
      await MenuItem.new({
        id: 'resetZoom',
        text: 'Reset Zoom',
        accelerator: `${cmdKey}${plusSeparator}0`,
        action: handlers.onResetZoom,
      }),
    ],
  });
}
