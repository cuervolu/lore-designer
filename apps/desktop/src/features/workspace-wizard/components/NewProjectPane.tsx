import { useState } from "react";

interface NewProjectPaneProps {
  onOpen: () => void;
}

const TEMPLATES = [
  {
    id: "blank",
    title: "Blank project",
    desc: "An empty workspace. Add characters, places, and drafts as you go.",
  },
  {
    id: "novel",
    title: "Long-form novel",
    desc: "Pre-organized for chapters, character files, and a places index.",
  },
  {
    id: "game",
    title: "Game / narrative bible",
    desc: "Factions, regions, and quest-style threads for ongoing development.",
  },
  {
    id: "sample",
    title: "Sample — Saltreach Cycle",
    desc: "A small worked example showing how characters, places, and drafts link together.",
  },
];

export function NewProjectPane({ onOpen }: NewProjectPaneProps) {
  const [name, setName] = useState("");
  const [path, setPath] = useState("~/Documents/Lore");
  const [template, setTemplate] = useState("blank");

  return (
    <div className="wiz-form-wrap">
      <h1 className="wiz-form-title">New project</h1>

      <div className="wiz-form-row">
        <label className="wiz-form-label">Name</label>
        <input
          autoFocus
          className="wiz-input"
          onChange={(e) => setName(e.target.value)}
          placeholder="The Quiet Atlas"
          value={name}
        />
      </div>

      <div className="wiz-form-row">
        <label className="wiz-form-label">Location</label>
        <div>
          <div style={{ display: "flex", gap: 6 }}>
            <input
              className="wiz-input wiz-input--mono"
              onChange={(e) => setPath(e.target.value)}
              style={{ flex: 1 }}
              value={path}
            />
            <button className="ld-btn" type="button">
              Choose…
            </button>
          </div>
          <div className="wiz-input-hint">
            A folder will be created at this path. You can move it later.
          </div>
        </div>
      </div>

      <div className="wiz-form-row">
        <label className="wiz-form-label">Start from</label>
        <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
          {TEMPLATES.map((t) => (
            <button
              className={`wiz-template-option${template === t.id ? " selected" : ""}`}
              key={t.id}
              onClick={() => setTemplate(t.id)}
              type="button"
            >
              <div className={`wiz-template-option__radio${template === t.id ? " selected" : ""}`}>
                {template === t.id && <div className="wiz-template-option__radio-dot" />}
              </div>
              <div>
                <div className="wiz-template-option__title">{t.title}</div>
                <div className="wiz-template-option__desc">{t.desc}</div>
                {t.id === "sample" && template === "sample" && <SampleSummary />}
              </div>
            </button>
          ))}
        </div>
      </div>

      <div className="wiz-form-actions">
        <button className="ld-btn" type="button">
          Cancel
        </button>
        <button className="ld-btn primary" onClick={onOpen} type="button">
          Create project
        </button>
      </div>
    </div>
  );
}

function SampleSummary() {
  return (
    <div
      style={{
        marginTop: 10,
        padding: "8px 10px",
        borderRadius: 5,
        background: "rgba(125,118,102,0.08)",
        border: "1px solid var(--divider)",
        fontSize: 11.5,
        color: "var(--ink-2)",
        lineHeight: 1.55,
        fontFamily: "var(--font-sans)",
      }}
    >
      <div style={{ color: "var(--ink-3)", marginBottom: 4 }}>Includes</div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "2px 16px" }}>
        <span>5 characters · Wren, Corvin, Mira…</span>
        <span>5 locations · Saltreach, Lowmarsh…</span>
        <span>3 factions · 4 lore entries</span>
        <span>4 drafts incl. Chapter 01</span>
        <span>2 open threads</span>
        <span>Cross-references between all of the above</span>
      </div>
    </div>
  );
}
