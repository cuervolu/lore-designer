<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next';
import { Progress } from '@/components/ui/progress';

const props = defineProps<{
  isIndexing: boolean;
  progress: number;
}>();
</script>

<template>
  <div class="border-t h-6 bg-muted/20 flex items-center px-3 text-xs text-muted-foreground justify-between">
    <!-- Left side - status info -->
    <div class="flex items-center">
      <div v-if="isIndexing" class="flex items-center gap-2">
        <Loader2 class="h-3 w-3 animate-spin" />
        <span>Indexing...</span>
        <Progress class="w-24 h-1.5" :value="progress" />
      </div>
      <div v-else>Ready</div>
    </div>

    <!-- Right side - version info -->
    <div>Lore Designer v0.0.1</div>
  </div>
</template>
