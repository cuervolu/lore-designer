import { createFileRoute, Outlet, useNavigate } from '@tanstack/react-router';
import { PropertiesInspector } from '@features/properties-inspector/PropertiesInspector';
import { WorkspaceEditorPane } from '@features/workspace-shell/WorkspaceEditorPane';
import { WorkspaceSidebar } from '@features/workspace-shell/WorkspaceSidebar';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';
import { ChevronLeft } from 'lucide-react';

export const Route = createFileRoute('/workspace')({ component: WorkspaceRoute });

function WorkspaceRoute() {
  const navigate = useNavigate();
  const state = useEditorShellStore();
  const activeEntry = getActiveEntry(state);

  return (
    <main className="flex h-full w-full overflow-hidden">
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
          className="flex h-full w-[18px] shrink-0 cursor-pointer items-center justify-center border-l border-border bg-toolbar text-faint hover:text-primary"
          onClick={() => state.setMetadataOpen(true)}
          type="button"
        >
          <ChevronLeft size={11} strokeWidth={1.6} />
        </button>
      ) : null}
      <Outlet />
    </main>
  );
}
