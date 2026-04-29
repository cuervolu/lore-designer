import { useEditorShellStore } from "@core/editor-shell/store";
import { DocumentEditor } from "@features/document-editor/DocumentEditor";
import { EditorTabs } from "@features/editor-tabs/EditorTabs";
import { LoreFileTree } from "@features/file-tree/LoreFileTree";
import { PropertiesInspector } from "@features/properties-inspector/PropertiesInspector";

export function EditorShell() {
  const { activeDocument, workspaceName } = useEditorShellStore();

  return (
    <div className="editor-frame">
      <div className="editor-layout">
        <LoreFileTree />

        <div className="editor-main">
          <EditorTabs />
          <DocumentEditor />
          <footer className="editor-statusbar">
            <span>{workspaceName}</span>
            <span className="text-muted-foreground">main</span>
            <span className="text-muted-foreground">{activeDocument.kind}</span>
            <span className="ml-auto text-muted-foreground">source</span>
            <span className="text-muted-foreground">UTF-8</span>
            <span className="text-muted-foreground">LF</span>
            <span className="text-primary">{activeDocument.status ?? "saved"}</span>
          </footer>
        </div>

        <PropertiesInspector />
      </div>
    </div>
  );
}
