<script setup lang="ts">
import {ref, watch, onMounted, onUnmounted, nextTick} from 'vue';
import {ScrollArea} from '@/components/ui/scroll-area';
import type {LucideIcon} from 'lucide-vue-next';

export interface CommandMenuItem {
  id: string;
  type: 'command' | 'character' | 'location' | 'lore';
  label: string;
  icon: LucideIcon;
  action: () => void;
}

const props = defineProps<{
  items: CommandMenuItem[];
  loading?: boolean;
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
      item.scrollIntoView({block: 'nearest'});
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
  <div class="command-menu">
    <div v-if="props.loading" class="command-menu-loading">
      <div class="spinner"></div>
      <span>Searching files...</span>
    </div>


    <div v-else-if="props.items.length > 0"
         class="z-50 min-w-[250px] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md animate-in fade-in-0 zoom-in-95 command-menu-items"
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
            <component :is="item.icon" class="h-4 w-4 text-muted-foreground"/>
            <span class="flex-1">{{ item.label }}</span>
          </button>
        </div>
      </ScrollArea>
    </div>
    <div v-else class="command-menu-empty">
      <span>No files found</span>
    </div>
  </div>
</template>

<style scoped>
.command-menu-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  color: #666;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.command-menu-empty {
  padding: 8px 12px;
  color: #666;
  font-style: italic;
}
</style>
