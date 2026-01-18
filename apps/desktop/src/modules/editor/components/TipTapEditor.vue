<script setup lang="ts">
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import { Markdown } from '@tiptap/markdown'
import { Placeholder } from '@tiptap/extension-placeholder'
import { onBeforeUnmount, ref, watch, nextTick, computed } from 'vue'
import { error as logError } from 'tauri-plugin-tracing'
import CommandMenu, { type CommandMenuItem } from './CommandMenu.vue'
import { useEditorStore } from '@editor/stores/editor.store'
import { useFileIndexStore } from '@editor/stores/file-index.store'
import type { EntityLinkType } from '@editor/types/editor.types'
import {
  Code,
  Heading1,
  Heading2,
  Heading3,
  List,
  ListOrdered,
  MapPin,
  Quote,
  User,
  BookOpen
} from 'lucide-vue-next'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'textUpdate', value: string): void
  (e: 'change'): void
}>()

const editorStore = useEditorStore()
const fileIndexStore = useFileIndexStore()

const editorContainer = ref<HTMLElement | null>(null)
const isUpdatingContent = ref(false)
const internalValue = ref(props.modelValue)

const slashState = ref<{
  open: boolean
  trigger: string
  query: string
  x: number
  y: number
}>({
  open: false,
  trigger: '',
  query: '',
  x: 0,
  y: 0
})

const commandMenuItems = computed<CommandMenuItem[]>(() => {
  const items: CommandMenuItem[] = [
    {
      id: 'heading1',
      label: 'Heading 1',
      icon: Heading1,
      command: () => editor.value?.chain().focus().toggleHeading({ level: 1 }).run()
    },
    {
      id: 'heading2',
      label: 'Heading 2',
      icon: Heading2,
      command: () => editor.value?.chain().focus().toggleHeading({ level: 2 }).run()
    },
    {
      id: 'heading3',
      label: 'Heading 3',
      icon: Heading3,
      command: () => editor.value?.chain().focus().toggleHeading({ level: 3 }).run()
    },
    {
      id: 'bulletList',
      label: 'Bullet List',
      icon: List,
      command: () => editor.value?.chain().focus().toggleBulletList().run()
    },
    {
      id: 'orderedList',
      label: 'Numbered List',
      icon: ListOrdered,
      command: () => editor.value?.chain().focus().toggleOrderedList().run()
    },
    {
      id: 'blockquote',
      label: 'Quote',
      icon: Quote,
      command: () => editor.value?.chain().focus().toggleBlockquote().run()
    },
    {
      id: 'codeBlock',
      label: 'Code Block',
      icon: Code,
      command: () => editor.value?.chain().focus().toggleCodeBlock().run()
    }
  ]

  if (editorStore.activeWorkspace) {
    const characters = fileIndexStore.getFilesByType('Character')
    const locations = fileIndexStore.getFilesByType('Location')
    const lore = fileIndexStore.getFilesByType('Lore')

    if (characters.length > 0) {
      items.push({
        id: 'character-link',
        label: 'Link Character',
        icon: User,
        submenu: characters.map((char) => ({
          id: `character-${char.path}`,
          label: char.name,
          icon: User,
          command: () => insertEntityLink('character', char.path, char.name)
        }))
      })
    }

    if (locations.length > 0) {
      items.push({
        id: 'location-link',
        label: 'Link Location',
        icon: MapPin,
        submenu: locations.map((loc) => ({
          id: `location-${loc.path}`,
          label: loc.name,
          icon: MapPin,
          command: () => insertEntityLink('location', loc.path, loc.name)
        }))
      })
    }

    if (lore.length > 0) {
      items.push({
        id: 'lore-link',
        label: 'Link Lore',
        icon: BookOpen,
        submenu: lore.map((l) => ({
          id: `lore-${l.path}`,
          label: l.name,
          icon: BookOpen,
          command: () => insertEntityLink('lore', l.path, l.name)
        }))
      })
    }
  }

  return items
})

const filteredItems = computed(() => {
  if (!slashState.value.query) return commandMenuItems.value

  const query = slashState.value.query.toLowerCase()
  return commandMenuItems.value.filter((item) =>
    item.label.toLowerCase().includes(query)
  )
})

// TipTap Editor instance
const editor = useEditor({
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [1, 2, 3, 4, 5, 6]
      },
      codeBlock: {
        HTMLAttributes: {
          class: 'code-block'
        }
      }
    }),
    Markdown.configure({
      html: false,
      tightLists: true,
      transformPastedText: true,
      transformCopiedText: true
    }),
    Placeholder.configure({
      placeholder: props.placeholder || 'Start writing...'
    })
  ],
  content: props.modelValue,
  editorProps: {
    attributes: {
      class: 'tiptap-content'
    },
    handleKeyDown: (view, event) => {
      if (event.key === '/') {
        const { from } = view.state.selection
        const textBefore = view.state.doc.textBetween(Math.max(0, from - 50), from, '\n', '\0')

        if (textBefore.endsWith('\n') || textBefore.endsWith(' ') || from === 0) {
          setTimeout(() => {
            const coords = view.coordsAtPos(from + 1)
            slashState.value = {
              open: true,
              trigger: '/',
              query: '',
              x: coords.left,
              y: coords.bottom
            }
          }, 0)
        }
      }

      // Handle slash menu navigation
      if (slashState.value.open) {
        if (event.key === 'Escape') {
          closeSlashMenu()
          return true
        }
      }

      return false
    },
    handleTextInput: (view, from, to, text) => {
      if (slashState.value.open) {
        const { from: selFrom } = view.state.selection
        const textBefore = view.state.doc.textBetween(Math.max(0, selFrom - 50), selFrom, '\n', '\0')
        const match = /\/(\w*)$/.exec(textBefore)

        if (match) {
          slashState.value.query = match[1]
        } else {
          closeSlashMenu()
        }
      }

      return false
    }
  },
  onUpdate: ({ editor }) => {
    if (isUpdatingContent.value) return

    const markdown = editor.storage.markdown.getMarkdown()

    if (markdown !== internalValue.value) {
      internalValue.value = markdown
      emit('update:modelValue', markdown)

      const plainText = stripMarkdownSyntax(markdown)
      emit('textUpdate', plainText)
      emit('change')
    }
  }
})

watch(() => props.modelValue, async (newValue) => {
  if (!editor.value || newValue === internalValue.value) return

  try {
    isUpdatingContent.value = true
    internalValue.value = newValue
    editor.value.commands.setContent(newValue)

    const plainText = stripMarkdownSyntax(newValue)
    emit('textUpdate', plainText)
  } catch (error) {
    await logError(`Error updating TipTap content: ${error as string}`)
  } finally {
    await nextTick()
    isUpdatingContent.value = false
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})

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
    .trim()
}
function insertEntityLink(type: EntityLinkType, path: string, label: string) {
  if (!editor.value) return

  const linkText = `[[${type}:${label}]]`
  editor.value.chain().focus().insertContent(linkText).run()

  closeSlashMenu()
}

// Close slash menu
function closeSlashMenu() {
  slashState.value.open = false

  if (editor.value) {
    const { from } = editor.value.state.selection
    const textBefore = editor.value.state.doc.textBetween(Math.max(0, from - 50), from, '\n', '\0')

    if (textBefore.endsWith('/')) {
      editor.value.commands.deleteRange({ from: from - 1, to: from })
    }
  }
}

function handleCommandExecution(command: () => void) {
  if (editor.value) {
    const { from } = editor.value.state.selection
    const queryLength = slashState.value.query.length
    editor.value.commands.deleteRange({ from: from - queryLength - 1, to: from })
  }

  command()
  closeSlashMenu()
}
</script>

<template>
  <div class="relative h-full w-full">
    <div ref="editorContainer" class="h-full w-full">
      <EditorContent :editor="editor" class="h-full" />
    </div>

    <Teleport to="body" v-if="slashState.open">
      <div class="slash-menu-wrapper" :style="{
        position: 'fixed',
        left: `${slashState.x}px`,
        top: `${slashState.y}px`,
        zIndex: 9999
      }">
        <CommandMenu :items="filteredItems" :query="slashState.query" @select="handleCommandExecution"
          @close="closeSlashMenu" />
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.relative {
  position: relative;
}

:deep(.tiptap-content) {
  height: 100%;
  padding: 1rem;
  outline: none;
  color: var(--foreground);
  font-family: var(--font-sans);
  line-height: 1.6;
}

/* Typography - matching milkdown-custom-theme.css */
:deep(.tiptap-content p) {
  margin: 0.75rem 0;
  color: var(--foreground);
}

:deep(.tiptap-content h1),
:deep(.tiptap-content h2),
:deep(.tiptap-content h3),
:deep(.tiptap-content h4),
:deep(.tiptap-content h5),
:deep(.tiptap-content h6) {
  margin: 1.5rem 0 0.75rem;
  color: var(--foreground);
  font-weight: 600;
  line-height: 1.2;
}

:deep(.tiptap-content h1) {
  font-size: 1.8rem;
}

:deep(.tiptap-content h2) {
  font-size: 1.5rem;
}

:deep(.tiptap-content h3) {
  font-size: 1.3rem;
}

:deep(.tiptap-content h4) {
  font-size: 1.2rem;
}

:deep(.tiptap-content h5) {
  font-size: 1.1rem;
}

:deep(.tiptap-content h6) {
  font-size: 1rem;
}

/* Code blocks */
:deep(.tiptap-content pre) {
  background-color: var(--card);
  border-radius: var(--radius);
  font-family: var(--font-mono);
  padding: 1rem;
  margin: 1rem 0;
  overflow-x: auto;
}

:deep(.tiptap-content code) {
  font-family: var(--font-mono);
  font-size: 0.9em;
  background-color: var(--muted);
  color: var(--foreground);
  padding: 0.2em 0.4em;
  border-radius: var(--radius-sm);
}

:deep(.tiptap-content pre code) {
  background-color: transparent;
  padding: 0;
  border-radius: 0;
}

/* Blockquotes */
:deep(.tiptap-content blockquote) {
  border-left: 4px solid var(--primary);
  padding-left: 1rem;
  margin: 1rem 0;
  color: var(--muted-foreground);
}

/* Lists */
:deep(.tiptap-content ul),
:deep(.tiptap-content ol) {
  padding-left: 1.5rem;
  margin: 0.75rem 0;
}

:deep(.tiptap-content li) {
  margin: 0.25rem 0;
}

:deep(.tiptap-content li > p) {
  margin: 0.25rem 0;
}

/* Horizontal rule */
:deep(.tiptap-content hr) {
  border: none;
  border-top: 1px solid var(--border);
  margin: 1.5rem 0;
}

/* Links */
:deep(.tiptap-content a) {
  color: var(--primary);
  text-decoration: none;
  transition: color 0.2s;
}

:deep(.tiptap-content a:hover) {
  color: var(--primary-foreground);
  text-decoration: underline;
}

/* Placeholder */
:deep(.tiptap-content p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: var(--muted-foreground);
  pointer-events: none;
  height: 0;
}

/* Tables */
:deep(.tiptap-content table) {
  border-collapse: collapse;
  table-layout: fixed;
  width: 100%;
  margin: 1rem 0;
  overflow: hidden;
}

:deep(.tiptap-content td),
:deep(.tiptap-content th) {
  border: 1px solid var(--border);
  padding: 0.5rem;
  vertical-align: top;
  box-sizing: border-box;
  position: relative;
}

:deep(.tiptap-content th) {
  font-weight: bold;
  text-align: left;
  background-color: var(--muted);
}

/* Focus styles */
:deep(.tiptap-content:focus) {
  outline: none;
}

/* Selection */
:deep(.tiptap-content ::selection) {
  background-color: var(--primary);
  color: var(--primary-foreground);
}
</style>
