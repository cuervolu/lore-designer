import { cn } from "@lore/ui";
import { FileTree, useFileTree, useFileTreeSearch } from "@pierre/trees/react";
import { useEffect, useRef, type CSSProperties } from "react";
import { AlertCircle, ChevronDown, Clock, Search, StickyNote, Tag, Trash2 } from "lucide-react";
import { useAppVersion } from "@/api/app";
import { getTypeTone, isDocumentPath } from "@/store/editor-shell-helpers";
import { useEditorShellStore } from "@/store/editor-shell";

const TREE_UNSAFE_CSS = `
  :host {
    background: transparent;
  }

  [data-type='scroll-container'] {
    padding: 0;
  }

  [data-type='item'] {
    border-radius: 5px;
    margin-inline: 8px;
  }

  [data-type='item']:hover {
    background: rgba(125, 118, 102, 0.06);
  }

  [data-type='item'][data-item-selected] {
    background: var(--trees-selected-bg);
    color: var(--trees-selected-fg);
  }

  [data-type='item'][data-item-selected] [data-item-section='content'] {
    font-weight: 500;
  }

  [data-type='item'][data-item-type='folder']:not([data-item-parent-path]) {
    margin-inline: 0;
    border-radius: 0;
    padding-inline: 10px;
    color: var(--trees-fg-muted);
    font-size: 11px;
    font-weight: 550;
    letter-spacing: 0.04em;
    text-transform: uppercase;
  }

  [data-type='item'][data-item-type='folder']:not([data-item-parent-path]):hover,
  [data-type='item'][data-item-type='folder']:not([data-item-parent-path])[data-item-selected] {
    background: transparent;
    color: var(--trees-fg-muted);
  }

  [data-type='item'][data-item-type='folder']:not([data-item-parent-path]) [data-item-section='icon'] {
    color: var(--trees-fg-muted);
  }

  [data-type='item'][data-item-type='file'] {
    font-size: 12.5px;
    letter-spacing: -0.003em;
  }

  [data-type='item'][data-item-type='file'] [data-item-section='content'] {
    color: var(--trees-fg);
  }

  [data-type='item'][data-item-type='file'][data-item-selected] [data-item-section='content'] {
    color: var(--trees-selected-fg);
  }

  [data-item-section='decoration'] {
    font-family: var(--trees-font-family);
    font-size: 10.5px;
    color: var(--trees-fg-muted);
  }
`;

type MiscItem = {
  Icon: typeof AlertCircle | typeof Clock | typeof Tag | typeof Trash2;
  badge?: string;
  label: string;
};

const MISC_ITEMS: readonly MiscItem[] = [
  { Icon: Tag, label: "Types & templates" },
  { Icon: Clock, label: "Timeline" },
  { Icon: AlertCircle, label: "Open threads", badge: "3" },
  { Icon: Trash2, label: "Trash" },
] as const;

const TREE_STYLE = {
  height: "100%",
  width: "100%",
  "--trees-accent-override": "var(--sel-ink)",
  "--trees-bg-override": "transparent",
  "--trees-bg-muted-override": "rgba(125, 118, 102, 0.06)",
  "--trees-border-color-override": "transparent",
  "--trees-fg-override": "var(--ink)",
  "--trees-fg-muted-override": "var(--ink-3)",
  "--trees-font-family-override": "var(--font-sans)",
  "--trees-font-size-override": "12.5px",
  "--trees-font-weight-regular-override": "400",
  "--trees-font-weight-semibold-override": "550",
  "--trees-focus-ring-color-override": "rgba(61, 86, 124, 0.24)",
  "--trees-focus-ring-offset-override": "-1px",
  "--trees-gap-override": "7px",
  "--trees-item-margin-x-override": "0px",
  "--trees-item-padding-x-override": "10px",
  "--trees-level-gap-override": "18px",
  "--trees-padding-inline-override": "0px",
  "--trees-selected-bg-override": "var(--sel-bg)",
  "--trees-selected-fg-override": "var(--sel-ink)",
  "--trees-selected-focused-border-color-override": "rgba(61, 86, 124, 0.3)",
} as CSSProperties;

function countWords(text: string) {
  return text.trim().split(/\s+/).filter(Boolean).length;
}

function formatCompactWords(words: number) {
  if (words >= 1000) {
    return `${(words / 1000).toFixed(1)}k`;
  }

  return String(words);
}

function normalizeDirectoryPath(path: string) {
  return path.endsWith("/") ? path : `${path}/`;
}

export function LoreNavigator() {
  const {
    activePath,
    documents,
    expandedPaths,
    openDocument,
    searchQuery,
    setSearchQuery,
    treePaths,
    workspaceName,
    workspaceNodes,
  } = useEditorShellStore();
  const selectionStateRef = useRef({ activePath, documents, openDocument });

  useEffect(() => {
    selectionStateRef.current = { activePath, documents, openDocument };
  }, [activePath, documents, openDocument]);

  const { model } = useFileTree({
    density: "compact",
    fileTreeSearchMode: "hide-non-matches",
    icons: "minimal",
    initialExpandedPaths: expandedPaths.map(normalizeDirectoryPath),
    initialSearchQuery: searchQuery.trim() === "" ? null : searchQuery,
    initialSelectedPaths: [activePath],
    onSelectionChange(selectedPaths) {
      const {
        activePath: currentActivePath,
        documents: currentDocuments,
        openDocument,
      } = selectionStateRef.current;
      const selectedPath =
        selectedPaths.length > 0 ? selectedPaths[selectedPaths.length - 1] : undefined;
      if (selectedPath == null || selectedPath === currentActivePath) {
        return;
      }

      if (isDocumentPath(selectedPath, currentDocuments)) {
        openDocument(selectedPath);
      }
    },
    paths: treePaths,
    renderRowDecoration({ item, row }) {
      if (row.kind === "directory" && row.level === 0) {
        const section = workspaceNodes.find((node) => node.path === item.path.replace(/\/$/, ""));
        if (section?.children?.length != null) {
          return { text: String(section.children.length) };
        }
      }

      if (row.kind === "file" && isDocumentPath(item.path, documents)) {
        const document = documents[item.path];
        if (document.kind === "draft") {
          const words = document.content.reduce((total, block) => {
            if (block.type === "heading") {
              return total + countWords(block.text);
            }

            if (block.type === "paragraph" || block.type === "callout") {
              return (
                total +
                block.segments.reduce((segmentTotal, segment) => {
                  return segmentTotal + countWords(segment.text);
                }, 0)
              );
            }

            return (
              total +
              block.items.reduce((itemsTotal, itemSegments) => {
                return (
                  itemsTotal +
                  itemSegments.reduce((segmentTotal, segment) => {
                    return segmentTotal + countWords(segment.text);
                  }, 0)
                );
              }, 0)
            );
          }, 0);

          return { text: formatCompactWords(words) };
        }
      }

      return null;
    },
    unsafeCSS: TREE_UNSAFE_CSS,
  });

  const treeSearch = useFileTreeSearch(model);
  const appVersion = useAppVersion();
  const projectInitial = workspaceName.charAt(0).toUpperCase();

  useEffect(() => {
    model.resetPaths(treePaths, {
      initialExpandedPaths: expandedPaths.map(normalizeDirectoryPath),
    });
  }, [expandedPaths, model, treePaths]);

  useEffect(() => {
    if (treeSearch.value !== searchQuery) {
      treeSearch.setValue(searchQuery.trim() === "" ? null : searchQuery);
    }
  }, [searchQuery, treeSearch]);

  useEffect(() => {
    const currentSelection = model.getSelectedPaths();
    currentSelection.forEach((path) => {
      if (path !== activePath) {
        model.getItem(path)?.deselect();
      }
    });

    const activeItem = model.getItem(activePath);
    activeItem?.select();
    model.focusPath(activePath);
  }, [activePath, model]);

  return (
    <nav className="navigator">
      <div className="navigator__header">
        <div className="navigator__project-icon">{projectInitial}</div>
        <div style={{ minWidth: 0, flex: 1 }}>
          <div className="navigator__project-name">{workspaceName}</div>
          <div className="navigator__project-path">~/Documents/Lore/{workspaceName}</div>
        </div>
        <button className="ld-icon-btn" title="Project menu" type="button">
          <ChevronDown size={11} strokeWidth={1.4} />
        </button>
      </div>

      <div className="navigator__search">
        <div className="navigator__search-box">
          <Search size={12} color="var(--ink-4)" strokeWidth={1.4} />
          <input
            onChange={(e) => {
              treeSearch.setValue(e.target.value);
              setSearchQuery(e.target.value);
            }}
            placeholder="Search project"
            value={treeSearch.value}
          />
          <span className="navigator__search-kbd">⌘K</span>
        </div>
      </div>

      <div className="navigator__sections">
        <div className="navigator__tree-frame">
          <FileTree className="navigator__tree-host" model={model} style={TREE_STYLE} />
        </div>
      </div>

      <div className="navigator__extras">
        <div className="navigator__divider" />
        {MISC_ITEMS.map(({ Icon, label, badge }) => (
          <button className="navigator__misc-item" key={label} type="button">
            <Icon size={12} color="var(--ink-3)" strokeWidth={1.4} />
            <span>{label}</span>
            {badge && <span className="navigator__badge">{badge}</span>}
          </button>
        ))}
      </div>

      <div className="navigator__footer">
        <span>Saved 14:02</span>
        <span className="navigator__footer-version">{appVersion ? `v${appVersion}` : "v…"}</span>
      </div>
    </nav>
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

export { StickyNote };
