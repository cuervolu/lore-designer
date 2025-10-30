<script setup lang="ts">
import {invoke} from "@tauri-apps/api/core";
import {open} from '@tauri-apps/plugin-dialog'
import {error} from '@tauri-apps/plugin-log'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import WorkspaceItem from '@wizard/components/WorkspaceItem.vue'
import { useRecentWorkspacesStore } from '@wizard/stores/recent-workspaces.store'
import { toast } from 'vue-sonner'

const router = useRouter()
const { t } = useI18n()
const recentWorkspacesStore = useRecentWorkspacesStore()

const searchQuery = ref('')
const isLoading = ref(false)

onMounted(async () => {
  try {
    isLoading.value = true
    await recentWorkspacesStore.loadRecentWorkspaces()
  } catch (err) {
    toast.error(t('wizard.workspaces.errors.loadFailed'))
  } finally {
    isLoading.value = false
  }
})

const filteredWorkspaces = computed(() => {
  const query = searchQuery.value.toLowerCase()
  if (!query) return recentWorkspacesStore.workspaces

  return recentWorkspacesStore.workspaces.filter(workspace =>
    workspace.name.toLowerCase().includes(query) ||
    workspace.path.toLowerCase().includes(query)
  )
})

const handleOpenWorkspace = async () => {
  try {
    const selectedPath = await open({
      directory: true,
      title: t('wizard.workspaces.openTitle')
    });

    if (selectedPath) {
      const result = await invoke<{ path: string, message: string }>('open_existing_workspace', {
        workspacePath: selectedPath
      });

      await router.push({
        name: 'editor',
        query: { path: result.path }
      });
    }
  } catch (err) {
    await error(`Failed to open workspace: ${err}`);
    toast.error(t('wizard.workspaces.errors.openFailed'))
  }
};
</script>

<template>
  <div class="container mx-auto p-8">
    <header class="flex justify-between items-center mb-8">
      <h1 class="text-2xl font-bold">{{ $t('welcome.routeTitles.welcome') }}</h1>
      <div class="flex gap-2">
        <Button variant="outline"> {{ $t('wizard.workspaces.import') }} </Button>
        <Button variant="outline" @click="handleOpenWorkspace"> {{ $t('wizard.workspaces.open') }} </Button>
        <Button @click="router.push({ name: 'new-workspace' })"> {{ $t('welcome.newWorkspace') }} </Button>
      </div>
    </header>

    <div class="relative w-full max-w-sm items-center mb-6">
      <Input
        v-model="searchQuery"
        type="text"
        :placeholder="$t('wizard.workspaces.searchPlaceholder')"
        class="pl-10 max-w-md"
      />
      <span class="absolute start-0 inset-y-0 flex items-center justify-center px-2">
        <Search class="size-6 text-muted-foreground" />
      </span>
    </div>

    <div v-if="isLoading" class="py-4">
      <p class="text-muted-foreground text-center">{{ $t('wizard.workspaces.loading') }}</p>
    </div>

    <div v-else-if="filteredWorkspaces.length === 0" class="py-8 text-center">
      <p class="text-muted-foreground mb-4">{{ $t('wizard.workspaces.empty.message') }}</p>
      <Button @click="router.push({ name: 'new-workspace' })">{{ $t('wizard.workspaces.empty.createButton') }}</Button>
    </div>

    <div v-else class="grid gap-4 mt-4">
      <WorkspaceItem
        v-for="workspace in filteredWorkspaces"
        :key="workspace.path"
        :workspace="workspace"
      />
    </div>
  </div>
</template>
