<script setup lang="ts">
import {error as logError} from '@tauri-apps/plugin-log';
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
import { Crepe } from '@milkdown/crepe';
import { replaceAll } from '@milkdown/kit/utils';

import '@milkdown/crepe/theme/common/style.css';
import "@/assets/milkdown-custom-theme.css";

const props = defineProps<{
  modelValue: string; // For v-model
  placeholder?: string;
}>();

const emit = defineEmits(['update:modelValue', 'change', 'textUpdate']);

const editorContainer = ref<HTMLElement | null>(null);
const crepe = ref<Crepe | null>(null);
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

  crepe.value = new Crepe({
    root: editorContainer.value,
    defaultValue: props.modelValue,
    featureConfigs: {
      [Crepe.Feature.Placeholder]: {
        text: props.placeholder || 'Start writing...',
        mode: 'block',
      },
      [Crepe.Feature.Table]: {
      },
    },
  });

  await crepe.value.create();

  // Set up listeners
  crepe.value.on((listener) => {
    listener.markdownUpdated((ctx, markdown) => {
      if (isUpdatingContent.value) return;

      if (markdown !== internalValue.value) {
        internalValue.value = markdown;
        emit('update:modelValue', markdown);

        const plainText = stripMarkdownSyntax(markdown);
        emit('textUpdate', plainText);
        emit('change');
      }
    });
  });
});

watch(() => props.modelValue, async (newValue) => {
  if (!crepe.value || newValue === internalValue.value) return;

  try {
    isUpdatingContent.value = true;
    internalValue.value = newValue;
    crepe.value.editor.action(replaceAll(newValue));

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
  crepe.value?.destroy();
});
</script>

<template>
  <div ref="editorContainer" class="h-full w-full"></div>
</template>
