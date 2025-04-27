<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next';
import { Progress } from '@/components/ui/progress';
import {onMounted, ref} from "vue";
import {getVersion} from "@tauri-apps/api/app";
import {error} from "@tauri-apps/plugin-log";

const props = defineProps<{
  isIndexing: boolean;
  progress: number;
}>();

const appVersion = ref('0.1.0')

onMounted(async () => {
  try {
    appVersion.value = await getVersion()
  } catch (err) {
    await error(`Failed to get app version: ${err}`)
  }
})

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
    <div>Lore Designer v{{ appVersion }}</div>
  </div>
</template>
