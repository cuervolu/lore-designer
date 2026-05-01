import { BookOpen, Compass, FileText, Flag, Plus, User, X } from 'lucide-react';
import { useEditorShellStore } from '@/store/editor-shell';

const KIND_ICON: Record<string, JSX.Element> = {
  character: <User size={11} strokeWidth={1.4} />,
  location: <Compass size={11} strokeWidth={1.4} />,
  draft: <FileText size={11} strokeWidth={1.4} />,
  lore: <BookOpen size={11} strokeWidth={1.4} />,
  faction: <Flag size={11} strokeWidth={1.4} />,
};

export function EditorTabs() {
  const { activePath, documents, openDocument, tabs } = useEditorShellStore();

  return (
    <div className="editor-tabs" role="tablist" aria-label="Open documents">
      {tabs.map((tab) => {
        const doc = documents[tab.path];
        const isActive = tab.path === activePath;
        const icon = doc ? (
          (KIND_ICON[doc.kind] ?? <FileText size={11} strokeWidth={1.4} />)
        ) : (
          <FileText size={11} strokeWidth={1.4} />
        );

        return (
          <button
            aria-selected={isActive}
            className={`editor-tab${isActive ? ' active' : ''}`}
            key={tab.path}
            onClick={() => openDocument(tab.path)}
            role="tab"
            type="button"
          >
            <span style={{ color: isActive ? 'var(--sel-ink)' : 'var(--ink-4)', flexShrink: 0 }}>
              {icon}
            </span>
            <span className="editor-tab__title">{tab.title}</span>
            {tab.dirty && <span className="editor-tab__dirty" aria-hidden="true" />}
            <span className="editor-tab__close" aria-hidden="true">
              <X size={10} color="var(--ink-4)" strokeWidth={1.4} />
            </span>
          </button>
        );
      })}
      <button className="editor-tab-new" title="New tab" type="button">
        <Plus size={11} color="var(--ink-4)" strokeWidth={1.4} />
      </button>
    </div>
  );
}
