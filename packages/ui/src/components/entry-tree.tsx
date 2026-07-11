import { useEffect, useMemo, type CSSProperties } from "react";
import { FileTree as PierreFileTree, useFileTree } from "@pierre/trees/react";

import { cn } from "@lore/ui/lib/utils";

interface EntryTreeGroup {
  name: string;
  entries: readonly { id: string; title: string }[];
}

interface EntryTreeProps {
  className?: string;
  groups: readonly EntryTreeGroup[];
  onSelectEntry: (id: string) => void;
  selectedEntryId: string | null;
}

const ENTRY_TREE_UNSAFE_CSS = `
  [data-item-type='folder'] {
    pointer-events: none;
    cursor: default;
    font-weight: var(--trees-font-weight-semibold);
    text-transform: uppercase;
    letter-spacing: 0.12em;
    font-size: 10px;
  }
  [data-item-section='icon'] {
    display: flex;
    align-items: center;
    justify-content: center;
  }
  [data-item-section='icon'] svg {
    display: none;
  }
  [data-item-type='folder'] [data-item-section='icon'] {
    display: none;
  }
  [data-item-type='file'] [data-item-section='icon']::before {
    content: '';
    width: 5px;
    height: 5px;
    border-radius: 9999px;
    background: transparent;
  }
  [data-item-type='file'][data-item-selected] [data-item-section='icon']::before {
    background: var(--trees-accent);
  }
`;

function pathForEntry(groupName: string, entryTitle: string) {
  return `${groupName}/${entryTitle}`;
}

function EntryTree({ className, groups, onSelectEntry, selectedEntryId }: EntryTreeProps) {
  const { paths, pathToId } = useMemo(() => {
    const paths: string[] = [];
    const pathToId = new Map<string, string>();
    for (const group of groups) {
      for (const entry of group.entries) {
        const path = pathForEntry(group.name, entry.title);
        paths.push(path);
        pathToId.set(path, entry.id);
      }
    }
    return { paths, pathToId };
  }, [groups]);

  const idToPath = useMemo(() => {
    const idToPath = new Map<string, string>();
    for (const [path, id] of pathToId) idToPath.set(id, path);
    return idToPath;
  }, [pathToId]);

  const { model } = useFileTree({
    dragAndDrop: false,
    icons: { set: "none" },
    initialExpansion: "open",
    onSelectionChange: (selectedPaths) => {
      const path = selectedPaths[0];
      const id = path ? pathToId.get(path) : undefined;
      if (id) onSelectEntry(id);
    },
    paths,
    search: false,
    unsafeCSS: ENTRY_TREE_UNSAFE_CSS,
  });

  useEffect(() => {
    model.resetPaths(paths);
  }, [model, paths]);

  useEffect(() => {
    const selectedPath = selectedEntryId ? idToPath.get(selectedEntryId) : undefined;
    const currentlySelected = model.getSelectedPaths();
    if (selectedPath && !currentlySelected.includes(selectedPath)) {
      model.getItem(selectedPath)?.select();
    }
  }, [model, selectedEntryId, idToPath]);

  return (
    <PierreFileTree
      className={cn("flex-1", className)}
      model={model}
      style={
        {
          "--trees-accent-override": "var(--accent)",
          "--trees-bg-muted-override": "transparent",
          "--trees-bg-override": "transparent",
          "--trees-border-color-override": "var(--border)",
          "--trees-fg-muted-override": "var(--faint)",
          "--trees-fg-override": "var(--muted)",
          "--trees-font-family-override": "'Lora Variable', Lora, Georgia, serif",
          "--trees-font-size-override": "14.5px",
          "--trees-selected-bg-override": "transparent",
          "--trees-selected-fg-override": "var(--accent)",
        } as CSSProperties
      }
    />
  );
}

export { EntryTree };
export type { EntryTreeGroup };
