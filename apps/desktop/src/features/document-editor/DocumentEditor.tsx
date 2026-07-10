import { Fragment, type CSSProperties } from 'react';
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
        {!codex ? <span className="mention-bracket">[[</span> : null}
        <button
          className="mention-link"
          onClick={(event) => {
            event.stopPropagation();
            selectEntryByName(segment.text);
          }}
          type="button"
        >
          {segment.text}
        </button>
        {!codex ? <span className="mention-bracket">]]</span> : null}
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
      <div className="editor-scroll">
        <div className="no-entry">
          <h1>No entries yet.</h1>
          <p>This world is still blank — start writing to give it shape.</p>
        </div>
      </div>
    );
  }

  const codex = state.viewMode === 'codex';
  const textSize = { small: 16, medium: 17.5, large: 19.5 }[state.fontSize];

  return (
    <div className="editor-scroll">
      <article
        className={codex ? 'codex-document' : 'edit-document'}
        style={{ '--editor-font-size': `${textSize}px` } as CSSProperties}
      >
        <div className={codex ? 'codex-kicker' : 'entry-kicker'}>
          {codex ? `Codex · ${entry.type}` : `${entry.type} · ${entry.status}`}
        </div>
        <h1>{entry.title}</h1>
        {entry.paragraphs.length === 0 ? (
          <p className={codex ? 'codex-empty' : 'entry-empty'}>
            {codex ? 'Nothing to read yet.' : "This entry hasn't been written yet."}
          </p>
        ) : (
          entry.paragraphs.map((paragraph, index) => {
            const paragraphId = `${entry.id}-p${index}`;
            const focused = state.focusedParagraphId === paragraphId;
            const dimmed = state.focusModeEnabled && state.focusedParagraphId !== null && !focused;
            return (
              <p
                className={`${focused ? 'is-focused' : ''}${dimmed ? ' is-dimmed' : ''}`}
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
