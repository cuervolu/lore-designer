import { $remark } from '@milkdown/kit/utils';
import { visit } from 'unist-util-visit';
import type { Node, Parent } from 'unist';

interface TextNode extends Node {
  type: 'text';
  value: string;
}

export const entityLinkRemarkPlugin = $remark('entityLinkRemark', () => {
  return (tree: Node) => {
    visit(tree, 'text', (node: TextNode, index: number | null, parent: Parent | null) => {
      if (!parent || index === null) return;

      const text = node.value;
      const regex = /\[\[([^|\]]+)(?:\|([^\]]+))?]]/g;
      let match;
      let lastIndex = 0;
      const newNodes: Node[] = [];

      while ((match = regex.exec(text)) !== null) {
        const [fullMatch, href, label] = match;
        const beforeText = text.slice(lastIndex, match.index);

        if (beforeText) {
          newNodes.push({
            type: 'text',
            value: beforeText
          } as TextNode);
        }

        newNodes.push({
          type: 'entityLink',
          data: {
            href,
            entityType: inferEntityType(href),
            label: label || getFileNameFromPath(href)
          },
          children: [{
            type: 'text',
            value: label || getFileNameFromPath(href)
          } as TextNode]
        } as Node);

        lastIndex = regex.lastIndex;
      }

      const remainingText = text.slice(lastIndex);
      if (remainingText) {
        newNodes.push({
          type: 'text',
          value: remainingText
        } as TextNode);
      }

      if (newNodes.length > 0) {
        parent.children.splice(index, 1, ...newNodes);
      }
    });
  };
});

function inferEntityType(href: string): string {
  if (href.includes('Characters/') || href.endsWith('.character.md')) return 'character';
  if (href.includes('Locations/') || href.endsWith('.location.md')) return 'location';
  if (href.includes('Lore/') || href.endsWith('.lore.md')) return 'lore';
  return 'unknown';
}

function getFileNameFromPath(path: string): string {
  return path.split('/').pop()?.replace(/\.[^.]+$/, '') || path;
}
