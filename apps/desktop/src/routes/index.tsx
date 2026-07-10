import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { WorkspaceWizard } from '@features/workspace-wizard/WorkspaceWizard';
import { useEditorShellStore } from '@/store/editor-shell';

export const Route = createFileRoute('/')({ component: IndexRoute });

function IndexRoute() {
  const navigate = useNavigate();
  const { openBlankProject, openExampleProject, openProject, setSettingsReturnPath } =
    useEditorShellStore();

  const enterWorkspace = () => void navigate({ to: '/workspace' });

  return (
    <WorkspaceWizard
      onOpenExample={() => {
        openExampleProject();
        enterWorkspace();
      }}
      onOpenProject={(project) => {
        openProject(project);
        enterWorkspace();
      }}
      onOpenSettings={() => {
        setSettingsReturnPath('/');
        void navigate({ to: '/settings' });
      }}
      onWorkspaceCreated={(name) => {
        openBlankProject(name);
        enterWorkspace();
      }}
    />
  );
}
