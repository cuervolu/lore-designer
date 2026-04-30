import React from "react";
import ReactDOM from "react-dom/client";
import { createMemoryHistory, createRouter, RouterProvider } from "@tanstack/react-router";
import "@fontsource-variable/inter/wght.css";
import "@fontsource-variable/fira-code/wght.css";
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
