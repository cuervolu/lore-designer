import { useState } from "react";
import { EditorShellProvider } from "@core/editor-shell/store";
import { WorkspaceWizard } from "./features/workspace-wizard/WorkspaceWizard";
import { EditorShell } from "./widgets/editor-shell/EditorShell";

function App() {
  const [view, setView] = useState<"editor" | "wizard">("wizard");

  return (
    <div className="dark lore-designer-app">
      {view === "wizard" ? (
        <WorkspaceWizard onOpenWorkspace={() => setView("editor")} />
      ) : (
        <EditorShellProvider>
          <EditorShell />
        </EditorShellProvider>
      )}
    </div>
  );
}

export default App;
