import { invoke } from '@tauri-apps/api/core';
import type {
  CreateWorkspaceRequest,
  CreateWorkspaceResult,
  WorkspaceTemplateSummary,
} from '@features/workspace-wizard/types';

// Workspace Tauri command stubs — real implementations land with lore-workspaces crate

export async function openWorkspace(path: string): Promise<void> {
  return invoke('open_workspace', { path });
}

export async function listWorkspaceTemplates(): Promise<WorkspaceTemplateSummary[]> {
  return invoke('list_workspace_templates');
}

export async function createWorkspace(
  request: CreateWorkspaceRequest,
): Promise<CreateWorkspaceResult> {
  return invoke('create_workspace', { request });
}

export async function listRecentWorkspaces(): Promise<unknown[]> {
  return invoke('list_recent_workspaces');
}
