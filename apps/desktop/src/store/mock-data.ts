import type { ProjectSummary, WorkspaceDataset, WorkspaceDatasetId } from '@/types/editor';

export const RECENT_PROJECTS: ProjectSummary[] = [
  {
    datasetId: 'default',
    entryCount: 47,
    id: 'shattered-coast',
    lastEdited: 'Edited 2 hours ago',
    name: 'The Shattered Coast',
  },
  {
    datasetId: 'default',
    entryCount: 112,
    id: 'ashfall-chronicles',
    lastEdited: 'Edited 3 days ago',
    name: 'Ashfall Chronicles',
  },
  {
    datasetId: 'default',
    entryCount: 9,
    id: 'vaelthorn',
    lastEdited: 'Edited 1 week ago',
    name: 'Vaelthorn',
    status: 'Draft',
  },
];

const DEFAULT_DATASET: WorkspaceDataset = {
  groupOrder: ['Characters', 'Factions', 'Locations', 'Timeline', 'Items'],
  entries: [
    {
      id: 'iskra-vane',
      title: 'Iskra Vane',
      type: 'Character',
      status: 'Active',
      group: 'Characters',
      tags: ['exile', 'diplomat', 'ex-privateer', 'coastal'],
      relationships: [
        { name: 'The Ember Concord', note: 'former target, now employer' },
        { name: 'Thornmere Hold', note: 'place of origin' },
        { name: 'Thala Ashgrove', note: 'former first mate' },
      ],
      paragraphs: [
        "Iskra Vane spent the first half of her life running contraband through the reefs south of Thornmere, and the second half trying to be forgiven for it. She was born to a net-mender in the Hold's lower quarter and left at sixteen aboard a ship she had no right to board, having talked her way onto the crew with a lie about her age and a truth about her nerve.",
        'For nine years she captained the Grey Tern under no flag but her own, moving salt, dye, and the occasional fugitive noble between the coastal states. She was good at it. She was better at knowing when to stop.',
        "When [[The Ember Concord]] offered full pardon in exchange for a season of testimony against her old trading partners, she took it — not out of loyalty to the Concord, but because [[Thornmere Hold]]'s new magistrate had started hanging smugglers younger than she'd been when she started.",
        'She now serves as an unofficial envoy between Thornmere Hold and the free ports, a job with no formal title and considerable informal power. Nobody trusts her completely. Everybody negotiates through her anyway.',
      ],
    },
    {
      id: 'thala-ashgrove',
      title: 'Thala Ashgrove',
      type: 'Character',
      status: 'Draft',
      group: 'Characters',
      tags: ['character'],
      relationships: [{ name: 'Iskra Vane', note: 'former captain' }],
      paragraphs: [],
    },
    {
      id: 'ember-concord',
      title: 'The Ember Concord',
      type: 'Faction',
      status: 'Active',
      group: 'Factions',
      tags: ['merchant guild', 'mages', 'coastal trade'],
      relationships: [
        { name: 'Thornmere Hold', note: 'de facto governs' },
        { name: 'Iskra Vane', note: 'envoy, formerly hunted' },
        { name: 'The Drowned Parliament', note: 'rival' },
      ],
      paragraphs: [
        'The Ember Concord began as a guild of dye-merchants and ended, within two generations, as the closest thing the coast has to a navy. It still calls itself a trading company.',
        'Membership requires capital, a demonstrated aptitude in at least one licensed discipline, and a willingness to be audited by Concord accountants who are, without exception, more feared than its enforcers.',
        'The Concord does not conquer territory. It buys the debt of whoever already governs it, then quietly rewrites the terms. [[Thornmere Hold]] has not been formally annexed. Thornmere Hold has also not set its own tariffs in eleven years.',
      ],
    },
    {
      id: 'drowned-parliament',
      title: 'The Drowned Parliament',
      type: 'Faction',
      status: 'Draft',
      group: 'Factions',
      tags: ['faction'],
      relationships: [{ name: 'The Ember Concord', note: 'rival' }],
      paragraphs: [],
    },
    {
      id: 'thornmere-hold',
      title: 'Thornmere Hold',
      type: 'Location',
      status: 'Capital',
      group: 'Locations',
      tags: ['fortress-city', 'port', 'capital'],
      relationships: [
        { name: 'The Ember Concord', note: 'economically bound to' },
        { name: 'Iskra Vane', note: 'born here' },
        { name: 'The Sundering of Thornmere', note: 'site of' },
      ],
      paragraphs: [
        'Thornmere Hold is built into the cliff it is named for, a fortress-city with three harbors and no farmland, entirely dependent on trade for its next meal. This has made it wealthy and, twice in its history, nearly made it starve.',
        "The Hold proper — the keep, the magistrate's court, the old shrine — sits atop the cliff, reachable only by the Salt Stair or a two-hour cart road inland. Everyone who matters lives up there. Everyone who works lives below.",
        'The city survived [[The Sundering of Thornmere]] less than a century ago, when a third of the lower cliff face collapsed into the sea during a single stormy night.',
      ],
    },
    {
      id: 'salt-stair',
      title: 'The Salt Stair',
      type: 'Location',
      status: 'Draft',
      group: 'Locations',
      tags: ['location'],
      relationships: [{ name: 'Thornmere Hold', note: 'part of' }],
      paragraphs: [],
    },
    {
      id: 'sundering-thornmere',
      title: 'The Sundering of Thornmere',
      type: 'Timeline Event',
      status: 'Draft',
      group: 'Timeline',
      tags: ['disaster', 'history'],
      relationships: [{ name: 'Thornmere Hold', note: 'location' }],
      paragraphs: [
        "Ninety-one years ago, roughly a third of Thornmere's lower cliff face gave way in a single night, taking the tanning district and half the fish market with it. Nobody has fully explained why.",
      ],
    },
    {
      id: 'ashbound-crown',
      title: 'The Ashbound Crown',
      type: 'Item',
      status: 'Draft',
      group: 'Items',
      tags: ['item'],
      relationships: [],
      paragraphs: [],
    },
  ],
};

const HOLLOW_DATASET: WorkspaceDataset = {
  groupOrder: ['Notes', 'Characters', 'Locations', 'Lore', 'Drafts'],
  entries: [
    {
      id: 'welcome',
      title: 'Welcome',
      type: 'Note',
      status: 'Start Here',
      group: 'Notes',
      tags: ['start here'],
      relationships: [],
      paragraphs: [
        "Welcome to The Hollow — not a document, but a place. Everything you write here becomes an entry: a character, a location, a fragment of history. Entries aren't filed away in isolation. They remember each other.",
        "Mention [[Wren of the Hollow]] in a draft, and that draft becomes something Wren's own page knows about. Write about a fire, and every entry that fire touched carries the mark of it.",
        "Open [[Wren of the Hollow]] to meet its first resident. Follow the thread to [[Saltreach]], and from there to the night it burned. Nothing here is finished — it's all still connected, still asking to be written further.",
      ],
    },
    {
      id: 'wren-hollow',
      title: 'Wren of the Hollow',
      type: 'Character',
      status: 'Active',
      group: 'Characters',
      tags: ['wanderer', 'oath-bound', 'open thread'],
      relationships: [
        { name: 'Saltreach', note: "left home, hasn't returned" },
        { name: 'The Burning of Saltreach', note: "survived it, won't discuss it" },
      ],
      paragraphs: [
        'Wren left [[Saltreach]] three years before the fire and has never said why. Everyone who remembers the leaving assumes guilt. Wren has never confirmed or denied it.',
        "What draws Wren back now is unclear — a debt, a dream, or simply the pull of a town that no longer has a name worth keeping. That answer hasn't been written yet.",
      ],
    },
    {
      id: 'saltreach',
      title: 'Saltreach',
      type: 'Location',
      status: 'Ruined',
      group: 'Locations',
      tags: ['coastal', 'abandoned', 'ash'],
      relationships: [
        { name: 'Wren of the Hollow', note: 'left, presumed connected to the fire' },
        { name: 'The Burning of Saltreach', note: 'site of' },
      ],
      paragraphs: [
        'Saltreach was a fishing town of maybe four hundred people, built where a freshwater spring met the tide flats. It had one road in and, after the fire, no reason for anyone to take it.',
        "What's left is mostly foundation stones and a bell that didn't melt. Nobody has resettled it. Nobody has explained why not.",
      ],
    },
    {
      id: 'burning-saltreach',
      title: 'The Burning of Saltreach',
      type: 'Lore Event',
      status: 'Unresolved',
      group: 'Lore',
      tags: ['fire', 'disaster', 'open question'],
      relationships: [
        { name: 'Saltreach', note: 'location' },
        { name: 'Wren of the Hollow', note: 'left before, returns after' },
      ],
      paragraphs: [
        'The fire took Saltreach in a single night, and — unlike most disasters this world remembers clearly — nobody agrees on how it started. Three accounts exist. All three blame something different.',
        "Wren was gone three years by then. That hasn't stopped anyone from asking.",
      ],
    },
    {
      id: 'chapter-01',
      title: 'Chapter 01',
      type: 'Draft',
      status: 'In Progress',
      group: 'Drafts',
      tags: ['draft', 'scene'],
      relationships: [{ name: 'Wren of the Hollow', note: 'mentioned' }],
      paragraphs: [
        "The road to [[Saltreach]] hadn't changed, which was the first thing [[Wren of the Hollow]] noticed and the last thing they said out loud.",
        '[[Wren of the Hollow]] kept walking anyway. Three years was long enough to forget the smell of a place, but not long enough to forget why you left it.',
      ],
    },
  ],
};

export const WORKSPACE_DATASETS: Record<WorkspaceDatasetId, WorkspaceDataset> = {
  blank: { entries: [], groupOrder: ['Characters', 'Factions', 'Locations', 'Timeline', 'Items'] },
  default: DEFAULT_DATASET,
  hollow: HOLLOW_DATASET,
};
