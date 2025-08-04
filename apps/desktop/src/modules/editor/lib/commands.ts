import { $command } from '@milkdown/kit/utils';
import { TextSelection } from '@milkdown/kit/prose/state';
import { entityLinkSchema } from './entity-link';

export type InsertEntityLinkPayload = {
  href: string;
  entityType: 'character' | 'location' | 'lore' | 'unknown' | 'command';
  label: string;
};

// This regex must match the one used in the SlashProvider to correctly identify and replace the trigger text.
const TRIGGER_REGEX = /(?:^|\s)([\/@#!])(\w*)$/;

export const insertEntityLinkCommand = $command(
  'InsertEntityLink',
  (ctx) => (payload: InsertEntityLinkPayload) => {
    return (state, dispatch) => {
      const { tr, schema } = state;
      const { from } = state.selection;

      // Find the start of the trigger sequence (e.g., '@arin').
      const textBefore = state.doc.textBetween(Math.max(0, from - 20), from, '\n');
      const match = TRIGGER_REGEX.exec(textBefore);

      if (!match) return false;

      const [fullMatch, trigger, query] = match;
      const matchStart = from - fullMatch.trimStart().length;
      const matchEnd = from;

      const mark = entityLinkSchema.type(ctx).create({
        href: payload.href,
        entityType: payload.entityType,
        label: payload.label,
      });

      // Replace the trigger text (e.g., '@arin') with the full label (e.g., 'Arin el Valiente')
      // and apply our custom mark to it.
      tr.replaceWith(matchStart, matchEnd, schema.text(payload.label, [mark]));

      // Set the cursor to be right after the newly inserted link.
      const newPos = matchStart + payload.label.length;
      tr.setSelection(TextSelection.create(tr.doc, newPos));

      if (dispatch) {
        dispatch(tr.scrollIntoView());
      }

      return true;
    };
  },
);
