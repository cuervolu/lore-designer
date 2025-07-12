<script setup lang="ts">
import {error as logError} from "@tauri-apps/plugin-log";
import {nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {defaultValueCtx, Editor, rootCtx, editorViewOptionsCtx} from '@milkdown/kit/core';
import {commonmark} from '@milkdown/kit/preset/commonmark';
import {gfm} from '@milkdown/kit/preset/gfm';
import {history} from '@milkdown/kit/plugin/history';
import {clipboard} from '@milkdown/kit/plugin/clipboard';
import {listener, listenerCtx} from '@milkdown/kit/plugin/listener';
import {replaceAll} from '@milkdown/kit/utils';
import '@milkdown/kit/prose/view/style/prosemirror.css';
import '@/assets/milkdown-custom-theme.css';

const props = defineProps<{
  modelValue: string; // For v-model
  placeholder?: string;
}>();

const emit = defineEmits(['update:modelValue', 'change', 'textUpdate']);

const editorContainer = ref<HTMLElement | null>(null);
const editor = ref<Editor | null>(null);
const internalValue = ref(props.modelValue);
const isUpdatingContent = ref(false);

function stripMarkdownSyntax(markdown: string): string {
  return markdown
  .replace(/^#+\s+/gm, '') // Remove headings
  .replace(/^[*-]\s+/gm, '') // Remove list markers
  .replace(/^>\s+/gm, '') // Remove blockquote markers
  .replace(/^\d+\.\s+/gm, '') // Remove ordered list markers
  .replace(/`{3}[\s\S]*?`{3}/g, '') // Remove code blocks
  .replace(/`([^`]+)`/g, '$1') // Remove inline code
  .replace(/\*\*([^*]+)\*\*/g, '$1') // Remove bold
  .replace(/\*([^*]+)\*/g, '$1') // Remove italic
  .replace(/\[([^\]]+)]\([^)]+\)/g, '$1') // Remove links
  .replace(/!\[([^\]]+)]\([^)]+\)/g, '') // Remove images
  .replace(/\[([^\]]+)]/g, '$1') // Remove reference-style links
  .replace(/~~([^~]+)~~/g, '$1') // Remove strikethrough
  .replace(/^-{3,}/gm, '') // Remove horizontal rules
  .trim();
}

onMounted(async () => {
  if (!editorContainer.value) return;

  editor.value = await Editor.make()
  .config((ctx) => {
    ctx.set(rootCtx, editorContainer.value);
    ctx.set(defaultValueCtx, props.modelValue);

    ctx.update(editorViewOptionsCtx, (prev) => ({
      ...prev,
      attributes: {
        ...prev.attributes,
        placeholder: props.placeholder || 'Start writing...',
      },
    }));

    const listenerManager = ctx.get(listenerCtx);
    listenerManager.markdownUpdated((_, markdown) => {
      if (isUpdatingContent.value) return;

      if (markdown !== internalValue.value) {
        internalValue.value = markdown;
        emit('update:modelValue', markdown);

        const plainText = stripMarkdownSyntax(markdown);
        emit('textUpdate', plainText);
        emit('change');
      }
    });
  })
  .use(commonmark)
  .use(gfm)
  .use(history)
  .use(clipboard)
  .use(listener)
  .create();
});

watch(() => props.modelValue, async (newValue) => {
  if (!editor.value || newValue === internalValue.value) return;

  try {
    isUpdatingContent.value = true;
    internalValue.value = newValue;
    editor.value.action(replaceAll(newValue));

    const plainText = stripMarkdownSyntax(newValue);
    emit('textUpdate', plainText);
  } catch (error) {
    await logError(`Error updating Milkdown content: ${error}`);
  } finally {
    await nextTick();
    isUpdatingContent.value = false;
  }
});

onBeforeUnmount(() => {
  editor.value?.destroy();
});
</script>

<template>
  <div ref="editorContainer" class="h-full w-full"></div>
</template>
