import { useState } from "react";
import { NewWorkspaceForm } from "./components/NewWorkspaceForm";
import { WorkspaceList } from "./components/WorkspaceList";
import type { Workspace } from "./types";

const INITIAL_WORKSPACES: Workspace[] = [
  {
    id: "ashen",
    name: "The Ashen Coast",
    path: "~/Worlds/the-ashen-coast",
    lastOpened: "2 hours ago",
    cover: { from: "oklch(0.42 0.15 35)", to: "oklch(0.30 0.10 280)", glyph: "AC" },
    exists: true,
    counts: { characters: 24, locations: 11, lore: 47 },
  },
  {
    id: "verdant",
    name: "Verdant Hollow",
    path: "~/Documents/Worlds/verdant-hollow",
    lastOpened: "Yesterday",
    cover: { from: "oklch(0.38 0.10 150)", to: "oklch(0.55 0.12 195)", glyph: "VH" },
    exists: true,
    counts: { characters: 18, locations: 22, lore: 19 },
  },
  {
    id: "third-tide",
    name: "Third Tide Chronicles",
    path: "/Users/wren/Projects/third-tide",
    lastOpened: "3 days ago",
    cover: { from: "oklch(0.32 0.09 245)", to: "oklch(0.45 0.13 200)", glyph: "TT" },
    exists: true,
    counts: { characters: 9, locations: 6, lore: 14 },
  },
  {
    id: "iron",
    name: "Iron & Vow",
    path: "~/Worlds/iron-and-vow",
    lastOpened: "Last week",
    cover: { from: "oklch(0.30 0.04 30)", to: "oklch(0.42 0.10 60)", glyph: "I&V" },
    exists: true,
    counts: { characters: 31, locations: 14, lore: 28 },
  },
  {
    id: "wisteria",
    name: "Wisteria Court",
    path: "~/old-drafts/wisteria",
    lastOpened: "3 weeks ago",
    cover: { from: "oklch(0.40 0.12 320)", to: "oklch(0.30 0.08 280)", glyph: "WC" },
    exists: false,
    counts: { characters: 7, locations: 4, lore: 9 },
  },
  {
    id: "sandbox",
    name: "Sandbox",
    path: "~/Worlds/sandbox",
    lastOpened: "A month ago",
    cover: { from: "oklch(0.40 0.05 260)", to: "oklch(0.30 0.04 220)", glyph: "SB" },
    exists: true,
    counts: { characters: 3, locations: 2, lore: 5 },
  },
];

type WizardView = "list" | "new";

interface WorkspaceWizardProps {
  onOpenWorkspace: () => void;
}

export function WorkspaceWizard({ onOpenWorkspace }: WorkspaceWizardProps) {
  const [view, setView] = useState<WizardView>("list");
  const [workspaces, setWorkspaces] = useState<Workspace[]>(INITIAL_WORKSPACES);

  const handleCreate = ({
    name,
    openAfter,
    path,
  }: {
    name: string;
    openAfter: boolean;
    path: string;
  }) => {
    const glyph =
      name
        .split(/\s+/)
        .map((w) => w[0])
        .slice(0, 2)
        .join("")
        .toUpperCase() || "·";

    const newWs: Workspace = {
      id: `ws-${Date.now()}`,
      name,
      path,
      lastOpened: "Just now",
      cover: { from: "oklch(0.40 0.10 150)", to: "oklch(0.30 0.08 195)", glyph },
      exists: true,
      counts: { characters: 0, locations: 0, lore: 0 },
    };
    setWorkspaces((ws) => [newWs, ...ws]);
    if (openAfter) onOpenWorkspace();
    else setView("list");
  };

  return (
    <div className="wiz">
      {view === "list" ? (
        <WorkspaceList
          onNew={() => setView("new")}
          onOpen={onOpenWorkspace}
          onOpenFolder={onOpenWorkspace}
          onRemove={(ws) => setWorkspaces((list) => list.filter((w) => w.id !== ws.id))}
          workspaces={workspaces}
        />
      ) : (
        <NewWorkspaceForm onBack={() => setView("list")} onCreate={handleCreate} />
      )}
    </div>
  );
}
