<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useProjectStore} from '~/stores/project.store'
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from '@/components/ui/card'
import {Tabs, TabsContent, TabsList, TabsTrigger} from '@/components/ui/tabs'
import {Button} from '@/components/ui/button'
import DateRangePicker from "~/components/DateRangePicker.vue"
import type {Project} from '~/interfaces'

const props = defineProps<{
  projectId: number
}>()

const {t} = useI18n()
const projectStore = useProjectStore()

const project = ref<Project | null>(null)
const loading = ref(true)

onMounted(async () => {
  if (props.projectId) {
    project.value = await projectStore.getProjectById(props.projectId)
  }
  loading.value = false
})

const tasksCompleted = ref(0)
const totalTasks = ref(0)
const activeTasks = ref(0)
const teamMembers = ref(0)
const nextDeadline = ref('')

// Estas funciones se pueden implementar más adelante cuando tengas la lógica para obtener estos datos
// const fetchTasksInfo = async () => { ... }
// const fetchTeamInfo = async () => { ... }
// const fetchDeadlineInfo = async () => { ... }
</script>

<template>
  <div v-if="loading" class="flex justify-center items-center h-screen">
    <p>{{ t('loading') }}</p>
  </div>
  <div v-else-if="project" class="flex-1 space-y-4 p-8 pt-6">
    <div class="flex items-center justify-between space-y-2">
      <h2 class="text-3xl font-bold tracking-tight">
        {{ project.name }}
      </h2>
      <div class="flex items-center space-x-2">
        <DateRangePicker/>
        <Button>{{ t('projects.dashboard.download') }}</Button>
      </div>
    </div>
    <Tabs default-value="overview" class="space-y-4">
      <TabsList>
        <TabsTrigger value="overview">
          {{ t('projects.tabs.overview') }}
        </TabsTrigger>
        <TabsTrigger value="kanban">
          {{ t('projects.tabs.kanban') }}
        </TabsTrigger>
        <TabsTrigger value="gdd">
          {{ t('projects.tabs.gdd') }}
        </TabsTrigger>
        <TabsTrigger value="timeline">
          {{ t('projects.tabs.timeline') }}
        </TabsTrigger>
      </TabsList>
      <TabsContent value="overview" class="space-y-4">
        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle class="text-sm font-medium">
                {{ t('projects.dashboard.tasksOverview') }}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold">
                {{
                  t('projects.dashboard.tasksCompleted', {
                    completed: tasksCompleted,
                    total: totalTasks
                  })
                }}
              </div>
              <p class="text-xs text-muted-foreground">
                {{ activeTasks }} {{ t('projects.dashboard.activeTasks') }}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle class="text-sm font-medium">
                {{ t('projects.dashboard.budget') }}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold">
                ${{ project.budget?.toLocaleString() || '0' }}
              </div>
              <p class="text-xs text-muted-foreground">
                {{ t('projects.dashboard.budgetTotal') }}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle class="text-sm font-medium">
                {{ t('projects.dashboard.teamSize') }}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold">
                {{ teamMembers }}
              </div>
              <p class="text-xs text-muted-foreground">
                {{ t('projects.dashboard.activeMembers') }}
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle class="text-sm font-medium">
                {{ t('projects.dashboard.nextDeadline') }}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold">
                {{
                  nextDeadline ? new Date(nextDeadline).toLocaleDateString() : t('projects.dashboard.noDeadline')
                }}
              </div>
              <p class="text-xs text-muted-foreground">
                {{ nextDeadline ? t('projects.dashboard.daysRemaining', {days: 30}) : '' }}
              </p>
            </CardContent>
          </Card>
        </div>
        <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
          <Card class="col-span-4">
            <CardHeader>
              <CardTitle>{{ t('projects.dashboard.progressOverview') }}</CardTitle>
            </CardHeader>
            <CardContent class="pl-2">
              <!-- Placeholder for progress chart -->
              <p>{{ t('projects.dashboard.progressChartPlaceholder') }}</p>
            </CardContent>
          </Card>
          <Card class="col-span-3">
            <CardHeader>
              <CardTitle>{{ t('projects.dashboard.recentTasks') }}</CardTitle>
              <CardDescription>
                {{ t('projects.dashboard.recentTasksDescription') }}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <!-- Placeholder for recent tasks list -->
              <p>{{ t('projects.dashboard.recentTasksPlaceholder') }}</p>
            </CardContent>
          </Card>
        </div>
      </TabsContent>
      <TabsContent value="kanban">
        <p>{{ t('projects.content.kanban') }}</p>
      </TabsContent>
      <TabsContent value="gdd">
        <p>{{ t('projects.content.gdd') }}</p>
      </TabsContent>
      <TabsContent value="timeline">
        <p>{{ t('projects.content.timeline') }}</p>
      </TabsContent>
    </Tabs>
  </div>
  <div v-else class="flex justify-center items-center h-screen">
    <p>{{ t('projects.notFound') }}</p>
  </div>
</template>