import { useState } from "react";
import { ChevronRight, Plus } from "lucide-react";
import { useEditorShellStore } from "@/store/editor-shell";
import type { FrontmatterField } from "@/types/editor";

type InspectorTab = "context" | "fields" | "history";

const HISTORY_MOCK = [
  { when: "today, 14:02", what: "Edited section content", kind: "edit" },
  { when: "today, 13:48", what: "Added field: First appearance", kind: "meta" },
  { when: "yesterday", what: "Linked from Chapter 01", kind: "link" },
  { when: "Apr 26", what: "Updated frontmatter", kind: "meta" },
  { when: "Apr 24", what: "Created from template", kind: "create" },
];

export function PropertiesInspector() {
  const { activeDocument, updateFrontmatterField } = useEditorShellStore();
  const [tab, setTab] = useState<InspectorTab>("context");

  return (
    <aside className="inspector">
      <div className="inspector__header">
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div className="inspector__doc-name">{activeDocument.title}</div>
          <button className="ld-icon-btn" type="button">
            <ChevronRight size={11} color="var(--ink-4)" strokeWidth={1.4} />
          </button>
        </div>
        <div className="inspector__doc-type" style={{ textTransform: "capitalize" }}>
          {activeDocument.kind} ·{" "}
          {(activeDocument.frontmatter.find((f) => f.key === "role")?.value as string) ?? ""}
        </div>
        <div className="inspector__tabs">
          {(["context", "fields", "history"] as InspectorTab[]).map((t) => (
            <button
              className={`inspector__tab${tab === t ? " active" : ""}`}
              key={t}
              onClick={() => setTab(t)}
              type="button"
            >
              {t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>
      </div>

      <div className="inspector__body">
        {tab === "context" && <ContextPane doc={activeDocument} />}
        {tab === "fields" && (
          <FieldsPane doc={activeDocument} updateField={updateFrontmatterField} />
        )}
        {tab === "history" && <HistoryPane />}
      </div>
    </aside>
  );
}

function ContextPane({ doc }: { doc: ReturnType<typeof useEditorShellStore>["activeDocument"] }) {
  return (
    <>
      {/* Open threads */}
      <section className="inspector__section">
        <div className="inspector__section-header">
          <div className="inspector__section-title accent">
            <span className="inspector__section-accent-dot" />
            Open threads
            <span className="inspector__section-count">2</span>
          </div>
          <button className="ld-icon-btn" type="button">
            <Plus size={10} color="var(--ink-4)" strokeWidth={1.4} />
          </button>
        </div>
        <div className="inspector__thread-card">
          <div className="inspector__thread-title">Unresolved reference</div>
          <div className="inspector__thread-meta">Ch.01 → Ch.04 · 21 days open</div>
          <div className="inspector__thread-body">
            Referenced but not fully explored — resolve before the arc closes.
          </div>
        </div>
        <div className="inspector__thread-card">
          <div className="inspector__thread-title">Origin not established</div>
          <div className="inspector__thread-meta">set up in Ch.02, not yet paid off</div>
          <div className="inspector__thread-body">Plant this earlier?</div>
        </div>
      </section>

      {/* Appearances */}
      <section className="inspector__section">
        <div className="inspector__section-header">
          <div className="inspector__section-title">
            Appears in
            <span className="inspector__section-count">{doc.backlinks.length}</span>
          </div>
          <button className="ld-icon-btn" type="button">
            <Plus size={10} color="var(--ink-4)" strokeWidth={1.4} />
          </button>
        </div>
        {doc.backlinks.map((bl) => (
          <button className="inspector__appearance" key={bl} type="button">
            <div className="inspector__appearance-title">{bl}</div>
            <div className="inspector__appearance-meta">referenced</div>
          </button>
        ))}
        {doc.backlinks.length === 0 && (
          <p
            style={{
              padding: "0 14px 12px",
              fontSize: 12,
              color: "var(--ink-4)",
              margin: 0,
              fontFamily: "var(--font-sans)",
            }}
          >
            No backlinks yet.
          </p>
        )}
      </section>

      {/* Notes */}
      <section className="inspector__section">
        <div className="inspector__section-header">
          <div className="inspector__section-title">Notes</div>
          <button className="ld-icon-btn" type="button">
            <Plus size={10} color="var(--ink-4)" strokeWidth={1.4} />
          </button>
        </div>
        <div className="inspector__notes-card">
          Character notes and reminders go here.
          <div className="inspector__notes-date">edited Apr 28</div>
        </div>
      </section>
    </>
  );
}

function FieldsPane({
  doc,
  updateField,
}: {
  doc: ReturnType<typeof useEditorShellStore>["activeDocument"];
  updateField: (path: string, key: string, value: FrontmatterField["value"]) => void;
}) {
  return (
    <div className="inspector__fields">
      {doc.frontmatter.map((field) => (
        <div className="inspector__field-row" key={field.key}>
          <span className="inspector__field-key">{field.label}</span>
          <span className="inspector__field-val">
            {field.type === "select" && field.options ? (
              <select
                className="inspector__field-select"
                onChange={(e) => updateField(doc.path, field.key, e.target.value)}
                value={String(field.value)}
              >
                {field.options.map((o) => (
                  <option key={o} value={o}>
                    {o}
                  </option>
                ))}
              </select>
            ) : field.type === "tags" || field.type === "relations" ? (
              <div className="inspector-chip-list" style={{ padding: 0 }}>
                {(field.value as string[]).map((item) => (
                  <span className="entity-link entity-link--muted" key={item}>
                    {item}
                  </span>
                ))}
                <button className="inspector-chip-add" type="button">
                  + Add
                </button>
              </div>
            ) : (
              <input
                className="inspector__field-input"
                onChange={(e) =>
                  updateField(
                    doc.path,
                    field.key,
                    field.type === "number" ? Number(e.target.value) : e.target.value,
                  )
                }
                type={field.type === "number" ? "number" : "text"}
                value={String(field.value)}
              />
            )}
          </span>
        </div>
      ))}
      <button className="inspector__add-field" type="button">
        <Plus size={10} color="var(--ink-4)" strokeWidth={1.4} /> Add field
      </button>
    </div>
  );
}

function HistoryPane() {
  return (
    <div className="inspector__history">
      {HISTORY_MOCK.map((e, i) => (
        <div className="inspector__history-item" key={i}>
          <div>
            <div className={`inspector__history-dot${e.kind === "create" ? " created" : ""}`} />
            {i < HISTORY_MOCK.length - 1 && (
              <div
                style={{
                  position: "absolute",
                  left: 3,
                  top: 14,
                  bottom: -8,
                  width: 1,
                  background: "var(--divider)",
                  marginLeft: 3,
                }}
              />
            )}
          </div>
          <div>
            <div className="inspector__history-what">{e.what}</div>
            <div className="inspector__history-when">{e.when}</div>
          </div>
        </div>
      ))}
    </div>
  );
}
