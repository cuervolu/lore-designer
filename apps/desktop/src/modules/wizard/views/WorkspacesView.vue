<script setup lang="ts">
import {invoke} from "@tauri-apps/api/core"
import {open} from '@tauri-apps/plugin-dialog'
import {error} from 'tauri-plugin-tracing'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, FolderOpen } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import WorkspaceItem from '@wizard/components/WorkspaceItem.vue'
import EmptyWorkspaces from '@wizard/components/EmptyWorkspaces.vue'
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
    })

    if (selectedPath) {
      const result = await invoke<{ path: string, message: string }>('open_existing_workspace', {
        workspacePath: selectedPath
      })

      await router.push({
        name: 'editor',
        query: { path: result.path }
      })
    }
  } catch (err) {
    await error(`Failed to open workspace: ${err}`)
    toast.error(t('wizard.workspaces.errors.openFailed'))
  }
}

const handleCreateNew = () => {
  router.push({ name: 'new-workspace' })
}
</script>

<template>
  <div class="container mx-auto p-8 max-w-7xl">
    <header class="mb-8">
      <div class="flex justify-between items-start mb-4">
        <div>
          <h1 class="text-3xl font-bold mb-2">{{ $t('welcome.routeTitles.welcome') }}</h1>
          <p class="text-muted-foreground">{{ $t('wizard.workspaces.subtitle') }}</p>
        </div>
        <div class="flex gap-2">
          <Button variant="outline" size="lg" @click="handleOpenWorkspace" class="gap-2">
            <FolderOpen class="w-4 h-4" />
            {{ $t('wizard.workspaces.open') }}
          </Button>
          <Button size="lg" @click="handleCreateNew" class="gap-2">
            <Plus class="w-4 h-4" />
            {{ $t('welcome.newWorkspace') }}
          </Button>
        </div>
      </div>
    </header>

    <div v-if="!isLoading && recentWorkspacesStore.workspaces.length > 0" class="mb-6">
      <div class="relative w-full max-w-md">
        <Input
          v-model="searchQuery"
          type="text"
          :placeholder="$t('wizard.workspaces.searchPlaceholder')"
          class="pl-10"
        />
        <span class="absolute start-0 inset-y-0 flex items-center justify-center px-3">
          <Search class="size-4 text-muted-foreground" />
        </span>
      </div>
    </div>

    <div v-if="isLoading" class="flex items-center justify-center py-16">
      <div class="animate-pulse text-muted-foreground">{{ $t('wizard.workspaces.loading') }}</div>
    </div>

    <EmptyWorkspaces
      v-else-if="filteredWorkspaces.length === 0 && !searchQuery"
      :on-create-click="handleCreateNew"
    />

    <div v-else-if="filteredWorkspaces.length === 0 && searchQuery" class="text-center py-16">
      <p class="text-muted-foreground">No workspaces found matching "{{ searchQuery }}"</p>
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <transition-group name="workspace-list">
        <WorkspaceItem
          v-for="workspace in filteredWorkspaces"
          :key="workspace.path"
          :workspace="workspace"
          @remove="recentWorkspacesStore.removeRecentWorkspace(workspace.path)"
        />
      </transition-group>
    </div>
  </div>
</template>

<style scoped>
.workspace-list-enter-active,
.workspace-list-leave-active {
  transition: all 0.3s ease;
}

.workspace-list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.workspace-list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
