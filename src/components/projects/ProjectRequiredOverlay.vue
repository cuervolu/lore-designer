<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useProjectStore} from '~/stores/project.store'
import {Button} from '@/components/ui/button'
import CreateProjectDialog from '~/components/projects/CreateProjectDialog.vue'

const projectStore = useProjectStore()
const hasProjects = ref(false)
const router = useRouter()
const localePath = useLocalePath()

onMounted(async () => {
  hasProjects.value = await projectStore.checkIfProjectsExist()
})

const goBack = () => {
  router.push(localePath('/'))
}

const onProjectCreated = (projectId: number) => {
  hasProjects.value = true
  router.push(localePath(`/projects/${projectId}`))
}
</script>
<template>
  <div v-if="!hasProjects"
       class="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center">
    <div class="bg-card p-6 rounded-lg shadow-lg max-w-md w-full text-center">
      <h2 class="text-2xl font-bold mb-4">{{ $t('projectOverlay.title') }}</h2>
      <p class="mb-6">{{ $t('projectOverlay.message') }}</p>
      <div class="flex justify-center space-x-4">
        <Button @click="goBack">{{ $t('projectOverlay.goBack') }}</Button>
        <CreateProjectDialog @project-created="onProjectCreated"/>
      </div>
    </div>
  </div>
</template>

