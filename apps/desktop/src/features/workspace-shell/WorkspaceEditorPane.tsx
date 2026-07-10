import { Image, Link, Minus, PanelRight } from 'lucide-react';
import { DocumentEditor } from '@features/document-editor/DocumentEditor';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';

export function WorkspaceEditorPane() {
  const state = useEditorShellStore();
  const entry = getActiveEntry(state);

  return (
    <section className="workspace-editor">
      {entry ? (
        <div className="editor-mode-row">
          <span>{state.focusModeEnabled ? 'click a paragraph to focus' : 'focus mode is off'}</span>
          <div className="segmented-control segmented-control--compact" aria-label="Editor view">
            <button
              className={state.viewMode === 'edit' ? 'is-active' : ''}
              onClick={() => state.setViewMode('edit')}
              type="button"
            >
              Edit
            </button>
            <button
              className={state.viewMode === 'codex' ? 'is-active' : ''}
              onClick={() => state.setViewMode('codex')}
              type="button"
            >
              Codex
            </button>
          </div>
        </div>
      ) : null}
      <DocumentEditor />
      {entry && state.viewMode === 'edit' ? (
        <div className="editor-toolbar" aria-label="Formatting toolbar">
          <button aria-label="Bold" className="format-button is-bold" type="button">
            B
          </button>
          <button aria-label="Italic" className="format-button is-italic" type="button">
            I
          </button>
          <button aria-label="Underline" className="format-button is-underline" type="button">
            U
          </button>
          <span className="toolbar-divider" />
          <button aria-label="Insert link" className="format-button" type="button">
            <Link size={15} strokeWidth={1.6} />
          </button>
          <button aria-label="Insert image" className="format-button" type="button">
            <Image size={15} strokeWidth={1.6} />
          </button>
          <button aria-label="Insert divider" className="format-button" type="button">
            <Minus size={15} strokeWidth={1.6} />
          </button>
          <span className="toolbar-spacer" />
          <button
            aria-label={state.metadataOpen ? 'Hide metadata' : 'Show metadata'}
            className="format-button"
            onClick={() => state.setMetadataOpen(!state.metadataOpen)}
            type="button"
          >
            <PanelRight size={15} strokeWidth={1.6} />
          </button>
        </div>
      ) : null}
    </section>
  );
}
