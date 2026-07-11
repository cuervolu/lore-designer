import { Bold, Image, Italic, Link, Minus, PanelRight, Underline } from 'lucide-react';
import { Button, SegmentedControl, SegmentedControlItem } from '@lore/ui';
import { DocumentEditor } from '@features/document-editor/DocumentEditor';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';
import type { EditorViewMode } from '@/types/editor';

export function WorkspaceEditorPane() {
  const state = useEditorShellStore();
  const entry = getActiveEntry(state);

  return (
    <section className="relative flex h-full flex-1 flex-col bg-background transition-colors">
      {entry ? (
        <div className="absolute top-[26px] right-9 z-2 flex items-center gap-2.5">
          <span className="text-[11px] text-faint italic">
            {state.focusModeEnabled ? 'click a paragraph to focus' : 'focus mode is off'}
          </span>
          <SegmentedControl
            aria-label="Editor view"
            onValueChange={(values) => {
              const next = values[0] as EditorViewMode | undefined;
              if (next) state.setViewMode(next);
            }}
            size="compact"
            value={[state.viewMode]}
          >
            <SegmentedControlItem value="edit">Edit</SegmentedControlItem>
            <SegmentedControlItem value="codex">Codex</SegmentedControlItem>
          </SegmentedControl>
        </div>
      ) : null}
      <DocumentEditor />
      {entry && state.viewMode === 'edit' ? (
        <div
          className="flex h-[46px] shrink-0 items-center gap-[3px] border-t border-border bg-toolbar px-[18px] transition-colors"
          aria-label="Formatting toolbar"
        >
          <Button aria-label="Bold" size="icon-sm" variant="ghost">
            <Bold size={15} strokeWidth={1.6} />
          </Button>
          <Button aria-label="Italic" size="icon-sm" variant="ghost">
            <Italic size={15} strokeWidth={1.6} />
          </Button>
          <Button aria-label="Underline" size="icon-sm" variant="ghost">
            <Underline size={15} strokeWidth={1.6} />
          </Button>
          <span className="mx-1.5 h-4 w-px bg-dash" />
          <Button aria-label="Insert link" size="icon-sm" variant="ghost">
            <Link size={15} strokeWidth={1.6} />
          </Button>
          <Button aria-label="Insert image" size="icon-sm" variant="ghost">
            <Image size={15} strokeWidth={1.6} />
          </Button>
          <Button aria-label="Insert divider" size="icon-sm" variant="ghost">
            <Minus size={15} strokeWidth={1.6} />
          </Button>
          <span className="flex-1" />
          <Button
            aria-label={state.metadataOpen ? 'Hide metadata' : 'Show metadata'}
            onClick={() => state.setMetadataOpen(!state.metadataOpen)}
            size="icon-sm"
            variant="ghost"
          >
            <PanelRight size={15} strokeWidth={1.6} />
          </Button>
        </div>
      ) : null}
    </section>
  );
}
