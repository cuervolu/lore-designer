import { Extension } from "@tiptap/core";
import Suggestion, {
  type SuggestionOptions,
} from "@tiptap/suggestion";

export interface SlashCommandsOptions {
  suggestion: Omit<SuggestionOptions, "editor">;
}

export const SlashCommandsExtension = Extension.create<SlashCommandsOptions>({
  name: "slashCommands",

  addOptions() {
    return {
      suggestion: {
        char: "/",
        command: ({ editor, range, props }) => {
          // This is a fallback, actual command logic is in suggestionConfig.command
          // and then delegated to CommandItem.command
          if (props && typeof props.command === "function") {
            props.command({ editor, range });
          }
        },
      },
    };
  },

  addProseMirrorPlugins() {
    if (!this.options.suggestion) {
      return [];
    }
    return [
      Suggestion({
        editor: this.editor,
        ...this.options.suggestion,
      }),
    ];
  },
});
