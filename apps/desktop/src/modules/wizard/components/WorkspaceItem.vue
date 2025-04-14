<script setup lang="ts">
import { writeText } from '@tauri-apps/plugin-clipboard-manager';
import { Folder, EllipsisVertical, ExternalLink, Copy, Trash } from 'lucide-vue-next';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button'

const props = defineProps<{
  workspace: {
    id: number;
    name: string;
    path: string;
  }
}>();

const handleOpen = () => {
  console.log('Open workspace');
  // Implementation will be added later
};

const handleShowInExplorer = () => {
  console.log('Show in Explorer');
  // Implementation will be added later
};

const handleCopyPath = async () => {
  await writeText(props.workspace.path);
};

const handleRemove = () => {
  console.log('Remove workspace');
  // Implementation will be added later
};
</script>

<template>
  <div class="flex items-center p-4 hover:bg-accent/60 hover:text-accent-foreground rounded-md cursor-pointer group">
    <div class="mr-4 bg-muted w-10 h-10 rounded-md flex items-center justify-center">
      <Folder class="w-6 h-6 text-muted-foreground" />
    </div>
    <div class="flex-1">
      <h3 class="font-medium">{{ workspace.name }}</h3>
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
          <DropdownMenuItem @click="handleOpen">
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
            <span>Remove</span>
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  </div>
</template>
