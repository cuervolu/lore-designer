import type { ReactNode } from 'react';
import { Badge } from '@lore/ui';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';

export function PropertiesInspector() {
  const state = useEditorShellStore();
  const entry = getActiveEntry(state);
  if (!entry) return null;

  return (
    <aside
      className="h-full w-[236px] shrink-0 overflow-y-auto border-l border-border bg-background px-6 pt-[88px] pb-10 transition-colors"
      aria-label="Entry metadata"
    >
      <MetadataLabel>Type</MetadataLabel>
      <div className="mb-7 text-[15px] text-body italic">{entry.type}</div>
      <MetadataLabel>Status</MetadataLabel>
      <div className="mb-7 text-[15px] text-body italic">{entry.status}</div>
      <MetadataLabel>Tags</MetadataLabel>
      <div className="mb-7 flex flex-wrap gap-1.5">
        {entry.tags.map((tag) => (
          <Badge
            className="rounded-[20px] border-border bg-chip px-2.5 py-1 text-xs font-normal text-muted-foreground"
            key={tag}
            variant="outline"
          >
            {tag}
          </Badge>
        ))}
      </div>
      <MetadataLabel>Relationships</MetadataLabel>
      <div>
        {entry.relationships.map((relationship) => (
          <button
            className="group block w-full cursor-pointer border-b border-border py-2.5 text-left"
            key={`${relationship.name}-${relationship.note}`}
            onClick={() => state.selectEntryByName(relationship.name)}
            type="button"
          >
            <strong className="block text-sm font-medium group-hover:text-primary">
              {relationship.name}
            </strong>
            <span className="mt-0.5 block text-xs text-faint italic">{relationship.note}</span>
          </button>
        ))}
      </div>
    </aside>
  );
}

function MetadataLabel({ children }: { children: ReactNode }) {
  return (
    <h2 className="mb-2 text-[10px] font-semibold tracking-[0.12em] text-faint uppercase">
      {children}
    </h2>
  );
}
