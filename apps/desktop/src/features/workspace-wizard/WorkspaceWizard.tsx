import { useState } from 'react';
import { Star } from 'lucide-react';
import logo from '@assets/logo.webp';
import { useAppVersion } from '@/api/app';
import { RecentPane } from './components/RecentPane';
import { NewProjectPane } from './components/NewProjectPane';
import { OpenExistingPane } from './components/OpenExistingPane';

const RECENTS = [
  {
    name: 'Saltreach Cycle',
    path: '~/Documents/Lore/Saltreach Cycle',
    opened: 'Today, 9:14',
    words: 38400,
    items: { characters: 5, locations: 5, factions: 3, lore: 4, drafts: 4 },
    threads: 3,
  },
  {
    name: 'The Quiet Atlas',
    path: '~/Writing/The Quiet Atlas',
    opened: 'Yesterday',
    words: 112680,
    items: { characters: 22, locations: 41, factions: 7, lore: 18, drafts: 36 },
    threads: 11,
  },
  {
    name: 'Glassbound (game bible)',
    path: '~/Projects/Glassbound/lore',
    opened: 'Apr 24',
    words: 21500,
    items: { characters: 14, locations: 9, factions: 5, lore: 22, drafts: 8 },
    threads: 6,
  },
  {
    name: 'Ironvigil — short story',
    path: '~/Documents/Drafts/Ironvigil',
    opened: 'Apr 12',
    words: 8200,
    items: { characters: 4, locations: 2, factions: 0, lore: 1, drafts: 3 },
    threads: 1,
  },
  {
    name: 'Notes — Northern Cycle',
    path: '~/Documents/Lore/Northern',
    opened: 'Mar 30',
    words: 4900,
    items: { characters: 2, locations: 6, factions: 2, lore: 7, drafts: 1 },
    threads: 0,
  },
];

export type RecentProject = (typeof RECENTS)[number];

const PINNED = [
  { name: 'Saltreach Cycle', meta: 'Wren · Ch.01 — Smoke' },
  { name: 'The Quiet Atlas', meta: "Atlas — Article 'Erath'" },
];

type WizardTab = 'recent' | 'new' | 'open';

interface WorkspaceWizardProps {
  onOpenWorkspace: () => void;
}

export function WorkspaceWizard({ onOpenWorkspace }: WorkspaceWizardProps) {
  const [tab, setTab] = useState<WizardTab>('recent');
  const appVersion = useAppVersion();

  return (
    <div className="wiz-root ld-fade">
      {/* Left rail */}
      <aside className="wiz-rail">
        <div className="wiz-rail__header">
          <img alt="Lore Designer" className="wiz-rail__logo" src={logo} />
          <div>
            <div className="wiz-rail__name">Lore Designer</div>
            <div className="wiz-rail__version">{appVersion ?? '…'}</div>
          </div>
        </div>

        <div className="wiz-rail__section-label">Start</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 1, marginBottom: 14 }}>
          <button
            className={`wiz-rail-tab${tab === 'recent' ? ' active' : ''}`}
            onClick={() => setTab('recent')}
            type="button"
          >
            <span>Recent projects</span>
            <span className="wiz-rail-tab__count">{RECENTS.length}</span>
          </button>
          <button
            className={`wiz-rail-tab${tab === 'new' ? ' active' : ''}`}
            onClick={() => setTab('new')}
            type="button"
          >
            New project
          </button>
          <button
            className={`wiz-rail-tab${tab === 'open' ? ' active' : ''}`}
            onClick={() => setTab('open')}
            type="button"
          >
            Open from disk…
          </button>
        </div>

        <div className="wiz-rail__section-label">Pinned</div>
        {PINNED.map((p) => (
          <button className="wiz-pinned-item" key={p.name} onClick={onOpenWorkspace} type="button">
            <Star size={11} color="var(--ink-4)" strokeWidth={1.4} />
            <div>
              <div className="wiz-pinned-item__name">{p.name}</div>
              <div className="wiz-pinned-item__meta">{p.meta}</div>
            </div>
          </button>
        ))}

        <div style={{ flex: 1 }} />
      </aside>

      {/* Main content */}
      <main className="wiz-main">
        {tab === 'recent' && (
          <RecentPane onNew={() => setTab('new')} onOpen={onOpenWorkspace} recents={RECENTS} />
        )}
        {tab === 'new' && <NewProjectPane onOpen={onOpenWorkspace} />}
        {tab === 'open' && <OpenExistingPane onOpen={onOpenWorkspace} />}
      </main>
    </div>
  );
}
