import React from "react";
import ReactDOM from "react-dom/client";
import { createMemoryHistory, createRouter, RouterProvider } from "@tanstack/react-router";
import "@fontsource-variable/ibm-plex-sans/wght.css";
import "@fontsource/ibm-plex-mono/400.css";
import "@fontsource/ibm-plex-mono/500.css";
import "@fontsource-variable/source-serif-4/wght.css";
import "@lore/ui/globals.css";
import { routeTree } from "./routeTree.gen";

const memoryHistory = createMemoryHistory({ initialEntries: ["/"] });

const router = createRouter({
  routeTree,
  history: memoryHistory,
});

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
);
