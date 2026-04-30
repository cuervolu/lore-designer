import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { WorkspaceWizard } from "@features/workspace-wizard/WorkspaceWizard";

export const Route = createFileRoute("/")({
  component: IndexRoute,
});

function IndexRoute() {
  const navigate = useNavigate();
  return <WorkspaceWizard onOpenWorkspace={() => navigate({ to: "/workspace" })} />;
}
