<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useProjectStore } from '~/stores/project.store'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import {
  Pagination,
  PaginationEllipsis,
  PaginationList,
  PaginationListItem,
  PaginationNext,
  PaginationPrev,
} from '@/components/ui/pagination'
import CreateProjectDialog from '~/components/projects/CreateProjectDialog.vue'
import ProjectCard from '~/components/projects/ProjectCard.vue'
import CustomBreadcrumb from '~/components/CustomBreadcrumb.vue'
import type { Project } from '~/interfaces'

const projectStore = useProjectStore()
const projects = ref<Project[]>([])
const totalProjects = ref(0)
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)

const totalPages = computed(() => Math.ceil(totalProjects.value / pageSize.value))

const fetchProjects = async (force = false) => {
  loading.value = true
  try {
    const { projects: fetchedProjects, total } = await projectStore.getProjects(currentPage.value, pageSize.value, force)
    projects.value = fetchedProjects
    totalProjects.value = total
  } catch (error) {
    console.error('Failed to fetch projects:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchProjects())
watch(currentPage, () => fetchProjects(true))

const changePage = (page: number) => {
  currentPage.value = page
}

const visiblePages = computed(() => {
  const delta = 2
  const range: (number | '...')[] = []
  for (
      let i = Math.max(2, currentPage.value - delta);
      i <= Math.min(totalPages.value - 1, currentPage.value + delta);
      i++
  ) {
    range.push(i)
  }
  if (currentPage.value - delta > 2) {
    range.unshift("...")
  }
  if (currentPage.value + delta < totalPages.value - 1) {
    range.push("...")
  }
  range.unshift(1)
  if (totalPages.value !== 1) {
    range.push(totalPages.value)
  }
  return range
})

const { t } = useI18n()
const localePath = useLocalePath()
const router = useRouter()

const navigateToProject = (projectId: number) => {
  router.push(localePath(`/projects/${projectId}`))
}

const onProjectCreated = (projectId: number) => {
  fetchProjects(true)
  router.push(localePath(`/projects/${projectId}`))
}
</script>

<template>
  <div class="p-6 max-w-7xl mx-auto">
    <CustomBreadcrumb new-route="/projects" from="home" to="projects.title" old-route="/" />

    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold">{{ t('projects.title') }}</h1>
      <CreateProjectDialog @project-created="onProjectCreated">
        <Button>{{ t('projects.createProject') }}</Button>
      </CreateProjectDialog>
    </div>

    <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
      <Skeleton v-for="i in pageSize" :key="i" class="h-64 w-full" />
    </div>

    <div v-else-if="projects.length === 0" class="flex flex-col items-center justify-center h-[50vh]">
      <p class="text-xl mb-4">{{ t('projects.noProjects') }}</p>
      <CreateProjectDialog @project-created="onProjectCreated">
        <Button>{{ t('projects.createProject') }}</Button>
      </CreateProjectDialog>
    </div>

    <div v-else>
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        <ProjectCard
            v-for="project in projects"
            :key="project.id"
            v-bind="project"
            @click="navigateToProject(project.id)"
        />
      </div>
    </div>

    <Pagination v-if="totalPages > 1" class="mt-6 flex justify-center">
      <PaginationPrev
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
      />
      <PaginationList>
        <template v-for="(page, index) in visiblePages" :key="index">
          <PaginationListItem
              v-if="typeof page === 'number'"
              :value="page"
              :active="page === currentPage"
              @click="changePage(page)"
          >
            {{ page }}
          </PaginationListItem>
          <PaginationEllipsis v-else />
        </template>
      </PaginationList>
      <PaginationNext
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
      />
    </Pagination>
  </div>
</template>

<style scoped>
.grid {
  @apply gap-6;
}
</style>