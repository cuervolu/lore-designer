<script setup lang="ts">
import { invoke, convertFileSrc } from "@tauri-apps/api/core";
import { writeText } from "@tauri-apps/plugin-clipboard-manager";
import { revealItemInDir } from "@tauri-apps/plugin-opener";
import { error as logError } from "@tauri-apps/plugin-log";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { Folder, EllipsisVertical, ExternalLink, Copy, Trash, Edit, Clock } from "lucide-vue-next";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { toast } from "vue-sonner";

const { t } = useI18n();

const props = defineProps<{
  workspace: {
    id?: number;
    name: string;
    path: string;
    exists?: boolean;
  };
}>();

const emit = defineEmits<{
  remove: [path: string];
}>();

const router = useRouter();
const workspaceIcon = ref<string>("");
const isIconLoaded = ref<boolean>(false);
const defaultIcon = new URL("@/assets/logo.webp", import.meta.url).href;

onMounted(async () => {
  try {
    const iconPath = await invoke<string>("get_workspace_icon", {
      workspacePath: props.workspace.path,
    });
    workspaceIcon.value = convertFileSrc(iconPath);
    isIconLoaded.value = true;
  } catch {
    workspaceIcon.value = defaultIcon;
  }
});

const handleOpen = async () => {
  if (!props.workspace.exists) {
    toast.error(t("wizard.workspaces.card.notFound"));
    return;
  }
  await router.push({
    name: "editor",
    query: { path: props.workspace.path },
  });
};

const handleShowInExplorer = async () => {
  try {
    await revealItemInDir(props.workspace.path);
  } catch (err) {
    await logError(`Failed to open path: ${err}`);
    toast.error(t("wizard.workspaces.card.failedToOpen"));
  }
};

const handleCopyPath = async () => {
  await writeText(props.workspace.path);
  toast.success(t("wizard.workspaces.card.pathCopied"));
};

const handleRemove = () => {
  emit("remove", props.workspace.path);
};
</script>

<template>
  <Card
    class="group overflow-hidden transition-all duration-300 hover:shadow-lg cursor-pointer"
    :class="
      workspace.exists !== false ? 'hover:border-primary/50' : 'opacity-60 border-destructive/30'
    "
    @click="handleOpen"
  >
    <CardContent class="p-0">
      <div
        class="relative bg-linear-to-br from-primary/10 to-primary/5 h-32 flex items-center justify-center"
      >
        <img
          v-if="isIconLoaded && workspace.exists !== false"
          :src="workspaceIcon"
          alt="Workspace Icon"
          class="w-16 h-16 object-contain transition-transform group-hover:scale-110 duration-300"
        />
        <Folder
          v-else
          class="w-16 h-16 transition-transform group-hover:scale-110 duration-300"
          :class="workspace.exists !== false ? 'text-primary/60' : 'text-destructive/40'"
        />

        <div
          class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity"
          @click.stop
        >
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="sm"
                class="h-8 w-8 p-0 bg-background/80 backdrop-blur-sm"
              >
                <EllipsisVertical class="w-4 h-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem @click.stop="handleOpen" :disabled="workspace.exists === false">
                <Edit class="mr-2 h-4 w-4" />
                <span>{{ $t("wizard.workspaces.card.openInEditor") }}</span>
              </DropdownMenuItem>
              <DropdownMenuItem @click.stop="handleShowInExplorer">
                <ExternalLink class="mr-2 h-4 w-4" />
                <span>{{ $t("wizard.workspaces.card.showInExplorer") }}</span>
              </DropdownMenuItem>
              <DropdownMenuItem @click.stop="handleCopyPath">
                <Copy class="mr-2 h-4 w-4" />
                <span>{{ $t("wizard.workspaces.card.copyPath") }}</span>
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem variant="destructive" @click.stop="handleRemove">
                <Trash class="mr-2 h-4 w-4" />
                <span>{{ $t("wizard.workspaces.card.removeFromList") }}</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <div
          v-if="workspace.exists === false"
          class="absolute inset-0 bg-destructive/10 backdrop-blur-[2px] flex items-center justify-center"
        >
          <span
            class="text-destructive font-medium text-sm bg-background/90 px-3 py-1 rounded-full"
          >
            {{ $t("wizard.workspaces.card.notFoundBadge") }}
          </span>
        </div>
      </div>

      <div class="p-4">
        <h3 class="font-semibold text-lg mb-1 truncate">{{ workspace.name }}</h3>
        <p class="text-xs text-muted-foreground truncate mb-3">{{ workspace.path }}</p>

        <div class="flex items-center gap-2 text-xs text-muted-foreground">
          <Clock class="w-3 h-3" />
          <span>{{ $t("wizard.workspaces.card.lastOpened") }}</span>
        </div>
      </div>
    </CardContent>
  </Card>
</template>
