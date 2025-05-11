<script setup lang="ts">
import {ref, watch} from "vue";
import {
  Heading1,
  Heading2,
  Heading3,
  Bold,
  Italic,
  List,
  ListOrdered,
  Quote,
  Code,
  type LucideIcon,
} from "lucide-vue-next";
import type {CommandItem} from "@editor/types/tiptap.types";
import {Button} from "@/components/ui/button";

interface Props {
  items: CommandItem[];
  // This callback is provided by the suggestion plugin's render lifecycle.
  // When an item is selected, this function is called with the item.
  commandCallback: (item: CommandItem) => void;
}

const props = defineProps<Props>();

const selectedIndex = ref(0);

watch(
  () => props.items,
  () => {
    selectedIndex.value = 0;
  },
  {deep: true}
);

const onKeyDown = ({event}: { event: KeyboardEvent }): boolean => {
  if (event.key === "ArrowUp") {
    upHandler();
    return true;
  }
  if (event.key === "ArrowDown") {
    downHandler();
    return true;
  }
  if (event.key === "Enter") {
    enterHandler();
    return true;
  }
  return false;
};

const upHandler = () => {
  selectedIndex.value =
    (selectedIndex.value + props.items.length - 1) % props.items.length;
  scrollToSelected();
};

const downHandler = () => {
  selectedIndex.value = (selectedIndex.value + 1) % props.items.length;
  scrollToSelected();
};

const enterHandler = () => {
  selectItem(selectedIndex.value);
};

const selectItem = (index: number) => {
  const item = props.items[index];
  if (item) {
    props.commandCallback(item);
  }
};

const commandListRef = ref<HTMLDivElement | null>(null);

const scrollToSelected = () => {
  const list = commandListRef.value;
  if (list) {
    const selectedElement = list.children[
      selectedIndex.value
      ] as HTMLElement;
    if (selectedElement) {
      selectedElement.scrollIntoView({block: "nearest"});
    }
  }
};

defineExpose({
  onKeyDown,
});

const iconMap: Record<string, LucideIcon> = {
  Heading1,
  Heading2,
  Heading3,
  Bold,
  Italic,
  List,
  ListOrdered,
  Quote,
  Code,
};

const getIcon = (item: CommandItem): LucideIcon | undefined => {
  if (item.icon) return item.icon;
  return undefined;
};
</script>

<template>
  <div
    ref="commandListRef"
    class="bg-background text-foreground border border-border rounded-md shadow-lg p-1.5 flex flex-col gap-0.5 max-h-60 min-w-[240px] overflow-y-auto z-50"
    role="listbox"
  >
    <template v-if="items.length">
      <Button
        v-for="(item, index) in items"
        :key="item.title + index"
        variant="ghost"
        :class="[
          'w-full h-auto justify-start text-left rounded-sm text-sm',
          'px-2 py-1.5 flex items-center gap-2',
          'transition-colors duration-75 ease-in-out',
          'focus:outline-none',
          index === selectedIndex
            ? 'bg-muted text-foreground'
            : 'hover:bg-muted/50 text-foreground',
        ]"
        @click="selectItem(index)"
        @mousemove="selectedIndex = index"
        role="option"
        :aria-selected="index === selectedIndex"
      >
        <component
          v-if="getIcon(item)"
          :is="getIcon(item)"
          class="w-4 h-4 flex-shrink-0 text-muted-foreground"
          :stroke-width="1.75"
        />
        <div v-else class="w-4 h-4 flex-shrink-0"></div>
        <div class="flex flex-col">
          <span class="font-medium leading-tight">{{ item.title }}</span>
          <span
            v-if="item.description"
            class="text-xs text-muted-foreground leading-tight"
          >{{ item.description }}</span
          >
        </div>
      </Button>
    </template>
    <div
      v-else
      class="px-2 py-4 text-sm text-muted-foreground text-center"
      role="option"
    >
      No results found.
    </div>
  </div>
</template>
