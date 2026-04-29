import { Folder, Plus, Search } from "lucide-react";
import { useMemo, useState } from "react";
import logo from "@assets/logo.webp";
import type { Workspace } from "../types";
import { WorkspaceCard } from "./WorkspaceCard";

interface WorkspaceListProps {
  onNew: () => void;
  onOpen: (workspace: Workspace) => void;
  onOpenFolder: () => void;
  onRemove: (workspace: Workspace) => void;
  workspaces: Workspace[];
}

export function WorkspaceList({
  onNew,
  onOpen,
  onOpenFolder,
  onRemove,
  workspaces,
}: WorkspaceListProps) {
  const [query, setQuery] = useState("");

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return workspaces;
    return workspaces.filter(
      (w) => w.name.toLowerCase().includes(q) || w.path.toLowerCase().includes(q),
    );
  }, [workspaces, query]);

  const handleAction = (kind: "copy" | "open" | "remove" | "reveal", ws: Workspace) => {
    if (kind === "open") onOpen(ws);
    else if (kind === "remove") onRemove(ws);
    else if (kind === "copy") {
      try {
        navigator.clipboard?.writeText(ws.path);
      } catch {
        /* noop */
      }
    }
  };

  const isEmpty = workspaces.length === 0;

  return (
    <div className="wiz-inner wiz-fade">
      <div className="wiz-hero">
        <div className="wiz-hero__left">
          <img alt="Lore Designer" className="wiz-hero__logo" src={logo} />
          <div>
            <h1 className="wiz-hero__name">Lore Designer</h1>
            <div className="wiz-hero__sub">Where worlds keep their notes.</div>
          </div>
        </div>
        <div className="wiz-hero__actions">
          <button className="btn" onClick={onOpenFolder} type="button">
            <Folder size={14} /> Open Folder
          </button>
          <button className="btn primary" onClick={onNew} type="button">
            <Plus size={14} /> New Workspace
          </button>
        </div>
      </div>

      {!isEmpty && (
        <div className="wiz-search">
          <Search size={14} />
          <input
            autoFocus
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Filter recent workspaces…"
            value={query}
          />
          <span className="kbd">⌘K</span>
        </div>
      )}

      {isEmpty ? (
        <div className="wiz-empty">
          <div className="wiz-empty__glyph">🪶</div>
          <h3>No workspaces yet</h3>
          <p>
            A workspace is a folder of Markdown files — your characters, places, and lore. Start a
            new one, or open an existing folder of writing.
          </p>
          <div className="wiz-empty__actions">
            <button className="btn" onClick={onOpenFolder} type="button">
              <Folder size={14} /> Open Folder
            </button>
            <button className="btn primary" onClick={onNew} type="button">
              <Plus size={14} /> Create Workspace
            </button>
          </div>
        </div>
      ) : filtered.length === 0 ? (
        <div className="wiz-empty">
          <div className="wiz-empty__glyph">🔎</div>
          <h3>No matches</h3>
          <p>Nothing in your recent workspaces matches "{query}".</p>
        </div>
      ) : (
        <>
          <div className="wiz-count">Recent · {filtered.length}</div>
          <div className="wiz-grid">
            {filtered.map((ws) => (
              <WorkspaceCard key={ws.id} onAction={handleAction} onOpen={onOpen} workspace={ws} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
