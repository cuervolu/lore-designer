export interface MenuAction {
  id: string;
  handler: () => void | Promise<void>;
}

export interface MenuState {
  consoleOpen: boolean;
  inspectorOpen: boolean;
}

export interface MenuActionHandlers {
  // File actions
  onNewFile?: () => void;
  onOpenFile?: () => void;
  onSave?: () => void;
  onSaveAs?: () => void;
  onCloseFile?: () => void;
  onExitToWorkspaces?: () => void;

  // Workspace actions
  onWorkspacesHome?: () => void;
  onNewWorkspace?: () => void;
  onRefreshFileTree?: () => void;
  onWorkspaceSettings?: () => void;

  // View actions
  onToggleConsole?: () => void;
  onToggleInspector?: () => void;
  onZoomIn?: () => void;
  onZoomOut?: () => void;
  onResetZoom?: () => void;

  // Help actions
  onDocumentation?: () => void;
  onKeyboardShortcuts?: () => void;
  onCheckUpdates?: () => void;
  onAbout?: () => void;

  // State getters
  getConsoleOpen?: () => boolean;
  getInspectorOpen?: () => boolean;
}

export type MenuContext = 'wizard' | 'editor';