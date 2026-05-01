import { useEffect, useMemo } from 'react';
import { createPortal } from 'react-dom';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
  CommandShortcut,
} from '@lore/ui/components/command';
import { BookOpen, Compass, FileText, Flag, User } from 'lucide-react';
import { useEditorShellStore } from '@/store/editor-shell';
import type { DocumentRecord } from '@/types/editor';

interface WorkspaceCommandPaletteProps {
  onOpenChange: (open: boolean) => void;
  open: boolean;
}

const KIND_LABEL: Record<string, string> = {
  character: 'Character',
  draft: 'Draft',
  faction: 'Faction',
  location: 'Location',
  lore: 'Lore',
};

const KIND_ICON = {
  character: User,
  draft: FileText,
  faction: Flag,
  location: Compass,
  lore: BookOpen,
} as const;

export function WorkspaceCommandPalette({ onOpenChange, open }: WorkspaceCommandPaletteProps) {
  const { activePath, documents, openDocument, tabs } = useEditorShellStore();

  const allDocuments = useMemo<DocumentRecord[]>(() => Object.values(documents), [documents]);
  const openTabs = useMemo<DocumentRecord[]>(
    () =>
      tabs
        .map((tab) => documents[tab.path])
        .filter((document): document is DocumentRecord => document != null),
    [documents, tabs],
  );
  const workspaceFiles = useMemo(
    () => allDocuments.filter((document) => !tabs.some((tab) => tab.path === document.path)),
    [allDocuments, tabs],
  );

  const handleSelect = (path: string) => {
    openDocument(path);
    onOpenChange(false);
  };

  useEffect(() => {
    if (!open) {
      return;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        event.preventDefault();
        onOpenChange(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onOpenChange, open]);

  if (!open || typeof document === 'undefined') {
    return null;
  }

  return createPortal(
    <div
      aria-hidden={false}
      className="workspace-command-palette"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onOpenChange(false);
        }
      }}
    >
      <div
        aria-label="Go to anything"
        aria-modal="false"
        className="workspace-command__dialog"
        onMouseDown={(event) => event.stopPropagation()}
        role="dialog"
      >
        <Command className="workspace-command">
          <CommandInput
            autoFocus
            className="workspace-command__input"
            placeholder="Go to anything…"
          />
          <CommandList className="workspace-command__list">
            <CommandEmpty>No matching documents.</CommandEmpty>

            {openTabs.length > 0 ? (
              <CommandGroup className="workspace-command__group" heading="Open tabs">
                {openTabs.map((document) => {
                  const Icon = KIND_ICON[document.kind as keyof typeof KIND_ICON] ?? FileText;
                  const kindLabel = KIND_LABEL[document.kind] ?? 'Entry';
                  const isActive = document.path === activePath;

                  return (
                    <CommandItem
                      className="workspace-command__item"
                      key={document.path}
                      keywords={[document.kind, document.path]}
                      onSelect={() => handleSelect(document.path)}
                      value={`${document.title} ${document.path}`}
                    >
                      <Icon className="workspace-command__item-icon" />
                      <span className="workspace-command__item-copy">
                        <span className="workspace-command__item-title">{document.title}</span>
                        <span className="workspace-command__item-meta">{document.path}</span>
                      </span>
                      <CommandShortcut className="workspace-command__shortcut">
                        {isActive ? 'Current' : kindLabel}
                      </CommandShortcut>
                    </CommandItem>
                  );
                })}
              </CommandGroup>
            ) : null}

            {openTabs.length > 0 && workspaceFiles.length > 0 ? (
              <CommandSeparator className="workspace-command__separator" />
            ) : null}

            {workspaceFiles.length > 0 ? (
              <CommandGroup className="workspace-command__group" heading="Workspace files">
                {workspaceFiles.map((document) => {
                  const Icon = KIND_ICON[document.kind as keyof typeof KIND_ICON] ?? FileText;
                  const kindLabel = KIND_LABEL[document.kind] ?? 'Entry';

                  return (
                    <CommandItem
                      className="workspace-command__item"
                      key={document.path}
                      keywords={[document.kind, document.path]}
                      onSelect={() => handleSelect(document.path)}
                      value={`${document.title} ${document.path}`}
                    >
                      <Icon className="workspace-command__item-icon" />
                      <span className="workspace-command__item-copy">
                        <span className="workspace-command__item-title">{document.title}</span>
                        <span className="workspace-command__item-meta">{document.path}</span>
                      </span>
                      <CommandShortcut className="workspace-command__shortcut">
                        {kindLabel}
                      </CommandShortcut>
                    </CommandItem>
                  );
                })}
              </CommandGroup>
            ) : null}
          </CommandList>
        </Command>
      </div>
    </div>,
    document.body,
  );
}
