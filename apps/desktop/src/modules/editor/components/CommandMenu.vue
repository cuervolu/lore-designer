<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue';
import { ScrollArea } from '@/components/ui/scroll-area';
import type { LucideIcon } from 'lucide-vue-next';

export interface CommandMenuItem {
  id: string;
  type: 'command' | 'character' | 'location' | 'lore';
  label: string;
  icon: LucideIcon;
  action: () => void;
}

const props = defineProps<{
  items: CommandMenuItem[];
}>();

const selectedIndex = ref(0);
const commandListRef = ref<HTMLElement | null>(null);

const onKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'ArrowUp') {
    event.preventDefault();
    selectedIndex.value = (selectedIndex.value + props.items.length - 1) % props.items.length;
    scrollToItem();
  } else if (event.key === 'ArrowDown') {
    event.preventDefault();
    selectedIndex.value = (selectedIndex.value + 1) % props.items.length;
    scrollToItem();
  } else if (event.key === 'Enter') {
    event.preventDefault();
    selectItem(selectedIndex.value);
  }
};

const selectItem = (index: number) => {
  const item = props.items[index];
  if (item) {
    item.action();
  }
};

const scrollToItem = () => {
  nextTick(() => {
    const list = commandListRef.value;
    const item = list?.children[selectedIndex.value] as HTMLElement;
    if (item) {
      item.scrollIntoView({ block: 'nearest' });
    }
  });
};

watch(() => props.items, () => {
  selectedIndex.value = 0;
});

onMounted(() => {
  window.addEventListener('keydown', onKeyDown);
});
onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown);
});
</script>

<template>
  <div
    class="z-50 min-w-[250px] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md animate-in fade-in-0 zoom-in-95"
  >
    <ScrollArea class="max-h-[300px] overflow-y-auto">
      <div ref="commandListRef" class="flex flex-col">
        <div v-if="items.length === 0" class="p-4 text-center text-sm text-muted-foreground">
          No results
        </div>
        <button
          v-for="(item, index) in items"
          :key="item.id"
          @click="selectItem(index)"
          :class="[
            'flex items-center gap-3 rounded-sm px-2 py-1.5 text-sm text-left outline-none transition-colors w-full',
            index === selectedIndex ? 'bg-accent text-accent-foreground' : 'hover:bg-accent/50',
          ]"
        >
          <component :is="item.icon" class="h-4 w-4 text-muted-foreground" />
          <span class="flex-1">{{ item.label }}</span>
        </button>
      </div>
    </ScrollArea>
  </div>
</template>
