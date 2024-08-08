<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useProjectStore} from '~/stores/project.store'
import EditProjectForm from '~/components/projects/EditProjectForm.vue'
import type {Project} from '~/interfaces'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const {t} = useI18n()

const project = ref<Project | null>(null)
const loading = ref(true)

onMounted(async () => {
  const projectId = parseInt(route.params.id as string)
  if (!isNaN(projectId)) {
    project.value = await projectStore.getProjectById(projectId)
  }
  loading.value = false
})

const handleProjectUpdate = (updatedProject: Project) => {
  project.value = updatedProject
  router.push(`/projects/${updatedProject.id}`)
}
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-4">{{ t('projects.editProject') }}</h1>
    <div v-if="loading" class="text-center">
      <p>{{ t('loading') }}</p>
    </div>
    <div v-else-if="project">
      <EditProjectForm :project="project" @update:project="handleProjectUpdate"/>
    </div>
    <div v-else class="text-center">
      <p>{{ t('projects.notFound') }}</p>
    </div>
  </div>
</template>