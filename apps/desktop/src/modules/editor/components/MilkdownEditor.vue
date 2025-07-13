<script setup lang="ts">
import { error as logError } from "@tauri-apps/plugin-log";
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { defaultValueCtx, Editor, editorViewOptionsCtx, rootCtx } from '@milkdown/kit/core';
import { commonmark } from '@milkdown/kit/preset/commonmark';
import { gfm } from '@milkdown/kit/preset/gfm';
import { history } from '@milkdown/kit/plugin/history';
import { clipboard } from '@milkdown/kit/plugin/clipboard';
import { listener, listenerCtx } from '@milkdown/kit/plugin/listener';
import { slashFactory, SlashProvider } from '@milkdown/kit/plugin/slash';
import { replaceAll } from '@milkdown/kit/utils';

import CommandMenu, { type CommandMenuItem } from './CommandMenu.vue';
import { useEditorStore } from '@editor/stores/editor.store';

import { Code, Heading1, Heading2, Heading3, List, ListOrdered, MapPin, Quote, User, BookOpen } from 'lucide-vue-next';

// Styles
import '@milkdown/kit/prose/view/style/prosemirror.css';
import '@/assets/milkdown-custom-theme.css';

const props = defineProps<{
  modelValue: string;
  placeholder?: string;
}>();
const emit = defineEmits(['update:modelValue', 'change', 'textUpdate']);

const editorContainer = ref<HTMLElement | null>(null);
const slashContainerRef = ref<HTMLElement | null>(null); // For positioning the slash menu
const editor = ref<Editor | null>(null);
const internalValue = ref(props.modelValue);
const isUpdatingContent = ref(false);
const editorStore = useEditorStore();

const slash = slashFactory('lore-commands');
const slashState = ref<{
  open: boolean;
  trigger: string | null;
  query: string | null;
}>({
  open: false,
  trigger: null,
  query: null,
});

const defaultCommands: Omit<CommandMenuItem, 'action'>[] = [
  { id: 'heading1', type: 'command', label: 'Heading 1', icon: Heading1 },
  { id: 'heading2', type: 'command', label: 'Heading 2', icon: Heading2 },
  { id: 'heading3', type: 'command', label: 'Heading 3', icon: Heading3 },
  { id: 'bulletList', type: 'command', label: 'Bullet List', icon: List },
  { id: 'orderedList', type: 'command', label: 'Ordered List', icon: ListOrdered },
  { id: 'blockquote', type: 'command', label: 'Blockquote', icon: Quote },
  { id: 'codeBlock', type: 'command', label: 'Code Block', icon: Code },
];

const commandItems = computed<CommandMenuItem[]>(() => {
  const trigger = slashState.value.trigger;
  const query = slashState.value.query?.toLowerCase() || '';

  let source: Omit<CommandMenuItem, 'action'>[] = [];

  if (trigger === '@') {
    // TODO: Replace with real data from editorStore.
    source = [
      { id: 'char1', type: 'character', label: 'Arin el Valiente', icon: User },
      { id: 'char2', type: 'character', label: 'La Hechicera Umbra', icon: User },
    ];
  } else if (trigger === '#') {
    // TODO: Replace with real data.
    source = [
      { id: 'loc1', type: 'location', label: 'Aldea Inicial', icon: MapPin },
      { id: 'loc2', type: 'location', label: 'El Bosque Tenebroso', icon: MapPin },
    ];
  } else if (trigger === '/') {
    source = defaultCommands;
  }

  return source
  .filter(item => item.label.toLowerCase().includes(query))
  .map(item => ({
    ...item,
    action: () => {
      console.log(`Selected: ${item.label} (ID: ${item.id})`);
      slashState.value.open = false;
    },
  }));
});

onMounted(async () => {
  if (!editorContainer.value || !slashContainerRef.value) return;

  const slashProvider = new SlashProvider({
    content: slashContainerRef.value,
    shouldShow: (view) => {
      const { selection } = view.state;
      if (!selection.empty) {
        slashState.value.open = false;
        return false;
      }

      const textBefore = slashProvider.getContent(view);
      if (!textBefore) {
        slashState.value.open = false;
        return false;
      }

      const match = /(?:^|\s)([\/\@\#])(\w*)$/.exec(textBefore);

      if (match) {
        const [, trigger, query] = match;
        slashState.value = { open: true, trigger, query };
        return true;
      }

      slashState.value.open = false;
      return false;
    },
  });

  editor.value = await Editor.make()
  .config((ctx) => {
    ctx.set(rootCtx, editorContainer.value);
    ctx.set(defaultValueCtx, props.modelValue);

    ctx.update(editorViewOptionsCtx, (prev) => ({
      ...prev,
      attributes: {
        ...(prev?.attributes || {}),
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

    ctx.set(slash.key, {
      view: () => ({
        update: (view, prevState) => slashProvider.update(view, prevState),
        destroy: () => slashProvider.destroy(),
      }),
    });
  })
  .use(commonmark)
  .use(gfm)
  .use(history)
  .use(clipboard)
  .use(listener)
  .use(slash)
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
    await logError(`Error updating Milkdown content: ${error as string}`);
  } finally {
    await nextTick();
    isUpdatingContent.value = false;
  }
});

onBeforeUnmount(() => {
  editor.value?.destroy();
});

function stripMarkdownSyntax(markdown: string): string {
  return markdown
  .replace(/^#+\s+/gm, '')
  .replace(/^[*-]\s+/gm, '')
  .replace(/^>\s+/gm, '')
  .replace(/^\d+\.\s+/gm, '')
  .replace(/`{3}[\s\S]*?`{3}/g, '')
  .replace(/`([^`]+)`/g, '$1')
  .replace(/\*\*([^*]+)\*\*/g, '$1')
  .replace(/\*([^*]+)\*/g, '$1')
  .replace(/\[([^\]]+)]\([^)]+\)/g, '$1')
  .replace(/!\[([^\]]+)]\([^)]+\)/g, '')
  .replace(/\[([^\]]+)]/g, '$1')
  .replace(/~~([^~]+)~~/g, '$1')
  .replace(/^-{3,}/gm, '')
  .trim();
}
</script>

<template>
  <!-- This wrapper div solves both issues:
    1. It provides a single root element, fixing the Vue warning.
    2. `position: relative` creates a positioning context for the slash menu.
  -->
  <div class="relative h-full w-full">
    <div ref="editorContainer" class="h-full w-full" />

    <!--
      This container is positioned absolutely within the relative parent.
      SlashProvider will control its `top` and `left` properties.
      It will also toggle the `data-show` attribute.
    -->
    <div ref="slashContainerRef" class="absolute" data-show="false">
      <CommandMenu
        v-if="slashState.open"
        :items="commandItems"
      />
    </div>
  </div>
</template>

<style>
/*
  This CSS hides the slash container when the provider determines
  it should not be visible, using the `data-show` attribute.
*/
.slash-provider-container[data-show='false'] {
  display: none;
}
</style>
