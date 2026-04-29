import {
  createContext,
  startTransition,
  use,
  useCallback,
  useMemo,
  useState,
  type PropsWithChildren,
} from "react";
import { mapWorkspaceToTreesData } from "./helpers";
import { createInitialEditorShellState } from "./mockData";
import type { DocumentRecord, EditorShellState, FrontmatterField, OpenTab } from "./types";

interface EditorShellStoreValue extends EditorShellState {
  activeDocument: DocumentRecord;
  openDocument: (path: string) => void;
  setSearchQuery: (value: string) => void;
  treePaths: string[];
  updateFrontmatterField: (
    path: string,
    fieldKey: string,
    value: FrontmatterField["value"],
  ) => void;
}

const EditorShellContext = createContext<EditorShellStoreValue | null>(null);

function upsertTab(tabs: OpenTab[], nextTab: OpenTab) {
  if (tabs.some((tab) => tab.path === nextTab.path)) {
    return tabs;
  }

  return [...tabs, nextTab];
}

export function EditorShellProvider({ children }: PropsWithChildren) {
  const [state, setState] = useState(createInitialEditorShellState);

  const openDocument = useCallback((path: string) => {
    setState((current) => {
      const document = current.documents[path];
      if (document == null) {
        return current;
      }

      return {
        ...current,
        activePath: path,
        tabs: upsertTab(current.tabs, {
          dirty: document.status === "unsaved",
          path: document.path,
          pinned: false,
          title: document.title,
        }),
      };
    });
  }, []);

  const setSearchQuery = useCallback((value: string) => {
    startTransition(() => {
      setState((current) => ({ ...current, searchQuery: value }));
    });
  }, []);

  const updateFrontmatterField = useCallback(
    (path: string, fieldKey: string, value: FrontmatterField["value"]) => {
      setState((current) => {
        const document = current.documents[path];
        if (document == null) {
          return current;
        }

        return {
          ...current,
          documents: {
            ...current.documents,
            [path]: {
              ...document,
              frontmatter: document.frontmatter.map((field) =>
                field.key === fieldKey ? { ...field, value } : field,
              ),
              status: "unsaved",
            },
          },
          tabs: current.tabs.map((tab) => (tab.path === path ? { ...tab, dirty: true } : tab)),
        };
      });
    },
    [],
  );

  const treePaths = useMemo(
    () => mapWorkspaceToTreesData(state.workspaceNodes),
    [state.workspaceNodes],
  );
  const activeDocument = state.documents[state.activePath];

  const value = useMemo<EditorShellStoreValue>(
    () => ({
      ...state,
      activeDocument,
      openDocument,
      setSearchQuery,
      treePaths,
      updateFrontmatterField,
    }),
    [activeDocument, openDocument, setSearchQuery, state, treePaths, updateFrontmatterField],
  );

  return <EditorShellContext value={value}>{children}</EditorShellContext>;
}

export function useEditorShellStore() {
  const context = use(EditorShellContext);

  if (context == null) {
    throw new Error("useEditorShellStore must be used inside EditorShellProvider");
  }

  return context;
}
