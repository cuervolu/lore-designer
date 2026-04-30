import { cn } from "@lore/ui";
import { useEditorShellStore } from "@/store/editor-shell";

export function EditorTabs() {
  const { activePath, documents, openDocument, tabs } = useEditorShellStore();

  return (
    <div className="editor-tabs" role="tablist" aria-label="Open documents">
      {tabs.map((tab) => {
        const document = documents[tab.path];
        const isActive = tab.path === activePath;

        return (
          <button
            aria-selected={isActive}
            className={cn("editor-tab", isActive && "editor-tab--active")}
            key={tab.path}
            onClick={() => openDocument(tab.path)}
            role="tab"
            type="button"
          >
            <span className="editor-tab__icon">{document?.icon ?? "📄"}</span>
            <span className="editor-tab__title">{tab.title}</span>
            <span
              aria-hidden="true"
              className={cn("editor-tab__dirty", tab.dirty && "editor-tab__dirty--visible")}
            />
          </button>
        );
      })}
    </div>
  );
}
