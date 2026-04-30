import { createFileRoute, Outlet } from "@tanstack/react-router";
import { EditorShell } from "@features/editor-shell/EditorShell";

export const Route = createFileRoute("/workspace")({
  component: WorkspaceRoute,
});

function WorkspaceRoute() {
  return (
    <>
      <EditorShell />
      <Outlet />
    </>
  );
}
