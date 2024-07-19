import {CheckSquare, FileText, House, Image, type LucideIcon, Scroll, Split} from "lucide-vue-next";

export interface Features {
  title: string;
  description: string;
  icon: LucideIcon;
}

export const features: Features[] = [
  {
    title: "gddEditor",
    description: "gddEditor",
    icon: FileText
  },
  {
    title: "dialogueTreeEditor",
    description: "dialogueTreeEditor",
    icon: Split
  },
  {
    title: "linearScriptEditor",
    description: "linearScriptEditor",
    icon: Scroll
  },
  {
    title: "localFirst",
    description: "localFirst",
    icon: House
  },
  {
    title: "assetManager",
    description: "assetManager",
    icon: Image
  },
  {
    title: "taskTracker",
    description: "taskTracker",
    icon: CheckSquare
  }
]