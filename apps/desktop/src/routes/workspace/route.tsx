import { SidebarProvider } from '@lore/ui/components/sidebar';
import { createFileRoute, Outlet, useNavigate } from '@tanstack/react-router';
import { useEffect, useState } from 'react';
import { PropertiesInspector } from '@features/properties-inspector/PropertiesInspector';
import { WorkspaceCommandPalette } from '@features/workspace-shell/WorkspaceCommandPalette';
import { WorkspaceEditorPane } from '@features/workspace-shell/WorkspaceEditorPane';
import { WorkspaceSidebar } from '@features/workspace-shell/WorkspaceSidebar';
import { WorkspaceTopbar } from '@features/workspace-shell/WorkspaceTopbar';

export const Route = createFileRoute('/workspace')({
  component: WorkspaceRoute,
});

function WorkspaceRoute() {
  const navigate = useNavigate();
  const [showSidebar, setShowSidebar] = useState(true);
  const [showInspector, setShowInspector] = useState(true);
  const [commandOpen, setCommandOpen] = useState(false);

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (!(event.metaKey || event.ctrlKey) || event.key.toLowerCase() !== 'p') {
        return;
      }

      event.preventDefault();
      setCommandOpen(true);
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <>
      <div className="workspace-route-shell">
        <SidebarProvider
          className="workspace-route"
          defaultOpen
          open={showSidebar}
          onOpenChange={setShowSidebar}
          style={{
            gridTemplateColumns: showSidebar ? '15rem minmax(0, 1fr)' : 'minmax(0, 1fr)',
          }}
        >
          {showSidebar && <WorkspaceSidebar />}
          <div className="workspace-route__main">
            <WorkspaceTopbar
              onExit={() => navigate({ to: '/' })}
              onOpenCommandPalette={() => setCommandOpen(true)}
              onToggleSidebar={() => setShowSidebar((value) => !value)}
              onToggleInspector={() => setShowInspector((value) => !value)}
            />
            <div
              className="workspace-route__body"
              style={{
                gridTemplateColumns: showInspector ? 'minmax(0, 1fr) 316px' : 'minmax(0, 1fr)',
              }}
            >
              <WorkspaceEditorPane />
              {showInspector && <PropertiesInspector />}
            </div>
            <Outlet />
          </div>
        </SidebarProvider>
      </div>
      <WorkspaceCommandPalette onOpenChange={setCommandOpen} open={commandOpen} />
    </>
  );
}
