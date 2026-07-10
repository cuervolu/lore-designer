import type { ReactNode } from 'react';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';

export function PropertiesInspector() {
  const state = useEditorShellStore();
  const entry = getActiveEntry(state);
  if (!entry) return null;

  return (
    <aside className="metadata-panel" aria-label="Entry metadata">
      <MetadataLabel>Type</MetadataLabel>
      <div className="metadata-value">{entry.type}</div>
      <MetadataLabel>Status</MetadataLabel>
      <div className="metadata-value">{entry.status}</div>
      <MetadataLabel>Tags</MetadataLabel>
      <div className="tag-list">
        {entry.tags.map((tag) => (
          <span key={tag}>{tag}</span>
        ))}
      </div>
      <MetadataLabel>Relationships</MetadataLabel>
      <div className="relationship-list">
        {entry.relationships.map((relationship) => (
          <button
            key={`${relationship.name}-${relationship.note}`}
            onClick={() => state.selectEntryByName(relationship.name)}
            type="button"
          >
            <strong>{relationship.name}</strong>
            <span>{relationship.note}</span>
          </button>
        ))}
      </div>
    </aside>
  );
}

function MetadataLabel({ children }: { children: ReactNode }) {
  return <h2 className="metadata-label">{children}</h2>;
}
