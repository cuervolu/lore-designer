import { createFileRoute, Outlet, useNavigate } from "@tanstack/react-router";
import { EditorShell } from "@features/editor-shell/EditorShell";

export const Route = createFileRoute("/workspace")({
  component: WorkspaceRoute,
});

function WorkspaceRoute() {
  const navigate = useNavigate();
  return (
    <>
      <EditorShell onExit={() => navigate({ to: "/" })} />
      <Outlet />
    </>
  );
}
