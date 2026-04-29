import type { DocumentRecord, EditorShellState, WorkspaceNode } from "./types";

const workspaceNodes: WorkspaceNode[] = [
  {
    children: [
      {
        icon: "🪽",
        id: "character-wren",
        kind: "file",
        name: "Wren of the Hollow",
        path: "Characters/Wren of the Hollow.md",
        typeTag: "character",
      },
      {
        icon: "🗡️",
        id: "character-calder",
        kind: "file",
        name: "Calder Vex",
        path: "Characters/Calder Vex.md",
        typeTag: "character",
      },
      {
        icon: "🫐",
        id: "character-mira",
        kind: "file",
        name: "Mira Quist",
        path: "Characters/Mira Quist.md",
        typeTag: "character",
      },
      {
        icon: "🔥",
        id: "character-keeper",
        kind: "file",
        name: "The Keeper",
        path: "Characters/The Keeper.md",
        typeTag: "character",
      },
    ],
    icon: "👥",
    id: "dir-characters",
    kind: "directory",
    name: "Characters",
    path: "Characters",
  },
  {
    children: [
      {
        icon: "⚓",
        id: "location-saltreach",
        kind: "file",
        name: "Saltreach",
        path: "Locations/Saltreach.md",
        typeTag: "location",
      },
      {
        icon: "🌲",
        id: "location-blackpine",
        kind: "file",
        name: "Blackpine Wood",
        path: "Locations/Blackpine Wood.md",
        typeTag: "location",
      },
      {
        icon: "🏰",
        id: "location-ash-tower",
        kind: "file",
        name: "The Ash Tower",
        path: "Locations/The Ash Tower.md",
        typeTag: "location",
      },
    ],
    icon: "🗺️",
    id: "dir-locations",
    kind: "directory",
    name: "Locations",
    path: "Locations",
  },
  {
    children: [
      {
        icon: "⚔️",
        id: "faction-grey-covenant",
        kind: "file",
        name: "The Grey Covenant",
        path: "Factions/The Grey Covenant.md",
        typeTag: "faction",
      },
    ],
    icon: "⚔️",
    id: "dir-factions",
    kind: "directory",
    name: "Factions",
    path: "Factions",
  },
  {
    children: [
      {
        icon: "📜",
        id: "lore-burning",
        kind: "file",
        name: "The Burning of Saltreach",
        path: "Lore/The Burning of Saltreach.md",
        typeTag: "lore",
      },
    ],
    icon: "📚",
    id: "dir-lore",
    kind: "directory",
    name: "Lore",
    path: "Lore",
  },
  {
    children: [
      {
        icon: "📄",
        id: "draft-chapter-01",
        kind: "file",
        name: "Chapter 01",
        path: "Drafts/Chapter 01.md",
        typeTag: "draft",
      },
      {
        icon: "📄",
        id: "draft-outline",
        kind: "file",
        name: "Outline",
        path: "Drafts/Outline.md",
        typeTag: "draft",
      },
    ],
    icon: "📝",
    id: "dir-drafts",
    kind: "directory",
    name: "Drafts",
    path: "Drafts",
  },
  {
    icon: "📕",
    id: "file-readme",
    kind: "file",
    name: "README",
    path: "README.md",
    typeTag: "system",
  },
];

const documents: Record<string, DocumentRecord> = {
  "Characters/Wren of the Hollow.md": {
    backlinks: ["The Burning of Saltreach", "Calder Vex", "Saltreach"],
    content: [
      {
        id: "wren-intro-1",
        segments: [
          {
            text: "She was born with a name no one used. The villagers called her Wren for the bird, for her smallness, for how she always seemed about to take flight.",
          },
        ],
        type: "paragraph",
      },
      {
        id: "wren-intro-2",
        segments: [
          { text: "On the morning the " },
          { text: "Burning of Saltreach", tone: "lore" },
          {
            text: " began, she was at the harbour with a basket of letters bound for the keeper. The smoke arrived before the news did.",
          },
        ],
        type: "paragraph",
      },
      { id: "wren-bearing", text: "Manner & bearing", type: "heading" },
      {
        id: "wren-bearing-1",
        segments: [
          {
            text: "Wren keeps her own counsel. She speaks quickly when she speaks at all, and only when there is something exact to be said. Strangers mistake this for shyness.",
          },
        ],
        type: "paragraph",
      },
      {
        icon: "🪽",
        id: "wren-callout",
        segments: [
          {
            text: "She is bound to a raven called Notch. The bond is not magical, exactly, but the raven knows things it should not.",
          },
        ],
        type: "callout",
      },
      { id: "wren-known", text: "Known whereabouts", type: "heading" },
      {
        id: "wren-bullets",
        items: [
          [
            { text: "Last seen at " },
            { text: "Saltreach", tone: "location" },
            { text: ", the morning of the fires." },
          ],
          [
            { text: "Believed to have travelled north through " },
            { text: "Blackpine Wood", tone: "location" },
            { text: "." },
          ],
          [
            { text: "One letter, unsigned, arrived at the " },
            { text: "Ash Tower", tone: "location" },
            { text: " three weeks later." },
          ],
        ],
        type: "bullets",
      },
      { id: "wren-open-threads", text: "Open threads", type: "heading" },
      {
        id: "wren-open-threads-1",
        items: [
          [{ text: "Who taught her to read the old script?" }],
          [{ text: "What did the raven witness in the keeper's chamber?" }],
          [{ text: "Why did she leave Verdant Hollow with only one satchel?" }],
        ],
        type: "bullets",
      },
    ],
    cover: {
      accent: "#f6b98d",
      gradient:
        "linear-gradient(115deg, rgba(156,77,49,0.92) 0%, rgba(68,32,49,0.9) 52%, rgba(13,32,63,0.92) 100%)",
    },
    frontmatter: [
      { key: "name", label: "Name", type: "text", value: "Wren of the Hollow" },
      {
        key: "status",
        label: "Status",
        options: ["alive", "missing", "dead"],
        type: "select",
        value: "alive",
      },
      { key: "role", label: "Role", type: "text", value: "Messenger" },
      { key: "faction", label: "Faction", type: "text", value: "The Grey Covenant" },
      { key: "age", label: "Age", type: "number", value: 24 },
      { key: "birthplace", label: "Birthplace", type: "text", value: "Verdant Hollow" },
      { key: "firstAppearance", label: "First appearance", type: "text", value: "Chapter 01" },
      {
        key: "tags",
        label: "Tags",
        type: "tags",
        value: ["protagonist", "raven-bound", "outsider"],
      },
      {
        key: "relationships",
        label: "Relationships",
        type: "relations",
        value: ["Calder Vex", "Mira Quist"],
      },
    ],
    icon: "🪽",
    kind: "character",
    path: "Characters/Wren of the Hollow.md",
    status: "unsaved",
    title: "Wren of the Hollow",
  },
  "Characters/Calder Vex.md": {
    backlinks: ["Wren of the Hollow"],
    content: [
      {
        id: "calder-p-1",
        segments: [{ text: "Calder keeps accounts for the covenant and remembers every debt." }],
        type: "paragraph",
      },
    ],
    frontmatter: [
      { key: "name", label: "Name", type: "text", value: "Calder Vex" },
      {
        key: "status",
        label: "Status",
        options: ["alive", "missing", "dead"],
        type: "select",
        value: "alive",
      },
      { key: "role", label: "Role", type: "text", value: "Quartermaster" },
    ],
    icon: "🗡️",
    kind: "character",
    path: "Characters/Calder Vex.md",
    status: "saved",
    title: "Calder Vex",
  },
  "Characters/Mira Quist.md": {
    backlinks: ["Wren of the Hollow"],
    content: [
      {
        id: "mira-p-1",
        segments: [
          { text: "Mira keeps the weather ledgers and can read the tide like scripture." },
        ],
        type: "paragraph",
      },
    ],
    frontmatter: [
      { key: "name", label: "Name", type: "text", value: "Mira Quist" },
      {
        key: "status",
        label: "Status",
        options: ["alive", "missing", "dead"],
        type: "select",
        value: "alive",
      },
      { key: "role", label: "Role", type: "text", value: "Navigator" },
    ],
    icon: "🫐",
    kind: "character",
    path: "Characters/Mira Quist.md",
    status: "saved",
    title: "Mira Quist",
  },
  "Characters/The Keeper.md": {
    backlinks: [],
    content: [
      {
        id: "keeper-p-1",
        segments: [{ text: "The keeper is older than the tower records admit." }],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "The Keeper" }],
    icon: "🔥",
    kind: "character",
    path: "Characters/The Keeper.md",
    status: "saved",
    title: "The Keeper",
  },
  "Locations/Saltreach.md": {
    backlinks: ["Wren of the Hollow"],
    content: [
      {
        id: "saltreach-p-1",
        segments: [
          {
            text: "Saltreach is a harbour town built where the cliffs finally stop resisting the sea.",
          },
        ],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "Saltreach" }],
    icon: "⚓",
    kind: "location",
    path: "Locations/Saltreach.md",
    status: "saved",
    title: "Saltreach",
  },
  "Locations/Blackpine Wood.md": {
    backlinks: [],
    content: [
      {
        id: "blackpine-p-1",
        segments: [{ text: "Blackpine is less a forest than an agreement between crows and fog." }],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "Blackpine Wood" }],
    icon: "🌲",
    kind: "location",
    path: "Locations/Blackpine Wood.md",
    status: "saved",
    title: "Blackpine Wood",
  },
  "Locations/The Ash Tower.md": {
    backlinks: [],
    content: [
      {
        id: "ash-p-1",
        segments: [{ text: "Ash still falls inside the tower even when the sky above is clear." }],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "The Ash Tower" }],
    icon: "🏰",
    kind: "location",
    path: "Locations/The Ash Tower.md",
    status: "saved",
    title: "The Ash Tower",
  },
  "Factions/The Grey Covenant.md": {
    backlinks: ["Wren of the Hollow"],
    content: [
      {
        id: "faction-p-1",
        segments: [
          { text: "A courier faction that acts more like a weather vane than a government." },
        ],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "The Grey Covenant" }],
    icon: "⚔️",
    kind: "faction",
    path: "Factions/The Grey Covenant.md",
    status: "saved",
    title: "The Grey Covenant",
  },
  "Lore/The Burning of Saltreach.md": {
    backlinks: ["Wren of the Hollow"],
    content: [
      {
        id: "burning-p-1",
        segments: [
          { text: "The fire began in the rope market and crossed the harbour before dawn bells." },
        ],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "The Burning of Saltreach" }],
    icon: "📜",
    kind: "lore",
    path: "Lore/The Burning of Saltreach.md",
    status: "saved",
    title: "The Burning of Saltreach",
  },
  "Drafts/Chapter 01.md": {
    backlinks: [],
    content: [
      {
        id: "chapter-1-p-1",
        segments: [{ text: "The chapter opens with gulls landing where ash should not be." }],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "Chapter 01" }],
    icon: "📄",
    kind: "draft",
    path: "Drafts/Chapter 01.md",
    status: "saved",
    title: "Chapter 01",
  },
  "Drafts/Outline.md": {
    backlinks: [],
    content: [
      {
        id: "outline-p-1",
        segments: [{ text: "Act I: smoke, letters, and the missing tide log." }],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "Outline" }],
    icon: "📄",
    kind: "draft",
    path: "Drafts/Outline.md",
    status: "saved",
    title: "Outline",
  },
  "README.md": {
    backlinks: [],
    content: [
      {
        id: "readme-p-1",
        segments: [
          { text: "Verdant Hollow is a workspace mock used to validate the editor shell." },
        ],
        type: "paragraph",
      },
    ],
    frontmatter: [{ key: "name", label: "Name", type: "text", value: "README" }],
    icon: "📕",
    kind: "system",
    path: "README.md",
    status: "saved",
    title: "README",
  },
};

export function createInitialEditorShellState(): EditorShellState {
  return {
    activePath: "Characters/Wren of the Hollow.md",
    documents,
    expandedPaths: ["Characters", "Locations", "Drafts"],
    searchQuery: "",
    tabs: [
      {
        dirty: false,
        path: "Characters/Wren of the Hollow.md",
        pinned: true,
        title: "Wren of the Hollow",
      },
      { dirty: false, path: "Lore/The Burning of Saltreach.md", title: "The Burning of Saltreach" },
      { dirty: false, path: "Locations/Saltreach.md", title: "Saltreach" },
      { dirty: true, path: "Drafts/Chapter 01.md", title: "Chapter 01" },
    ],
    workspaceName: "Verdant Hollow",
    workspaceNodes,
  };
}
