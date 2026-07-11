import { Fragment, type CSSProperties } from 'react';
import { cn } from '@lore/ui/lib/utils';
import { getActiveEntry, useEditorShellStore } from '@/store/editor-shell';

const MENTION_PATTERN = /\[\[([^\]]+)\]\]/g;

export function splitMentions(text: string) {
  const segments: Array<{ mention: boolean; text: string }> = [];
  let cursor = 0;
  for (const match of text.matchAll(MENTION_PATTERN)) {
    const index = match.index ?? 0;
    if (index > cursor) segments.push({ mention: false, text: text.slice(cursor, index) });
    segments.push({ mention: true, text: match[1] });
    cursor = index + match[0].length;
  }
  if (cursor < text.length) segments.push({ mention: false, text: text.slice(cursor) });
  return segments;
}

function ParagraphText({ codex, text }: { codex: boolean; text: string }) {
  const selectEntryByName = useEditorShellStore((state) => state.selectEntryByName);
  return splitMentions(text).map((segment, index) =>
    segment.mention ? (
      <Fragment key={`${segment.text}-${index}`}>
        {!codex ? <span className="text-faint opacity-55">[[</span> : null}
        <button
          className="cursor-pointer text-primary underline decoration-primary/45 underline-offset-[3px]"
          onClick={(event) => {
            event.stopPropagation();
            selectEntryByName(segment.text);
          }}
          type="button"
        >
          {segment.text}
        </button>
        {!codex ? <span className="text-faint opacity-55">]]</span> : null}
      </Fragment>
    ) : (
      <Fragment key={index}>{segment.text}</Fragment>
    ),
  );
}

export function DocumentEditor() {
  const state = useEditorShellStore();
  const entry = getActiveEntry(state);

  if (!entry) {
    return (
      <div className="min-h-0 flex-1 overflow-y-auto">
        <div className="mx-auto max-w-[420px] px-6 pt-[30vh] pb-[60px] text-center">
          <h1 className="mb-2.5 text-2xl font-semibold">No entries yet.</h1>
          <p className="text-[15px] leading-[1.6] text-muted-foreground italic">
            This world is still blank — start writing to give it shape.
          </p>
        </div>
      </div>
    );
  }

  const codex = state.viewMode === 'codex';
  const baseFontSize = { small: 16, medium: 17.5, large: 19.5 }[state.fontSize];
  const fontSize = codex ? baseFontSize + 1.5 : baseFontSize;

  return (
    <div className="min-h-0 flex-1 overflow-y-auto">
      <article
        className={
          codex
            ? 'mx-auto max-w-[600px] px-10 pt-24 pb-[100px]'
            : 'mx-auto max-w-[640px] px-10 pt-[88px] pb-[60px]'
        }
        style={{ '--editor-font-size': `${fontSize}px` } as CSSProperties}
      >
        <div
          className={
            codex
              ? 'mb-2 text-center text-[13px] text-faint italic'
              : 'mb-3.5 text-[11px] font-semibold tracking-[0.14em] text-primary uppercase'
          }
        >
          {codex ? `Codex · ${entry.type}` : `${entry.type} · ${entry.status}`}
        </div>
        <h1
          className={
            codex
              ? 'mb-12 text-center text-[46px] leading-[1.2] font-semibold'
              : 'mb-[34px] text-[42px] leading-[1.15] font-semibold'
          }
        >
          {entry.title}
        </h1>
        {entry.paragraphs.length === 0 ? (
          <p
            className={
              codex
                ? 'text-center text-faint italic'
                : 'border-t border-dash py-[18px] leading-[1.7] text-faint italic'
            }
          >
            {codex ? 'Nothing to read yet.' : "This entry hasn't been written yet."}
          </p>
        ) : (
          entry.paragraphs.map((paragraph, index) => {
            const paragraphId = `${entry.id}-p${index}`;
            const focused = !codex && state.focusedParagraphId === paragraphId;
            const dimmed =
              !codex && state.focusModeEnabled && state.focusedParagraphId !== null && !focused;
            return (
              <p
                className={cn(
                  'text-[length:var(--editor-font-size)] text-body transition-[opacity,background-color] duration-[350ms]',
                  codex
                    ? 'mb-[26px] leading-[1.9]'
                    : cn(
                        'mb-6 rounded-md py-2.5 leading-[1.85]',
                        state.focusModeEnabled ? 'cursor-pointer' : 'cursor-default',
                        focused && '-mx-3.5 bg-accent px-3.5',
                        dimmed && 'opacity-30',
                      ),
                )}
                key={paragraphId}
                onClick={
                  codex || !state.focusModeEnabled
                    ? undefined
                    : () => state.setFocusedParagraphId(focused ? null : paragraphId)
                }
              >
                <ParagraphText codex={codex} text={paragraph} />
              </p>
            );
          })
        )}
      </article>
    </div>
  );
}
