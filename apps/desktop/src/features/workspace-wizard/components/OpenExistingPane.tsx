import { useState } from "react";
import {
  BookOpen,
  Check,
  ChevronDown,
  ChevronRight,
  FileText,
  Folder,
  FolderOpen,
  Home,
} from "lucide-react";

interface OpenExistingPaneProps {
  onOpen: () => void;
}

type NodeKind = "folder" | "project" | "file";
interface FolderNode {
  name: string;
  kind: NodeKind;
  expanded?: boolean;
  valid?: boolean;
  meta?: string;
  children?: FolderNode[];
}

const FOLDER_TREE: FolderNode[] = [
  {
    name: "Documents",
    kind: "folder",
    children: [
      {
        name: "Lore",
        kind: "folder",
        expanded: true,
        children: [
          { name: "Saltreach Cycle", kind: "project", valid: true, meta: "project · 38.4k words" },
          { name: "Northern", kind: "project", valid: true, meta: "project · 4.9k words" },
          { name: "archive", kind: "folder" },
        ],
      },
      {
        name: "Drafts",
        kind: "folder",
        expanded: true,
        children: [
          { name: "Ironvigil", kind: "project", valid: true, meta: "project · 8.2k words" },
          { name: "short pieces.md", kind: "file" },
        ],
      },
    ],
  },
  {
    name: "Writing",
    kind: "folder",
    expanded: true,
    children: [
      { name: "The Quiet Atlas", kind: "project", valid: true, meta: "project · 112.7k words" },
      { name: "sketches", kind: "folder" },
    ],
  },
  {
    name: "Projects",
    kind: "folder",
    children: [
      {
        name: "Glassbound",
        kind: "folder",
        children: [
          { name: "lore", kind: "project", valid: true, meta: "project · 21.5k words" },
          { name: "src", kind: "folder" },
        ],
      },
    ],
  },
];

const RECENT_FOLDERS = [
  { name: "Lore", path: "~/Documents/Lore" },
  { name: "Drafts", path: "~/Documents/Drafts" },
  { name: "Writing", path: "~/Writing" },
  { name: "Glassbound", path: "~/Projects/Glassbound" },
  { name: "Northern", path: "~/Documents/Lore/Northern" },
  { name: "Desktop", path: "~/Desktop" },
];

interface PickedNode {
  name: string;
  kind: NodeKind;
  valid?: boolean;
}

export function OpenExistingPane({ onOpen }: OpenExistingPaneProps) {
  const [picked, setPicked] = useState<PickedNode>({
    name: "Saltreach Cycle",
    kind: "project",
    valid: true,
  });

  return (
    <div className="wiz-open-wrap">
      <div className="wiz-open-header">
        <h1 className="wiz-recent-title">Open existing project</h1>
        <button className="ld-btn primary" type="button">
          Browse…
        </button>
      </div>

      <div className="wiz-open-grid">
        <div>
          <div className="wiz-section-label">Recent folders</div>
          <div className="wiz-folder-chips">
            {RECENT_FOLDERS.map((f) => (
              <button className="wiz-folder-chip" key={f.path} type="button">
                <Folder size={13} color="var(--ink-3)" strokeWidth={1.4} />
                <div>
                  <div className="wiz-folder-chip__name">{f.name}</div>
                  <div className="wiz-folder-chip__path">{f.path}</div>
                </div>
              </button>
            ))}
          </div>

          <div className="wiz-section-label">Browse</div>
          <div className="wiz-browser">
            <div className="wiz-browser__toolbar">
              <Home size={11} color="var(--ink-4)" strokeWidth={1.4} />
              <span>~/</span>
              <span style={{ flex: 1 }} />
              <kbd
                style={{
                  fontFamily: "var(--font-mono)",
                  fontSize: 10,
                  color: "var(--ink-4)",
                  background: "var(--kbd-bg)",
                  padding: "1px 4px",
                  borderRadius: 3,
                  border: "1px solid var(--divider)",
                }}
              >
                ⌘O
              </kbd>
            </div>
            <div className="wiz-browser__tree">
              {FOLDER_TREE.map((node, i) => (
                <BrowserNode depth={0} key={i} node={node} onPick={setPicked} picked={picked} />
              ))}
            </div>
          </div>

          <div className="wiz-what-is">
            <div className="wiz-what-is__title">What is a Lore Designer project</div>
            A folder that contains a{" "}
            <code style={{ fontFamily: "var(--font-mono)", fontSize: 11, color: "var(--ink)" }}>
              project.lore
            </code>{" "}
            manifest at its root, alongside an{" "}
            <code style={{ fontFamily: "var(--font-mono)", fontSize: 11, color: "var(--ink)" }}>
              entries/
            </code>{" "}
            directory. Plain Markdown files inside an entries folder are treated as drafts and
            notes.
          </div>
        </div>

        <PickedPanel onOpen={onOpen} picked={picked} />
      </div>
    </div>
  );
}

function BrowserNode({
  node,
  depth,
  picked,
  onPick,
}: {
  node: FolderNode;
  depth: number;
  picked: PickedNode | null;
  onPick: (n: PickedNode) => void;
}) {
  const [open, setOpen] = useState(!!node.expanded);
  const isProject = node.kind === "project";
  const isFolder = node.kind === "folder";
  const isPicked = picked?.name === node.name;

  return (
    <>
      <button
        className={`wiz-browser-row${isPicked ? " picked" : ""}`}
        onClick={() => {
          if (isFolder) setOpen((o) => !o);
          if (isProject) onPick(node);
        }}
        style={{ padding: `3px 10px 3px ${10 + depth * 16}px` }}
        type="button"
      >
        <span style={{ color: "var(--ink-4)" }}>
          {isFolder ? (
            open ? (
              <ChevronDown size={9} strokeWidth={1.4} />
            ) : (
              <ChevronRight size={9} strokeWidth={1.4} />
            )
          ) : null}
        </span>
        {isProject ? (
          <BookOpen size={12} color="var(--sel-ink)" strokeWidth={1.4} />
        ) : isFolder && open ? (
          <FolderOpen size={13} color="var(--ink-3)" strokeWidth={1.4} />
        ) : isFolder ? (
          <Folder size={13} color="var(--ink-3)" strokeWidth={1.4} />
        ) : (
          <FileText size={12} color="var(--ink-3)" strokeWidth={1.4} />
        )}
        <span
          style={{
            fontWeight: isProject ? 500 : 400,
            color: node.kind === "file" ? "var(--ink-3)" : "var(--ink)",
            overflow: "hidden",
            textOverflow: "ellipsis",
            whiteSpace: "nowrap",
          }}
        >
          {node.name}
        </span>
        {isProject && (
          <span style={{ fontFamily: "var(--font-mono)", fontSize: 10.5, color: "var(--ink-4)" }}>
            {node.meta?.replace("project · ", "")}
          </span>
        )}
      </button>
      {isFolder &&
        open &&
        node.children?.map((c, i) => (
          <BrowserNode depth={depth + 1} key={i} node={c} onPick={onPick} picked={picked} />
        ))}
    </>
  );
}

function PickedPanel({ picked, onOpen }: { picked: PickedNode | null; onOpen: () => void }) {
  if (!picked) {
    return (
      <aside className="wiz-picked">
        <p
          style={{ fontSize: 12, color: "var(--ink-3)", fontFamily: "var(--font-sans)", margin: 0 }}
        >
          Select a folder to inspect it.
        </p>
      </aside>
    );
  }
  return (
    <aside className="wiz-picked">
      <div className="wiz-picked__label">Selected</div>
      <div className="wiz-picked__name">{picked.name}</div>
      <div className="wiz-picked__path">~/Documents/Lore/{picked.name}</div>

      <div className="wiz-picked__manifest">
        <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 6 }}>
          <Check size={11} color="var(--good)" strokeWidth={1.4} />
          <span style={{ color: "var(--ink-2)", fontSize: 11.5, fontFamily: "var(--font-sans)" }}>
            Valid{" "}
            <code style={{ fontFamily: "var(--font-mono)", fontSize: 11, color: "var(--ink)" }}>
              project.lore
            </code>{" "}
            manifest
          </span>
        </div>
        <div
          style={{
            color: "var(--ink-3)",
            lineHeight: 1.5,
            fontSize: 11.5,
            fontFamily: "var(--font-sans)",
          }}
        >
          5 characters · 5 locations · 4 drafts
          <br />
          last opened today, 9:14
        </div>
      </div>

      <div style={{ display: "flex", gap: 6, marginTop: 12 }}>
        <button className="ld-btn primary" onClick={onOpen} type="button">
          Open
        </button>
        <button className="ld-btn" type="button">
          Show folder
        </button>
      </div>
    </aside>
  );
}
