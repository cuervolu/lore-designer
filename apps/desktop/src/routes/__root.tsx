import { createRootRoute, Outlet } from '@tanstack/react-router';

export const Route = createRootRoute({ component: RootLayout });

function RootLayout() {
  return (
    <div className="h-full min-h-0 overflow-hidden bg-background text-foreground transition-colors">
      <Outlet />
    </div>
  );
}
