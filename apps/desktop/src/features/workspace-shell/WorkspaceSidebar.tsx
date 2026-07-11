import { useMemo } from 'react';
import { ArrowLeftFromLine, Settings } from 'lucide-react';
import { Button, EntryTree, type EntryTreeGroup } from '@lore/ui';
import { getActiveDataset, useEditorShellStore } from '@/store/editor-shell';

interface WorkspaceSidebarProps {
  onOpenSettings: () => void;
  onSwitchProject: () => void;
}

export function WorkspaceSidebar({ onOpenSettings, onSwitchProject }: WorkspaceSidebarProps) {
  const state = useEditorShellStore();
  const dataset = getActiveDataset(state);

  const groups = useMemo<EntryTreeGroup[]>(
    () =>
      dataset.groupOrder.map((group) => ({
        entries: dataset.entries
          .filter((entry) => entry.group === group)
          .map((entry) => ({ id: entry.id, title: entry.title })),
        name: group,
      })),
    [dataset],
  );

  return (
    <aside className="flex h-full w-[250px] shrink-0 flex-col overflow-y-auto bg-sidebar transition-colors">
      <div className="flex items-center justify-between gap-1.5 border-b border-border px-4 pt-6 pb-[18px]">
        <div className="min-w-0">
          <span className="block text-[10px] font-semibold tracking-[0.14em] text-faint uppercase">
            Project
          </span>
          <span className="mt-1.5 block truncate text-[15px] font-semibold">
            {state.projectName}
          </span>
        </div>
        <div className="flex shrink-0 items-center gap-0.5">
          <Button
            aria-label="Switch project"
            className="size-6 rounded-[7px] text-faint hover:text-primary"
            onClick={onSwitchProject}
            size="icon"
            variant="ghost"
          >
            <ArrowLeftFromLine aria-hidden="true" size={13} strokeWidth={1.7} />
          </Button>
          <Button
            aria-label="Open settings"
            className="size-6 rounded-[7px] text-faint hover:text-primary"
            onClick={onOpenSettings}
            size="icon"
            variant="ghost"
          >
            <Settings aria-hidden="true" size={14} strokeWidth={1.6} />
          </Button>
        </div>
      </div>
      <EntryTree
        className="min-h-0 flex-1 py-1"
        groups={groups}
        onSelectEntry={state.selectEntry}
        selectedEntryId={state.selectedEntryId}
      />
    </aside>
  );
}
