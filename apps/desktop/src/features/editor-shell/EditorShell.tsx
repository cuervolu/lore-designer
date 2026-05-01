import { useState } from "react";
import {
  ChevronLeft,
  ChevronRight,
  Columns2,
  PanelLeft,
  PanelRight,
  Plus,
  Search,
} from "lucide-react";
import { useEditorShellStore } from "@/store/editor-shell";
import { DocumentEditor } from "@features/document-editor/DocumentEditor";
import { EditorTabs } from "@features/editor-tabs/EditorTabs";
import { LoreNavigator } from "@features/file-tree/LoreFileTree";
import { PropertiesInspector } from "@features/properties-inspector/PropertiesInspector";

interface EditorShellProps {
  onExit?: () => void;
}

export function EditorShell({ onExit }: EditorShellProps) {
  const { activeDocument } = useEditorShellStore();
  const [showNav, setShowNav] = useState(true);
  const [showIns, setShowIns] = useState(true);

  const navW = 240;
  const insW = 316;

  const columns = [showNav ? `${navW}px` : "0px", "1fr", showIns ? `${insW}px` : "0px"].join(" ");

  return (
    <div className="editor-root">
      {/* Toolbar */}
      <div className="editor-toolbar" style={{ gridTemplateColumns: columns }}>
        <div
          className="editor-toolbar__nav"
          style={{
            borderRight: showNav ? "1px solid var(--divider)" : "none",
            width: showNav ? navW : "auto",
          }}
        >
          <button className="ld-icon-btn" onClick={onExit} title="Back to projects" type="button">
            <ChevronLeft size={13} color="var(--ink-2)" strokeWidth={1.4} />
          </button>
          <button className="ld-icon-btn" title="Forward" type="button">
            <ChevronRight size={13} color="var(--ink-3)" strokeWidth={1.4} />
          </button>
          <span className="editor-toolbar__nav-sep" />
          <button className="ld-icon-btn" title="New entry" type="button">
            <Plus size={12} color="var(--ink-2)" strokeWidth={1.4} />
          </button>
        </div>

        <div className="editor-toolbar__center">
          <div className="editor-toolbar__search">
            <Search size={11} color="var(--ink-4)" strokeWidth={1.4} />
            <span style={{ flex: 1 }}>Go to anything…</span>
            <span className="editor-toolbar__search-kbd">⌘P</span>
          </div>
        </div>

        <div
          className="editor-toolbar__end"
          style={{
            borderLeft: showIns ? "1px solid var(--divider)" : "none",
            width: showIns ? insW : "auto",
          }}
        >
          <button
            className="ld-icon-btn"
            onClick={() => setShowNav((v) => !v)}
            title="Toggle navigator"
            type="button"
          >
            <PanelLeft size={13} color="var(--ink-3)" strokeWidth={1.4} />
          </button>
          <button
            className="ld-icon-btn"
            onClick={() => setShowIns((v) => !v)}
            title="Toggle inspector"
            type="button"
          >
            <PanelRight size={13} color="var(--ink-3)" strokeWidth={1.4} />
          </button>
        </div>
      </div>

      {/* Body */}
      <div className="editor-body" style={{ gridTemplateColumns: columns }}>
        {showNav && <LoreNavigator />}
        <div className="editor-center">
          <EditorTabs />
          <EditorSubtoolbar title={activeDocument.title} kind={activeDocument.kind} />
          <DocumentEditor />
          <div className="editor-statusbar">
            <span>
              {activeDocument.kind} · {activeDocument.title}
            </span>
            <span>1,284 words · last edit 2 min ago</span>
          </div>
        </div>
        {showIns && <PropertiesInspector />}
      </div>
    </div>
  );
}

function EditorSubtoolbar({ title, kind }: { title: string; kind: string }) {
  const { workspaceName } = useEditorShellStore();
  const [view, setView] = useState<"read" | "edit" | "outline">("read");

  return (
    <div className="editor-subtoolbar">
      <div className="editor-breadcrumbs">
        <span>{workspaceName}</span>
        <ChevronRight size={9} color="var(--faint)" strokeWidth={1.4} />
        <span style={{ textTransform: "capitalize" }}>{kind}s</span>
        <ChevronRight size={9} color="var(--faint)" strokeWidth={1.4} />
        <span className="editor-breadcrumbs__current">{title}</span>
      </div>
      <div className="editor-view-controls">
        {(["read", "edit", "outline"] as const).map((v) => (
          <button
            className={`editor-seg-btn${view === v ? " active" : ""}`}
            key={v}
            onClick={() => setView(v)}
            type="button"
          >
            {v.charAt(0).toUpperCase() + v.slice(1)}
          </button>
        ))}
        <span className="editor-view-sep" />
        <button className="ld-icon-btn" title="Find" type="button">
          <Search size={13} color="var(--ink-3)" strokeWidth={1.4} />
        </button>
        <button className="ld-icon-btn" title="Split view" type="button">
          <Columns2 size={13} color="var(--ink-3)" strokeWidth={1.4} />
        </button>
      </div>
    </div>
  );
}
