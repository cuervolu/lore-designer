import { AlertTriangle, Copy, ExternalLink, Monitor, MoreHorizontal, Trash2 } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import type { Workspace } from '../types';

type CardAction = 'copy' | 'open' | 'remove' | 'reveal';

interface WorkspaceCardProps {
  onAction: (action: CardAction, workspace: Workspace) => void;
  onOpen: (workspace: Workspace) => void;
  workspace: Workspace;
}

export function WorkspaceCard({ onAction, onOpen, workspace }: WorkspaceCardProps) {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!menuOpen) return;
    const onDoc = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', onDoc);
    return () => document.removeEventListener('mousedown', onDoc);
  }, [menuOpen]);

  return (
    <div
      className={`ws-card${!workspace.exists ? ' ws-card--missing' : ''}`}
      onClick={() => workspace.exists && onOpen(workspace)}
    >
      <div
        className="ws-cover"
        style={{
          background: `linear-gradient(135deg, ${workspace.cover.from}, ${workspace.cover.to})`,
        }}
      >
        <div className="ws-cover__grain" />
        <div className="ws-cover__glyph">{workspace.cover.glyph}</div>
        <button
          aria-label="Workspace options"
          className="ws-cover__menu-btn"
          onClick={(e) => {
            e.stopPropagation();
            setMenuOpen((v) => !v);
          }}
          type="button"
        >
          <MoreHorizontal size={14} />
        </button>
        {menuOpen && (
          <div
            className="ws-popover"
            onClick={(e) => e.stopPropagation()}
            ref={menuRef}
            style={{ top: 40, right: 6 }}
          >
            <button
              className="ws-popover__item"
              onClick={() => {
                onAction('open', workspace);
                setMenuOpen(false);
              }}
              type="button"
            >
              <ExternalLink size={14} /> Open in Editor
            </button>
            <button
              className="ws-popover__item"
              onClick={() => {
                onAction('reveal', workspace);
                setMenuOpen(false);
              }}
              type="button"
            >
              <Monitor size={14} /> Show in File Manager
            </button>
            <button
              className="ws-popover__item"
              onClick={() => {
                onAction('copy', workspace);
                setMenuOpen(false);
              }}
              type="button"
            >
              <Copy size={14} /> Copy Path
            </button>
            <div className="ws-popover__divider" />
            <button
              className="ws-popover__item ws-popover__item--danger"
              onClick={() => {
                onAction('remove', workspace);
                setMenuOpen(false);
              }}
              type="button"
            >
              <Trash2 size={14} /> Remove from List
            </button>
          </div>
        )}
      </div>

      <div className="ws-body">
        <div className="ws-name">{workspace.name}</div>
        <div className="ws-path" title={workspace.path}>
          {workspace.path}
        </div>
        <div className="ws-meta">
          {workspace.exists ? (
            <>
              <span>{workspace.counts.characters} characters</span>
              <span>·</span>
              <span>{workspace.counts.locations} locations</span>
              <span>·</span>
              <span>Last opened {workspace.lastOpened}</span>
            </>
          ) : (
            <>
              <span className="ws-badge">
                <AlertTriangle size={10} /> Not Found
              </span>
              <span style={{ marginLeft: 'auto' }}>Last opened {workspace.lastOpened}</span>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
