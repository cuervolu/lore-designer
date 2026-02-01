<script setup lang="ts">
import { invoke } from '@tauri-apps/api/core';
import { error as logError } from '@fltsci/tauri-plugin-tracing';
import { ref, computed } from 'vue';
import { FolderOpen, Folder } from 'lucide-vue-next';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from '@/components/ui/hover-card';
import { Skeleton } from '@/components/ui/skeleton';
import type { RecentWorkspace, WorkspacePreview } from '@lore/shared';
import { formatTimestamp } from '@lore/shared';

interface Props {
  workspace: RecentWorkspace;
  isLastUsed?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  open: [workspace: RecentWorkspace];
}>();

const iconSrc = ref<string | null>(null);
const iconError = ref(false);
const preview = ref<WorkspacePreview | null>(null);
const previewLoading = ref(false);

async function loadIcon() {
  try {
    const icon = await invoke<string>('get_workspace_icon', {
      workspacePath: props.workspace.path,
    });
    iconSrc.value = icon;
  } catch (err) {
    iconError.value = true;
  }
}

async function loadPreview() {
  if (preview.value || previewLoading.value) return;

  previewLoading.value = true;
  try {
    const result = await invoke<WorkspacePreview>('get_workspace_preview', {
      workspacePath: props.workspace.path,
    });
    preview.value = result;
  } catch (err) {
    await logError(`Failed to load workspace preview: ${err}`);
  } finally {
    previewLoading.value = false;
  }
}

const truncatedPath = computed(() => {
  const parts = props.workspace.path.split(/[/\\]/);
  if (parts.length > 3) {
    return `...${parts.slice(-2).join('/')}`;
  }
  return props.workspace.path;
});

loadIcon();
</script>

<template>
  <HoverCard :open-delay="300" @open-auto-focus="loadPreview">
    <HoverCardTrigger as-child>
      <Card
        class="relative flex flex-col items-center gap-3 p-4 cursor-pointer transition-all duration-200 hover:shadow-lg hover:border-primary/50 hover:scale-[1.02]"
        @click="emit('open', workspace)"
      >
        <div class="flex items-center justify-center w-12 h-12">
          <img
            v-if="iconSrc && !iconError"
            :src="`asset://localhost/${iconSrc}`"
            :alt="workspace.name"
            class="w-full h-full object-contain"
            @error="iconError = true"
          />
          <FolderOpen v-else :size="48" class="text-primary" />
        </div>

        <div class="w-full text-center">
          <h3 class="text-lg font-semibold truncate">
            {{ workspace.name }}
          </h3>
          <p
            class="text-xs text-muted-foreground truncate"
            :title="workspace.path"
          >
            {{ truncatedPath }}
          </p>
        </div>

        <Badge
          v-if="isLastUsed"
          variant="secondary"
          class="absolute top-2 right-2"
        >
          Last Used
        </Badge>

        <p class="text-xs text-muted-foreground">
          {{ formatTimestamp(workspace.last_accessed) }}
        </p>
      </Card>
    </HoverCardTrigger>

    <HoverCardContent class="w-80" side="right">
      <div class="space-y-3">
        <div class="space-y-1">
          <h4 class="text-sm font-semibold flex items-center gap-2">
            <Folder :size="16" class="text-primary" />
            Workspace Structure
          </h4>
        </div>

        <div v-if="previewLoading" class="space-y-2">
          <Skeleton class="h-4 w-full" />
          <Skeleton class="h-4 w-3/4" />
          <Skeleton class="h-4 w-1/2" />
        </div>

        <div
          v-else-if="preview && preview.folders.length > 0"
          class="space-y-1"
        >
          <div
            v-for="folder in preview.folders"
            :key="folder.name"
            class="flex items-center justify-between text-sm"
          >
            <span class="flex items-center gap-2">
              <Folder :size="14" class="text-muted-foreground" />
              {{ folder.name }}
            </span>
            <span class="text-xs text-muted-foreground">
              {{ folder.file_count }}
              {{ folder.file_count === 1 ? 'file' : 'files' }}
            </span>
          </div>
        </div>

        <p v-else class="text-sm text-muted-foreground">Empty workspace</p>

        <div class="pt-2 border-t space-y-1">
          <p class="text-xs text-muted-foreground flex items-center gap-2">
            <span class="font-medium">Path:</span>
            <span class="truncate">{{ workspace.path }}</span>
          </p>
          <p class="text-xs text-muted-foreground">
            <span class="font-medium">Last accessed:</span>
            {{ formatTimestamp(workspace.last_accessed) }}
          </p>
        </div>
      </div>
    </HoverCardContent>
  </HoverCard>
</template>
