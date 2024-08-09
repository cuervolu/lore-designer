<script setup lang="ts">
import {ref, onMounted, watch} from 'vue'
import {useProjectStore} from '@/stores/project.store'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

const projectStore = useProjectStore()
const projects = ref<{ id: number; name: string }[]>([])
const selectedProjectId = ref<number | null>(null)

onMounted(async () => {
  projects.value = await projectStore.getProjectsForSelection()
  if (projects.value.length > 0) {
    selectedProjectId.value = projects.value[0].id
    emit('update:projectId', selectedProjectId.value)
  }
})

const emit = defineEmits(['update:projectId'])

const updateSelectedProject = (id: string) => {
  selectedProjectId.value = parseInt(id, 10)
  emit('update:projectId', selectedProjectId.value)
}

// Watch for changes in projects and update selected project if necessary
watch(projects, (newProjects) => {
  if (newProjects.length > 0 && !selectedProjectId.value) {
    selectedProjectId.value = newProjects[0].id
    emit('update:projectId', selectedProjectId.value)
  }
})
</script>

<template>
  <Select v-model="selectedProjectId" @update:modelValue="updateSelectedProject">
    <SelectTrigger class="w-full">
      <SelectValue :placeholder="$t('projects.selectProject')"/>
    </SelectTrigger>
    <SelectContent>
      <SelectGroup>
        <SelectItem v-for="project in projects" :key="project.id" :value="project.id.toString()">
          {{ project.name }}
        </SelectItem>
      </SelectGroup>
    </SelectContent>
  </Select>
</template>