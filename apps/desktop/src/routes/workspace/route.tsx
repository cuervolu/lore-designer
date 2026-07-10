import { createFileRoute, Outlet, useNavigate } from '@tanstack/react-router';
import { PropertiesInspector } from '@features/properties-inspector/PropertiesInspector';
import { WorkspaceEditorPane } from '@features/workspace-shell/WorkspaceEditorPane';
import { WorkspaceSidebar } from '@features/workspace-shell/WorkspaceSidebar';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';

export const Route = createFileRoute('/workspace')({ component: WorkspaceRoute });

function WorkspaceRoute() {
  const navigate = useNavigate();
  const state = useEditorShellStore();
  const activeEntry = getActiveEntry(state);

  return (
    <main className="workspace-shell">
      <WorkspaceSidebar
        onOpenSettings={() => {
          state.setSettingsReturnPath('/workspace');
          void navigate({ to: '/settings' });
        }}
        onSwitchProject={() => void navigate({ to: '/' })}
      />
      <WorkspaceEditorPane />
      {activeEntry && state.viewMode === 'edit' && state.metadataOpen ? (
        <PropertiesInspector />
      ) : null}
      {activeEntry && state.viewMode === 'edit' && !state.metadataOpen ? (
        <button
          aria-label="Show metadata"
          className="metadata-tab"
          onClick={() => state.setMetadataOpen(true)}
          type="button"
        >
          ‹
        </button>
      ) : null}
      <Outlet />
    </main>
  );
}
