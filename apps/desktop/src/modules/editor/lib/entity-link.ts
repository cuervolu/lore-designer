import type { Mark } from '@milkdown/kit/prose/model';
import { $markSchema } from '@milkdown/kit/utils';

export const entityLinkSchema = $markSchema('entityLink', () => ({
  attrs: {
    href: { default: '' },
    entityType: { default: 'unknown' },
    label: { default: '' },
  },
  parseDOM: [
    {
      tag: 'span[data-entity-type]',
      getAttrs: (dom) => {
        const element = dom as HTMLElement;
        return {
          href: element.getAttribute('data-href') || '',
          entityType: element.getAttribute('data-entity-type') || 'unknown',
          label: element.innerText,
        };
      },
    },
  ],
  toDOM: (mark: Mark) => {
    const { href, entityType, label } = mark.attrs;
    return [
      'span',
      {
        class: `entity-link entity-type--${entityType}`,
        'data-href': href,
        'data-entity-type': entityType,
        'data-label': label,
        title: `${entityType}: ${href}`
      },
      0,
    ];
  },
  // This part is required for the schema to be valid.
  // It tells Milkdown how to handle this mark when converting to/from Markdown.
  toMarkdown: {
    match: (mark) => mark.type.name === 'entityLink',
    runner: (state, mark) => {
      const { href, entityType, label } = mark.attrs;
      // We serialize to a custom syntax to preserve our data.
      state.write(`@[${label || state.stack.pop()?.value || ''}](${href}){entityType=${entityType}}`);
    },
  },
  parseMarkdown: {
    // For now, we only parse standard markdown links into this mark.
    // A full parser for the custom syntax would require a remark plugin.
    match: (node) => node.type === 'link',
    runner: (state, node, markType) => {
      state.openMark(markType, { href: node.url, title: node.title });
      state.next(node.children);
      state.closeMark(markType);
    },
  },
}));
