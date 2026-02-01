<script setup lang="ts">
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import WorkspaceCard from './WorkspaceCard.vue'
import EmptyState from './EmptyState.vue'
import type { RecentWorkspace } from '@lore/shared'

interface Props {
  workspaces: RecentWorkspace[]
  currentPage?: number
  pageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  currentPage: 1,
  pageSize: 9,
})

const emit = defineEmits<{
  'update:currentPage': [page: number]
  openWorkspace: [workspace: RecentWorkspace]
  createWorkspace: []
  openExisting: []
}>()

const totalPages = computed(() => 
  Math.ceil(props.workspaces.length / props.pageSize)
)

const paginatedWorkspaces = computed(() => {
  const start = (props.currentPage - 1) * props.pageSize
  const end = start + props.pageSize
  return props.workspaces.slice(start, end)
})

const mostRecentWorkspace = computed(() => {
  if (props.workspaces.length === 0) return null
  
  return props.workspaces.reduce((latest, current) => 
    current.last_accessed > latest.last_accessed ? current : latest
  )
})

function nextPage() {
  if (props.currentPage < totalPages.value) {
    emit('update:currentPage', props.currentPage + 1)
  }
}

function prevPage() {
  if (props.currentPage > 1) {
    emit('update:currentPage', props.currentPage - 1)
  }
}

function goToPage(page: number) {
  emit('update:currentPage', page)
}
</script>

<template>
  <div class="flex flex-col h-full">
    <!-- Empty state -->
    <EmptyState
      v-if="workspaces.length === 0"
      @create-workspace="emit('createWorkspace')"
      @open-workspace="emit('openExisting')"
    />

    <!-- Grid de workspaces -->
    <template v-else>
      <div class="flex-1">
        <div class="grid grid-cols-3 gap-4 mb-6">
          <WorkspaceCard
            v-for="workspace in paginatedWorkspaces"
            :key="workspace.path"
            :workspace="workspace"
            :is-last-used="workspace.path === mostRecentWorkspace?.path"
            @open="emit('openWorkspace', $event)"
          />
        </div>
      </div>

      <div v-if="totalPages > 1" class="flex items-center justify-between py-4 border-t">
        <p class="text-sm text-muted-foreground">
          Showing {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, workspaces.length) }} of {{ workspaces.length }} workspaces
        </p>

        <div class="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon-sm"
            :disabled="currentPage === 1"
            @click="prevPage"
          >
            <ChevronLeft :size="16" />
          </Button>

          <div class="flex gap-1">
            <Button
              v-for="page in totalPages"
              :key="page"
              :variant="page === currentPage ? 'default' : 'outline'"
              size="icon-sm"
              @click="goToPage(page)"
            >
              {{ page }}
            </Button>
          </div>

          <Button
            variant="outline"
            size="icon-sm"
            :disabled="currentPage === totalPages"
            @click="nextPage"
          >
            <ChevronRight :size="16" />
          </Button>
        </div>
      </div>
    </template>
  </div>
</template>