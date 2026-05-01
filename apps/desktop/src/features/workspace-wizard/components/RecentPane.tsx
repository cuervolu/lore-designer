import { useState } from "react";
import { BookOpen, Folder, Search } from "lucide-react";
import type { RecentProject } from "../WorkspaceWizard";

interface RecentPaneProps {
  onNew: () => void;
  onOpen: () => void;
  recents: RecentProject[];
}

export function RecentPane({ onNew, onOpen, recents }: RecentPaneProps) {
  const [q, setQ] = useState("");
  const [sel, setSel] = useState(0);

  const filtered = recents.filter((r) => r.name.toLowerCase().includes(q.toLowerCase()));
  const project = filtered[sel] ?? filtered[0];

  return (
    <div className="wiz-recent-grid">
      <div style={{ minWidth: 0 }}>
        <div className="wiz-recent-header">
          <h1 className="wiz-recent-title">Recent projects</h1>
          <div style={{ display: "flex", gap: 6 }}>
            <button className="ld-btn" onClick={onNew} type="button">
              New project
            </button>
            <button className="ld-btn primary" type="button">
              Open…
            </button>
          </div>
        </div>

        <div className="wiz-search-box">
          <Search size={12} color="var(--ink-4)" strokeWidth={1.4} />
          <input
            onChange={(e) => {
              setQ(e.target.value);
              setSel(0);
            }}
            placeholder="Filter projects"
            value={q}
          />
          <kbd
            style={{
              fontFamily: "var(--font-mono)",
              fontSize: 10.5,
              color: "var(--ink-4)",
              background: "var(--kbd-bg)",
              padding: "1px 5px",
              borderRadius: 3,
              border: "1px solid var(--divider)",
            }}
          >
            ⌘F
          </kbd>
        </div>

        <div className="wiz-list-header">
          <span>Project</span>
          <span style={{ textAlign: "right" }}>Words</span>
          <span style={{ textAlign: "right" }}>Opened</span>
        </div>

        {filtered.map((r, i) => (
          <button
            className={`wiz-project-row${i === sel ? " selected" : ""}`}
            key={r.name}
            onClick={() => setSel(i)}
            onDoubleClick={onOpen}
            type="button"
          >
            <Folder
              size={13}
              color={i === sel ? "var(--sel-ink)" : "var(--ink-3)"}
              strokeWidth={1.4}
            />
            <div style={{ minWidth: 0 }}>
              <div className="wiz-project-name">
                {r.name}
                {r.threads > 0 && <span className="wiz-thread-chip">{r.threads}</span>}
              </div>
              <div className="wiz-project-path">{r.path}</div>
            </div>
            <div className="wiz-project-words">
              {(r.words / 1000).toFixed(r.words >= 100000 ? 0 : 1)}k
            </div>
            <div className="wiz-project-opened">{r.opened}</div>
          </button>
        ))}

        <div
          style={{
            marginTop: 14,
            paddingTop: 12,
            borderTop: "1px dashed var(--divider)",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <div style={{ fontSize: 12, color: "var(--ink-3)", fontFamily: "var(--font-sans)" }}>
            Don't have a project yet?
          </div>
          <button
            className="ld-btn"
            onClick={onOpen}
            type="button"
            style={{ display: "inline-flex", alignItems: "center", gap: 6 }}
          >
            <BookOpen size={12} color="var(--ink-2)" strokeWidth={1.4} />
            Try the sample project
          </button>
        </div>
      </div>

      {project && <ProjectPreview onOpen={onOpen} project={project} />}
    </div>
  );
}

function ProjectPreview({ project, onOpen }: { project: RecentProject; onOpen: () => void }) {
  return (
    <aside className="wiz-preview">
      <div className="wiz-preview__label">Preview</div>
      <div className="wiz-preview__name">{project.name}</div>
      <div className="wiz-preview__path">{project.path}</div>

      <div className="wiz-preview__stats">
        <span className="wiz-preview__stat-label">Characters</span>
        <span className="wiz-preview__stat-value">{project.items.characters}</span>
        <span className="wiz-preview__stat-label">Locations</span>
        <span className="wiz-preview__stat-value">{project.items.locations}</span>
        <span className="wiz-preview__stat-label">Factions</span>
        <span className="wiz-preview__stat-value">{project.items.factions}</span>
        <span className="wiz-preview__stat-label">Lore</span>
        <span className="wiz-preview__stat-value">{project.items.lore}</span>
        <span className="wiz-preview__stat-label">Drafts</span>
        <span className="wiz-preview__stat-value">{project.items.drafts}</span>
        <span className="wiz-preview__stat-label">Words</span>
        <span className="wiz-preview__stat-value">{project.words.toLocaleString()}</span>
      </div>

      {project.threads > 0 && (
        <div className="wiz-preview__threads">
          <span style={{ color: "var(--thread)", fontWeight: 500 }}>
            {project.threads} open threads
          </span>{" "}
          across {Math.max(2, Math.floor(project.threads * 0.7))} entries.
        </div>
      )}

      <div className="wiz-preview__actions">
        <button className="ld-btn primary" onClick={onOpen} type="button">
          Open project
        </button>
        <button className="ld-btn" type="button">
          Show folder
        </button>
      </div>
    </aside>
  );
}
