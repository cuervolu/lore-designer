import { Button, cn } from "@lore/ui";
import { FileTree, useFileTree } from "@pierre/trees/react";
import { Plus, Search } from "lucide-react";
import { useDeferredValue, useEffect, useRef } from "react";
import { getTypeTone, isDocumentPath } from "@core/editor-shell/helpers";
import { useEditorShellStore } from "@core/editor-shell/store";

const treeUnsafeCss = `
  :host {
    --trees-bg-override: transparent;
    --trees-border-color-override: transparent;
    --trees-fg-override: rgba(244, 245, 247, 0.92);
    --trees-muted-fg-override: rgba(150, 156, 171, 0.88);
    --trees-directory-fg-override: rgba(239, 241, 247, 0.96);
    --trees-selected-bg-override: rgba(41, 84, 59, 0.88);
    --trees-selected-fg-override: rgba(244, 249, 246, 0.96);
    --trees-hover-bg-override: rgba(255, 255, 255, 0.05);
    --trees-focus-ring-override: rgba(102, 220, 152, 0.55);
    --trees-search-bg-override: rgba(255, 255, 255, 0.06);
    --trees-search-border-override: rgba(255, 255, 255, 0.08);
    --trees-search-fg-override: rgba(241, 244, 247, 0.9);
    --trees-sticky-bg-override: rgba(21, 24, 31, 0.96);
    min-height: 0;
    width: 100%;
  }

  * {
    scrollbar-width: none;
  }

  *::-webkit-scrollbar {
    display: none;
  }

  [data-file-tree-row][data-item-selected] {
    border-radius: 12px;
    box-shadow: inset 1px 0 0 rgba(112, 255, 170, 0.95);
  }

  [data-file-tree-row][data-kind='directory'] [data-type='item-label'] {
    font-weight: 600;
  }

  [data-type='header'] {
    position: sticky;
    top: 0;
    z-index: 2;
    background: linear-gradient(180deg, rgba(20, 22, 28, 0.98), rgba(20, 22, 28, 0.92));
    border-bottom: 1px solid rgba(255, 255, 255, 0.04);
    backdrop-filter: blur(14px);
  }
`;

export function LoreFileTree() {
  const {
    activePath,
    documents,
    openDocument,
    searchQuery,
    setSearchQuery,
    treePaths,
    workspaceName,
  } = useEditorShellStore();
  const deferredQuery = useDeferredValue(searchQuery);
  const openDocumentRef = useRef(openDocument);

  useEffect(() => {
    openDocumentRef.current = openDocument;
  }, [openDocument]);

  const { model } = useFileTree({
    density: "compact",
    fileTreeSearchMode: "hide-non-matches",
    flattenEmptyDirectories: true,
    initialExpandedPaths: ["Characters", "Locations", "Drafts"],
    initialSelectedPaths: [activePath],
    onSelectionChange: (selectedPaths) => {
      const nextPath = selectedPaths[selectedPaths.length - 1];
      if (nextPath == null || !isDocumentPath(nextPath, documents)) {
        return;
      }

      openDocumentRef.current(nextPath);
    },
    paths: treePaths,
    renderRowDecoration: ({ item }) => {
      const document = documents[item.path];
      if (document == null) {
        return null;
      }

      return {
        text: document.kind === "draft" ? "draft" : document.kind,
        title: document.kind,
      };
    },
    search: false,
    stickyFolders: true,
    unsafeCSS: treeUnsafeCss,
  });

  useEffect(() => {
    model.setSearch(deferredQuery.trim() === "" ? null : deferredQuery);
  }, [deferredQuery, model]);

  useEffect(() => {
    const currentSelection = model.getSelectedPaths();
    if (currentSelection.includes(activePath)) {
      return;
    }

    currentSelection.forEach((path) => model.getItem(path)?.deselect());
    model.getItem(activePath)?.select();
    model.focusNearestPath(activePath);
  }, [activePath, model]);

  return (
    <div className="editor-sidebar">
      <div className="editor-sidebar__header">
        <div>
          <p className="editor-sidebar__eyebrow">Workspace</p>
          <h2 className="editor-sidebar__title">{workspaceName}</h2>
        </div>
        <Button className="editor-sidebar__add" size="sm" variant="ghost">
          <Plus />
        </Button>
      </div>

      <label className="editor-search">
        <Search className="size-4" />
        <input
          aria-label="Search files"
          onChange={(event) => setSearchQuery(event.target.value)}
          placeholder="Search files"
          value={searchQuery}
        />
      </label>

      <div className="editor-tree-wrap">
        <FileTree
          className="editor-tree-host"
          header={
            <div className="editor-tree-header">
              <span className="editor-tree-header__label">Workspace files</span>
              <span className="editor-tree-header__meta">{treePaths.length} items</span>
            </div>
          }
          model={model}
        />
      </div>

      <div className="editor-sidebar__footer">
        <button className="editor-sidebar__footer-action" type="button">
          Trash
        </button>
        <button className="editor-sidebar__footer-action" type="button">
          Types
        </button>
      </div>
    </div>
  );
}

export function InlineEntityTag({
  className,
  label,
  tone,
}: {
  className?: string;
  label: string;
  tone: "character" | "draft" | "faction" | "location" | "lore" | "muted" | "system";
}) {
  return (
    <span
      className={cn(
        "entity-link",
        tone === "muted" ? "entity-link--muted" : getTypeTone(tone),
        className,
      )}
    >
      {label}
    </span>
  );
}
