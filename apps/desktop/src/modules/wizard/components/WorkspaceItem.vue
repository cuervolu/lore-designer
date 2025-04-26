<script setup lang="ts">
import { convertFileSrc, invoke } from "@tauri-apps/api/core";
import { writeText } from '@tauri-apps/plugin-clipboard-manager';
import { revealItemInDir } from '@tauri-apps/plugin-opener';
import { error as logError} from "@tauri-apps/plugin-log";
import { onMounted, ref } from "vue";
import { Folder, EllipsisVertical, ExternalLink, Copy, Trash } from 'lucide-vue-next';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button'
import { toast } from 'vue-sonner'

const props = defineProps<{
  workspace: {
    id?: number;
    name: string;
    path: string;
    exists?: boolean;
  }
}>();

const emit = defineEmits<{
  remove: [path: string]
}>()

const workspaceIcon = ref<string>('');
const isIconLoaded = ref<boolean>(false);
const isIconError = ref<boolean>(false);

const defaultIcon = new URL('@/assets/logo.webp', import.meta.url).href;

onMounted(async () => {
  try {
    workspaceIcon.value = await invoke<string>('get_workspace_icon', {
      workspacePath: props.workspace.path
    });
    isIconLoaded.value = true;
  } catch (error) {
    await logError(`Failed to load workspace icon: ${error}`);
    workspaceIcon.value = defaultIcon;
    isIconError.value = true;
  }
});

const handleOpen = async () => {
  if (!props.workspace.exists) {
    toast.error('Workspace no longer exists at this location');
    return;
  }

  // Here we would navigate to the editor or open the workspace
  toast.info('Opening workspace...', {
    description: 'This functionality will be implemented later'
  });
};

const handleShowInExplorer = async () => {
  try {
    console.log("Opening path:", props.workspace.path);
    await revealItemInDir(props.workspace.path);
  } catch (err) {
    await logError(`Failed to open path: ${err}`);
    toast.error('Failed to open location');
  }
};

const handleCopyPath = async () => {
  await writeText(props.workspace.path);
  toast.success('Path copied to clipboard');
};

const handleRemove = () => {
  emit('remove', props.workspace.path);
  toast.success('Workspace removed from recent list');
};
</script>

<template>
  <div
    class="flex items-center p-4 rounded-md cursor-pointer group transition-all duration-200"
    :class="[
      workspace.exists !== false
        ? 'hover:bg-accent/60 hover:text-accent-foreground'
        : 'opacity-60 hover:bg-destructive/10'
    ]"
  >
    <div class="mr-4 w-10 h-10 rounded-md flex items-center justify-center"
         :class="[workspace.exists !== false ? 'bg-muted' : 'bg-destructive/20']">
      <img
        v-if="isIconLoaded && workspace.exists !== false"
        :src="workspaceIcon"
        alt="Workspace Icon"
        class="w-6 h-6 object-contain"
        @error="isIconError = true"
      />
      <template v-else-if="isIconError || workspace.exists === false">
        <Folder class="w-6 h-6" :class="workspace.exists !== false ? 'text-muted-foreground' : 'text-destructive/50'" />
      </template>
      <Folder v-else class="w-6 h-6 text-muted-foreground" />
    </div>
    <div class="flex-1">
      <h3 class="font-medium flex items-center gap-2">
        {{ workspace.name }}
        <span v-if="workspace.exists === false" class="text-xs text-destructive font-normal">(Not found)</span>
      </h3>
      <p class="text-sm text-muted-foreground">{{ workspace.path }}</p>
    </div>
    <div class="flex">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" class="p-2 hover:bg-accent/60 hover:text-accent-foreground rounded-md">
            <EllipsisVertical class="w-5 h-5" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem @click="handleOpen" :disabled="workspace.exists === false">
            <Folder class="mr-2 h-4 w-4" />
            <span>Open</span>
          </DropdownMenuItem>
          <DropdownMenuItem @click="handleShowInExplorer">
            <ExternalLink class="mr-2 h-4 w-4" />
            <span>Show in Explorer</span>
          </DropdownMenuItem>
          <DropdownMenuItem @click="handleCopyPath">
            <Copy class="mr-2 h-4 w-4" />
            <span>Copy Path</span>
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem variant="destructive" @click="handleRemove">
            <Trash class="mr-2 h-4 w-4" />
            <span>Remove from List</span>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  </div>
</template>
