<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { invoke } from '@tauri-apps/api/core'
import { error as logError } from '@fltsci/tauri-plugin-tracing'
import { useRouter } from 'vue-router'
import { Plus, FolderOpen } from 'lucide-vue-next'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import WorkspaceGrid from './WorkspaceGrid.vue'
import type { RecentWorkspace } from '@lore/shared'

const router = useRouter()
const workspaces = ref<RecentWorkspace[]>([])
const filteredWorkspaces = ref<RecentWorkspace[]>([])
const searchQuery = ref('')
const currentPage = ref(1)
const loading = ref(true)

async function loadWorkspaces() {
  loading.value = true
  try {
    const result = await invoke<RecentWorkspace[]>('get_recent_workspaces_command')
    workspaces.value = result
    filteredWorkspaces.value = result
  } catch (err) {
    await logError(`Failed to load recent workspaces: ${err}`)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  const query = searchQuery.value.toLowerCase().trim()
  
  if (!query) {
    filteredWorkspaces.value = workspaces.value
    currentPage.value = 1
    return
  }
  
  filteredWorkspaces.value = workspaces.value.filter(workspace =>
    workspace.name.toLowerCase().includes(query) ||
    workspace.path.toLowerCase().includes(query)
  )
  
  currentPage.value = 1
}

function openWorkspace(workspace: RecentWorkspace) {
  console.log('Opening workspace:', workspace)
}

function createWorkspace() {
  console.log('Create new workspace')
}

function openExisting() {
  console.log('Open existing workspace')
}

onMounted(loadWorkspaces)
</script>

<template>
  <div class="flex flex-col h-full p-6 gap-6">
    <div class="flex items-center gap-4">
      <div class="flex-1">
        <Input
          v-model="searchQuery"
          placeholder="Search your workspaces..."
          class="max-w-md"
          @input="handleSearch"
        />
      </div>
      
      <div class="flex gap-2">
        <Button variant="outline" @click="openExisting">
          <FolderOpen :size="16" />
          Open
        </Button>
        <Button @click="createWorkspace">
          <Plus :size="16" />
          New Workspace
        </Button>
      </div>
    </div>

    <WorkspaceGrid
      v-if="!loading"
      :workspaces="filteredWorkspaces"
      :current-page="currentPage"
      @update:current-page="currentPage = $event"
      @open-workspace="openWorkspace"
      @create-workspace="createWorkspace"
      @open-existing="openExisting"
    />

    <div v-else class="flex items-center justify-center h-full">
      <p class="text-muted-foreground">Loading workspaces...</p>
    </div>
  </div>
</template>