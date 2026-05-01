import { invoke } from '@tauri-apps/api/core';

// Document Tauri command stubs — real implementations land with lore-editor crate

export async function loadDocument(path: string): Promise<unknown> {
  return invoke('load_document', { path });
}

export async function saveDocument(path: string, content: string): Promise<void> {
  return invoke('save_document', { path, content });
}
