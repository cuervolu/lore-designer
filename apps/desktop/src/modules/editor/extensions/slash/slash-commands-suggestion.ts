import {error as logError} from "@tauri-apps/plugin-log";
import {type Editor, type Range, VueRenderer} from "@tiptap/vue-3";
import type {SuggestionOptions, SuggestionProps} from "@tiptap/suggestion";
import {
  computePosition,
  flip,
  shift,
  offset,
  autoUpdate,
} from "@floating-ui/dom";
import CommandsList from "@editor/components/CommandsList.vue";
import type {CommandItem, CommandExecProps} from "@editor/types/tiptap.types.ts";

import {
  Heading1,
  Heading2,
  Heading3,
  List,
  ListOrdered,
  Quote,
  Code,
  Minus,
  ImageIcon,
  Table,
} from "lucide-vue-next";
import {useImage} from "@editor/composables/useImage.ts";

const ALL_COMMANDS: CommandItem[] = [
  {
    title: "Heading 1",
    description: "Large section heading",
    icon: Heading1,
    command: ({editor, range}: CommandExecProps) => {
      editor
      .chain()
      .focus()
      .deleteRange(range)
      .setNode("heading", {level: 1})
      .run();
    },
  },
  {
    title: "Heading 2",
    description: "Medium section heading",
    icon: Heading2,
    command: ({editor, range}: CommandExecProps) => {
      editor
      .chain()
      .focus()
      .deleteRange(range)
      .setNode("heading", {level: 2})
      .run();
    },
  },
  {
    title: "Heading 3",
    description: "Small section heading",
    icon: Heading3,
    command: ({editor, range}: CommandExecProps) => {
      editor
      .chain()
      .focus()
      .deleteRange(range)
      .setNode("heading", {level: 3})
      .run();
    },
  },
  {
    title: "Bullet List",
    description: "Create a simple bullet list",
    icon: List,
    command: ({editor, range}: CommandExecProps) => {
      editor.chain().focus().deleteRange(range).toggleBulletList().run();
    },
  },
  {
    title: "Numbered List",
    description: "Create a list with numbering",
    icon: ListOrdered,
    command: ({editor, range}: CommandExecProps) => {
      editor.chain().focus().deleteRange(range).toggleOrderedList().run();
    },
  },
  {
    title: "Blockquote",
    description: "Capture a quote",
    icon: Quote,
    command: ({editor, range}: CommandExecProps) => {
      editor.chain().focus().deleteRange(range).toggleBlockquote().run();
    },
  },
  {
    title: "Code Block",
    description: "Capture a snippet of code",
    icon: Code,
    command: ({editor, range}: CommandExecProps) => {
      editor.chain().focus().deleteRange(range).toggleCodeBlock().run();
    },
  },
  {
    title: "Horizontal Rule",
    description: "Visually divide sections",
    icon: Minus,
    command: ({editor, range}: CommandExecProps) => {
      editor.chain().focus().deleteRange(range).setHorizontalRule().run();
    },
  },
  {
    title: 'Image',
    description: 'Embed an image from your computer',
    icon: ImageIcon,
    command:async ({editor, range}: CommandExecProps) => {
      const {selectAndCopyImageToWorkspace, error: imageError} = useImage();

      const managedImage = await selectAndCopyImageToWorkspace();

      if (managedImage) {
        editor.chain().focus().deleteRange(range).setImage({
          src: managedImage.displaySrc,
          alt: managedImage.altText,
        }).run();
      } else if (imageError.value) {
        await logError(`Image command error: ${imageError.value.message}`);
      }
    },
  },
  {
    title: 'Table',
    description: 'Insert a new table',
    icon: Table,
    command: ({editor, range}: CommandExecProps) => {
      editor.chain().focus().deleteRange(range).insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run();
    },
  },
];

export const suggestionConfig: Omit<SuggestionOptions<CommandItem>, "editor"> = {
  char: "/",
  command: ({
              editor,
              range,
              props,
            }: {
    editor: Editor;
    range: Range;
    props: CommandItem;
  }) => {
    props.command({editor, range});
  },

  items: ({query}: { query: string }): CommandItem[] => {
    if (!query) {
      return ALL_COMMANDS.slice(0, 10);
    }
    return ALL_COMMANDS.filter((item) =>
      item.title.toLowerCase().startsWith(query.toLowerCase())
    ).slice(0, 10);
  },

  render: () => {
    let component: VueRenderer | null = null;
    let popupElement: HTMLElement | null = null;
    let cleanupFloatingUI: (() => void) | null = null;

    const destroyPopup = () => {
      cleanupFloatingUI?.();
      cleanupFloatingUI = null;
      component?.destroy();
      popupElement?.remove();
      component = null;
      popupElement = null;
    };

    return {
      onStart: (props: SuggestionProps<CommandItem>) => {
        component = new VueRenderer(CommandsList, {
          props: {
            items: props.items,
            commandCallback: (item: CommandItem) => {
              props.command(item);
            },
          },
          editor: props.editor,
        });

        const newPopupInstance = component.element as HTMLElement;

        if (!newPopupInstance) {
          console.warn("CommandsList popup element not created by VueRenderer.");
          return; // Exit if popup element couldn't be created
        }
        popupElement = newPopupInstance;

        document.body.appendChild(popupElement);
        popupElement.style.visibility = "hidden";

        const currentPopup = popupElement;

        const virtualReference = {
          getBoundingClientRect: () => {
            const rect = props.clientRect?.();
            return rect || new DOMRect(0, 0, 0, 0);
          },
        };

        cleanupFloatingUI = autoUpdate(
          virtualReference,
          currentPopup,
          () => {
            const rect = virtualReference.getBoundingClientRect();
            if (!rect || (rect.width === 0 && rect.height === 0)) {
              currentPopup.style.visibility = "hidden";
              return;
            }

            computePosition(virtualReference, currentPopup, {
              placement: "bottom-start",
              middleware: [offset(8), flip({padding: 8}), shift({padding: 8})],
            }).then(({x, y, strategy}) => {
              currentPopup.style.left = `${x}px`;
              currentPopup.style.top = `${y}px`;
              currentPopup.style.position = strategy;
              currentPopup.style.visibility = "visible";
            });
          },
          {animationFrame: true}
        );
      },

      onUpdate: (props: SuggestionProps<CommandItem>) => {
        component?.updateProps({items: props.items});
        // autoUpdate handles repositioning. This visibility check is an immediate
        // reaction or fallback.
        if (popupElement && props.clientRect?.()) {
          popupElement.style.visibility = "visible";
        } else if (popupElement) {
          popupElement.style.visibility = "hidden";
        }
      },

      onKeyDown: (props: { event: KeyboardEvent }): boolean => {
        if (props.event.key === "Escape") {
          destroyPopup();
          return true;
        }
        return component?.ref?.onKeyDown(props) || false;
      },

      onExit: () => {
        destroyPopup();
      },
    };
  },
};


