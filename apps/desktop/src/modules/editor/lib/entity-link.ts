import type { Mark } from '@milkdown/kit/prose/model';
import { $markSchema } from '@milkdown/kit/utils';

interface EntityLinkData {
  href: string;
  entityType: string;
  label: string;
}

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
  toMarkdown: {
    match: (mark) => mark.type.name === 'entityLink',
    runner: (state, mark) => {
      const { href, label } = mark.attrs;

      if (label && label !== href) {
        state.addNode('text', undefined, `[[${href}|${label}]]`);
      } else {
        state.addNode('text', undefined, `[[${href}]]`);
      }
    },
  },
  parseMarkdown: {
    match: (node) => node.type === 'entityLink',
    runner: (state, node, markType) => {
      const { href, entityType, label } = (node.data as EntityLinkData) || {
        href: '',
        entityType: 'unknown',
        label: ''
      };

      state.openMark(markType, { href, entityType, label });
      state.next(node.children);
      state.closeMark(markType);
    },
  },
}));
