import { Settings } from 'lucide-react';
import { getActiveDataset, useEditorShellStore } from '@/store/editor-shell';

interface WorkspaceSidebarProps {
  onOpenSettings: () => void;
  onSwitchProject: () => void;
}

export function WorkspaceSidebar({ onOpenSettings, onSwitchProject }: WorkspaceSidebarProps) {
  const state = useEditorShellStore();
  const dataset = getActiveDataset(state);

  return (
    <aside className="workspace-sidebar">
      <div className="project-switcher">
        <button className="project-switcher__button" onClick={onSwitchProject} type="button">
          <span className="project-switcher__label">Project</span>
          <span className="project-switcher__name">
            {state.projectName}
            <em>switch ⌄</em>
          </span>
        </button>
        <button
          aria-label="Open settings"
          className="icon-button"
          onClick={onOpenSettings}
          type="button"
        >
          <Settings aria-hidden="true" size={15} strokeWidth={1.6} />
        </button>
      </div>
      <nav className="entry-navigation" aria-label="Project entries">
        {dataset.groupOrder.map((group) => (
          <section className="entry-group" key={group}>
            <h2>{group}</h2>
            {dataset.entries
              .filter((entry) => entry.group === group)
              .map((entry) => {
                const active = entry.id === state.selectedEntryId;
                return (
                  <button
                    aria-current={active ? 'page' : undefined}
                    className={`entry-link${active ? ' is-active' : ''}`}
                    key={entry.id}
                    onClick={() => state.selectEntry(entry.id)}
                    type="button"
                  >
                    <span className="entry-link__dot" />
                    <span>{entry.title}</span>
                  </button>
                );
              })}
          </section>
        ))}
      </nav>
    </aside>
  );
}
