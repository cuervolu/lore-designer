/*
* This file is full of types and interfaces that are used in the Lore Designer Tiptap editor.
* It is a work in progress and will be updated as the editor is developed.
*  (Maybe I should move it to a separate package?)
* */

import type {Editor, Range} from "@tiptap/core";
import type {LucideIcon} from "lucide-vue-next";

export interface CommandExecProps {
  editor: Editor;
  range: Range;
}

export interface CommandItem {
  title: string;
  description?: string;
  icon?: LucideIcon;
  command: (props: CommandExecProps) => void;
}
