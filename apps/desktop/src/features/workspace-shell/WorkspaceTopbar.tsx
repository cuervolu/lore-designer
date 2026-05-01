import { Kbd } from '@lore/ui/components/kbd';
import { ChevronLeft, ChevronRight, PanelLeft, PanelRight, Plus, Search } from 'lucide-react';

interface WorkspaceTopbarProps {
  onExit?: () => void;
  onOpenCommandPalette: () => void;
  onToggleSidebar: () => void;
  onToggleInspector: () => void;
}

export function WorkspaceTopbar({
  onExit,
  onOpenCommandPalette,
  onToggleSidebar,
  onToggleInspector,
}: WorkspaceTopbarProps) {
  return (
    <header className="workspace-topbar">
      <div className="workspace-topbar__nav">
        <button
          className="ld-icon-btn workspace-topbar__sidebar-trigger"
          onClick={onToggleSidebar}
          title="Toggle sidebar"
          type="button"
        >
          <PanelLeft size={12} color="var(--ink-3)" strokeWidth={1.35} />
        </button>
        <button className="ld-icon-btn" onClick={onExit} title="Back to projects" type="button">
          <ChevronLeft size={12} color="var(--ink-3)" strokeWidth={1.35} />
        </button>
        <button className="ld-icon-btn" title="Forward" type="button">
          <ChevronRight size={12} color="var(--ink-3)" strokeWidth={1.35} />
        </button>
        <span className="workspace-topbar__nav-sep" />
        <button className="ld-icon-btn" title="New entry" type="button">
          <Plus size={11} color="var(--ink-3)" strokeWidth={1.35} />
        </button>
      </div>

      <div className="workspace-topbar__center">
        <button
          className="workspace-topbar__command-trigger"
          onClick={onOpenCommandPalette}
          type="button"
        >
          <span className="workspace-topbar__command-copy">
            <Search className="workspace-topbar__command-icon" size={11} strokeWidth={1.4} />
            <span className="workspace-topbar__command-placeholder">Go to anything…</span>
          </span>
          <Kbd className="workspace-topbar__command-kbd">⌘P</Kbd>
        </button>
      </div>

      <div className="workspace-topbar__end">
        <button
          className="ld-icon-btn"
          onClick={onToggleInspector}
          title="Toggle inspector"
          type="button"
        >
          <PanelRight size={12} color="var(--ink-3)" strokeWidth={1.35} />
        </button>
      </div>
    </header>
  );
}
