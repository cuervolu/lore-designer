<script setup lang="ts">
import {ref, watch, onBeforeUnmount, type Ref} from 'vue';
import {useEditor, EditorContent} from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';

const props = defineProps<{
  modelValue: string; // For v-model
  placeholder?: string;
}>();

const emit = defineEmits(['update:modelValue', 'change', 'textUpdate']);

// Local ref to prevent feedback loop with watch
const internalContent: Ref<string> = ref(props.modelValue);

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({}),
    Placeholder.configure({
      placeholder: props.placeholder || 'Start writing...',
    }),
  ],
  editorProps: {
    attributes: {
      class: 'prose dark:prose-invert max-w-none focus:outline-none p-2',
    },
  },
  onUpdate: ({ editor }) => {
    const html = editor.getHTML();
    const text = editor.getText(); // Get plain text
    internalContent.value = html;
    emit('update:modelValue', html);
    emit('textUpdate', text);
    emit('change');
  },
});

watch(() => props.modelValue, (newValue) => {
  const isSame = newValue === internalContent.value;

  if (editor.value && !isSame) {
    // Update internal state *before* setting editor content
    // to prevent the watcher from re-triggering itself
    internalContent.value = newValue;
    // Set editor content without triggering the onUpdate callback again
    editor.value.commands.setContent(newValue, false);
    emit('textUpdate', editor.value.getText());
  }
});

onBeforeUnmount(() => {
  editor.value?.destroy();
});
</script>

<template>
  <EditorContent :editor="editor" class="h-full w-full"/>
</template>

<style>
/* Basic TipTap Styling (Tailwind Prose handles most) */
.ProseMirror {
  min-height: 100%;
  height: 100%;
}

.ProseMirror:focus {
  outline: none;
}

/* Placeholder styling */
.ProseMirror p.is-editor-empty:first-child::before {
  content: attr(data-placeholder);
  float: left;
  color: #adb5bd; /* Or use Tailwind muted color */
  pointer-events: none;
  height: 0;
}
</style>
