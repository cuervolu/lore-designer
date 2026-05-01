import { invoke } from '@tauri-apps/api/core';

// Workspace Tauri command stubs — real implementations land with lore-workspaces crate

export async function openWorkspace(path: string): Promise<void> {
  return invoke('open_workspace', { path });
}

export async function createWorkspace(name: string, path: string): Promise<void> {
  return invoke('create_workspace', { name, path });
}

export async function listRecentWorkspaces(): Promise<unknown[]> {
  return invoke('list_recent_workspaces');
}
