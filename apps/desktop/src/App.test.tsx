import { expect, test } from "vite-plus/test";
import { mapWorkspaceToTreesData } from "@/store/editor-shell-helpers";
import { createInitialEditorShellState } from "@/store/mock-data";

test("maps workspace nodes to tree paths for directories and files", () => {
  const state = createInitialEditorShellState();
  const paths = mapWorkspaceToTreesData(state.workspaceNodes);

  expect(paths).toContain("Characters/");
  expect(paths).toContain("Characters/Wren of the Hollow.md");
  expect(paths).toContain("Locations/");
  expect(paths).toContain("README.md");
});
