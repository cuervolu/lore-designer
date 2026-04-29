import { renderToStaticMarkup } from "react-dom/server";
import { expect, test } from "vite-plus/test";
import App from "./App";
import { mapWorkspaceToTreesData } from "@core/editor-shell/helpers";
import { createInitialEditorShellState } from "@core/editor-shell/mockData";

test("renders the workspace wizard as the app entry", () => {
  const html = renderToStaticMarkup(<App />);

  expect(html).toContain("Lore Designer");
  expect(html).toContain("Open a world and keep writing.");
  expect(html).toContain("Open Workspace");
  expect(html).toContain("Verdant Hollow");
});

test("maps workspace nodes to tree paths for directories and files", () => {
  const state = createInitialEditorShellState();
  const paths = mapWorkspaceToTreesData(state.workspaceNodes);

  expect(paths).toContain("Characters/");
  expect(paths).toContain("Characters/Wren of the Hollow.md");
  expect(paths).toContain("Locations/");
  expect(paths).toContain("README.md");
});
