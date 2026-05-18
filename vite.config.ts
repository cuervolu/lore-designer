import { defineConfig } from "vite-plus";

export default defineConfig({
  lint: {
    options: { typeAware: true, typeCheck: true },
    ignorePatterns: [
      "**/dist/**",
      "**/node_modules/**",
      "**/target/**",
      "**/.git/**",
      "apps/desktop/routeTree.gen.ts",
      "apps/desktop/src-tauri/**",
    ],
  },
  staged: {
    "*": "vp check --fix",
  },
});
