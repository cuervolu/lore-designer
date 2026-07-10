import { createRootRoute, Outlet } from '@tanstack/react-router';

export const Route = createRootRoute({ component: RootLayout });

function RootLayout() {
  return (
    <div className="lore-designer-app">
      <Outlet />
    </div>
  );
}
