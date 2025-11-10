import {parse, stringify} from 'yaml';
import type {CharacterFormData} from "@editor/types/form.type.ts";

export function useMarkdownSerializer() {
  const parseMarkdown = (content: string): CharacterFormData => {
    const frontmatterMatch = content.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n([\s\S]*)$/);

    if (!frontmatterMatch || !frontmatterMatch[1]) {
      return {frontmatter: {}, content: content.trim()};
    }

    try {
      const frontmatter = parse(frontmatterMatch[1]) || {};
      const body = (frontmatterMatch[2] || '').trim();
      return {frontmatter, content: body};
    } catch (error) {
      console.error('YAML parse error:', error);
      return {frontmatter: {}, content: content.trim()};
    }
  };

  const serializeMarkdown = (data: CharacterFormData): string => {
    if (Object.keys(data.frontmatter).length === 0) {
      return data.content;
    }

    const yamlStr = stringify(data.frontmatter, {
      lineWidth: 0,
      defaultStringType: 'QUOTE_DOUBLE'
    });

    return `---\n${yamlStr}---\n${data.content}`;
  };

  return {parseMarkdown, serializeMarkdown};
}
