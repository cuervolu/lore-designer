import {
  Menu,
  MenuItem,
  Submenu,
  PredefinedMenuItem,
  CheckMenuItem,
} from '@tauri-apps/api/menu'
import type { Composer } from 'vue-i18n'
import type { Ref } from 'vue'
import { error as logError } from '@tauri-apps/plugin-log'

interface UsePlatformReturn {
  platform: Readonly<Ref<string>>
  cmdKey: Readonly<Ref<string>>
  shiftKey: Readonly<Ref<string>>
  plusSeparator: Readonly<Ref<string>>
  symbols: {
    cmd: Readonly<Ref<string>>
    shift: Readonly<Ref<string>>
    alt: Ref<string>
    ctrl: Ref<string>
  }
}

interface MenuActions {
  onNewFile: () => void
  onOpenFile: () => void
  onSave: () => void
  onSaveAs: () => void
  onCloseFile: () => void
  onExitToWorkspaces: () => void
  onWorkspacesHome: () => void
  onNewWorkspace: () => void
  onRefreshFileTree: () => void
  onWorkspaceSettings: () => void
  onToggleConsole: () => void
  onToggleInspector: () => void
  onZoomIn: () => void
  onZoomOut: () => void
  onResetZoom: () => void
  onDocumentation: () => void
  onKeyboardShortcuts: () => void
  onCheckUpdates: () => void
  onAbout: () => void
  getConsoleOpen: () => boolean
  getInspectorOpen: () => boolean
}

type MenuBuilder = (
  i18n: Composer,
  platform: UsePlatformReturn,
  actions: MenuActions,
) => Promise<void>

async function createItem(
  t: (key: string) => string,
  platform: UsePlatformReturn,
  key: string,
  shortcut?: string,
  action?: () => void,
) {
  const item: any = {
    id: key,
    text: t(`menu.${key}`),
    action: action,
  }
  if (shortcut) {
    item.accelerator = shortcut
      .replace('{{cmdKey}}', platform.cmdKey.value)
      .replace('{{shiftKey}}', platform.shiftKey.value)
      .replace('{{plusSeparator}}', platform.plusSeparator.value)
  }
  return await MenuItem.new(item)
}

async function createPredefined(
  t: (key: string) => string,
  item: 'Undo' | 'Redo' | 'Cut' | 'Copy' | 'Paste' | 'SelectAll' | 'Quit',
  key: string,
) {
  return await PredefinedMenuItem.new({
    item: item,
    text: t(`menu.${key}`),
  })
}

export const buildEditorMenu: MenuBuilder = async (
  i18n,
  platform,
  actions,
) => {
  const { t } = i18n
  const { cmdKey, shiftKey, plusSeparator } = platform

  const fileSubmenu = await Submenu.new({
    text: t('menu.file'),
    items: [
      await createItem(
        t,
        platform,
        'newFile',
        `${cmdKey.value}${plusSeparator.value}N`,
        actions.onNewFile,
      ),
      await createItem(
        t,
        platform,
        'openFile',
        `${cmdKey.value}${plusSeparator.value}O`,
        actions.onOpenFile,
      ),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await createItem(
        t,
        platform,
        'save',
        `${cmdKey.value}${plusSeparator.value}S`,
        actions.onSave,
      ),
      await createItem(
        t,
        platform,
        'saveAs',
        `${cmdKey.value}${shiftKey.value}${plusSeparator.value}S`,
        actions.onSaveAs, // TODO: Implementar saveActiveFileAs en el store
      ),
      await createItem(
        t,
        platform,
        'closeFile',
        `${cmdKey.value}${plusSeparator.value}W`,
        actions.onCloseFile,
      ),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await createItem(
        t,
        platform,
        'exitToWorkspaces',
        undefined,
        actions.onExitToWorkspaces,
      ),
      // Corregido: Chequear platform.platform.value
      ...(platform.platform.value === 'macos'
        ? []
        : [
          await PredefinedMenuItem.new({ item: 'Separator' }),
          await createPredefined(t, 'Quit', 'exit'),
        ]),
    ],
  })

  const editSubmenu = await Submenu.new({
    text: t('menu.edit'),
    items: [
      await createPredefined(t, 'Undo', 'undo'),
      await createPredefined(t, 'Redo', 'redo'),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await createPredefined(t, 'Cut', 'cut'),
      await createPredefined(t, 'Copy', 'copy'),
      await createPredefined(t, 'Paste', 'paste'),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await Submenu.new({
        text: t('menu.findSubMenu'),
        items: [
          await createItem(
            t,
            platform,
            'find',
            `${cmdKey.value}${plusSeparator.value}F`,
          ), // TODO: Acción
          await createItem(
            t,
            platform,
            'replace',
            `${cmdKey.value}${plusSeparator.value}H`,
          ), // TODO: Acción
          await createItem(
            t,
            platform,
            'findInFiles',
            `${cmdKey.value}${shiftKey.value}${plusSeparator.value}F`,
          ), // TODO: Acción
        ],
      }),
    ],
  })

  const workspaceSubmenu = await Submenu.new({
    text: t('menu.workspace'),
    items: [
      await createItem(
        t,
        platform,
        'workspacesHome',
        undefined,
        actions.onWorkspacesHome,
      ),
      await createItem(
        t,
        platform,
        'newWorkspace',
        undefined,
        actions.onNewWorkspace,
      ),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await createItem(
        t,
        platform,
        'refreshFileTree',
        `${cmdKey.value}${plusSeparator.value}R`,
        actions.onRefreshFileTree,
      ),
      await createItem(
        t,
        platform,
        'workspaceSettings',
        undefined,
        actions.onWorkspaceSettings,
      ),
    ],
  })

  const viewSubmenu = await Submenu.new({
    text: t('menu.view'),
    items: [
      await CheckMenuItem.new({
        id: 'showConsole',
        text: t('menu.showConsole'),
        checked: actions.getConsoleOpen(),
        accelerator: `${cmdKey.value}${shiftKey.value}${plusSeparator.value}C`,
        action: actions.onToggleConsole,
      }),
      await CheckMenuItem.new({
        id: 'showInspector',
        text: t('menu.showInspector'),
        checked: actions.getInspectorOpen(),
        accelerator: `${cmdKey.value}${plusSeparator.value}I`,
        action: actions.onToggleInspector,
      }),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await createItem(
        t,
        platform,
        'zoomIn',
        `${cmdKey.value}${plusSeparator.value}+`,
        actions.onZoomIn,
      ),
      await createItem(
        t,
        platform,
        'zoomOut',
        `${cmdKey.value}${plusSeparator.value}-`,
        actions.onZoomOut,
      ),
      await createItem(
        t,
        platform,
        'resetZoom',
        `${cmdKey.value}${plusSeparator.value}0`,
        actions.onResetZoom,
      ),
    ],
  })

  const helpSubmenu = await Submenu.new({
    text: t('menu.help'),
    items: [
      await createItem(
        t,
        platform,
        'documentation',
        undefined,
        actions.onDocumentation,
      ),
      await createItem(
        t,
        platform,
        'keyboardShortcuts',
        `${cmdKey.value}${plusSeparator.value}K${plusSeparator.value}S`,
        actions.onKeyboardShortcuts,
      ),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await createItem(
        t,
        platform,
        'checkUpdates',
        undefined,
        actions.onCheckUpdates,
      ),
      await PredefinedMenuItem.new({ item: 'Separator' }),
      await createItem(t, platform, 'about', undefined, actions.onAbout),
    ],
  })

  let menuItems: (Submenu | PredefinedMenuItem)[]
  if (platform.platform.value === 'macos') {
    const appSubmenu = await Submenu.new({
      text: t('app.title'),
      items: [
        await createItem(t, platform, 'about', undefined, actions.onAbout),
        await PredefinedMenuItem.new({ item: 'Separator' }),
        await PredefinedMenuItem.new({ item: 'Services' }),
        await PredefinedMenuItem.new({ item: 'Separator' }),
        await PredefinedMenuItem.new({ item: 'Hide' }),
        await PredefinedMenuItem.new({ item: 'HideOthers' }),
        await PredefinedMenuItem.new({ item: 'ShowAll' }),
        await PredefinedMenuItem.new({ item: 'Separator' }),
        await createPredefined(t, 'Quit', 'exit'),
      ],
    })
    menuItems = [
      appSubmenu,
      fileSubmenu,
      editSubmenu,
      workspaceSubmenu,
      viewSubmenu,
      helpSubmenu,
    ]
  } else {
    menuItems = [fileSubmenu, editSubmenu, workspaceSubmenu, viewSubmenu, helpSubmenu]
  }

  try {
    const menu = await Menu.new({ items: menuItems })
    await menu.setAsAppMenu()
  } catch(e) {
    await logError(`Failed to set editor app menu: ${e}`)
  }
}

export const buildWizardMenu: MenuBuilder = async (i18n, platform, actions) => {
  const { t } = i18n

  const fileSubmenu = await Submenu.new({
    text: t('menu.file'),
    items: [
      await createItem(
        t,
        platform,
        'newWorkspace',
        `${platform.cmdKey.value}${platform.plusSeparator.value}N`,
        actions.onNewWorkspace,
      ),
      ...(platform.platform.value === 'macos'
        ? []
        : [
          await PredefinedMenuItem.new({ item: 'Separator' }),
          await createPredefined(t, 'Quit', 'exit'),
        ]),
    ],
  })

  const helpSubmenu = await Submenu.new({
    text: t('menu.help'),
    items: [await createItem(t, platform, 'about', undefined, actions.onAbout)],
  })

  let menuItems: (Submenu | PredefinedMenuItem)[] = []
  if (platform.platform.value === 'macos') {
    const appSubmenu = await Submenu.new({
      text: t('app.title'),
      items: [
        await createItem(t, platform, 'about', undefined, actions.onAbout),
        await PredefinedMenuItem.new({ item: 'Separator' }),
        await PredefinedMenuItem.new({ item: 'Hide' }),
        await PredefinedMenuItem.new({ item: 'HideOthers' }),
        await PredefinedMenuItem.new({ item: 'ShowAll' }),
        await PredefinedMenuItem.new({ item: 'Separator' }),
        await createPredefined(t, 'Quit', 'exit'),
      ],
    })
    menuItems = [appSubmenu, fileSubmenu, helpSubmenu]
  } else {
    menuItems = [fileSubmenu, helpSubmenu]
  }

  try {
    const menu = await Menu.new({ items: menuItems })
    await menu.setAsAppMenu()
  } catch(e) {
    await logError(`Failed to set wizard app menu: ${e}`)
  }
}
