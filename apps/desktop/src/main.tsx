import React from 'react';
import ReactDOM from 'react-dom/client';
import { createMemoryHistory, createRouter, RouterProvider } from '@tanstack/react-router';
import '@fontsource-variable/lora/wght.css';
import '@lore/ui/globals.css';
import { AppProviders } from './AppProviders';
import { routeTree } from './routeTree.gen';

const memoryHistory = createMemoryHistory({ initialEntries: ['/'] });

const router = createRouter({
  routeTree,
  history: memoryHistory,
});

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  </React.StrictMode>,
);
