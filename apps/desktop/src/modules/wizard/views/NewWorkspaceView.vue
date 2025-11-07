<script setup lang="ts">
import {invoke} from '@tauri-apps/api/core'
import {open} from '@tauri-apps/plugin-dialog'
import {error, info} from '@tauri-apps/plugin-log'
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import { useI18n } from 'vue-i18n'
import {Card, CardContent, CardHeader, CardTitle} from '@/components/ui/card'
import {usePreferencesStore} from '@common/stores/preferences.store'
import {useRecentWorkspacesStore} from "@wizard/stores/recent-workspaces.store"
import NewWorkspaceWizard from '@wizard/components/NewWorkspaceWizard.vue'

const router = useRouter()
const { t } = useI18n()
const preferencesStore = usePreferencesStore()
const recentWorkspacesStore = useRecentWorkspacesStore()

const workspaceName = ref('')
const workspacePath = ref('')
const isCreating = ref(false)
const isBrowsing = ref(false)
const errorMessage = ref('')
const openAfterCreation = ref(true)

const goBack = () => {
  router.push({name: 'workspaces'})
}

const browseDirectory = async () => {
  try {
    isBrowsing.value = true
    errorMessage.value = ''

    const selectedPath = await open({directory: true, title: t('wizard.new.browseTitle')})

    if (selectedPath) {
      workspacePath.value = selectedPath
    }
  } catch (err) {
    await error(`Error selecting directory: ${err}`)
    errorMessage.value = t('wizard.new.errors.selectFailed', { err: String(err) })
  } finally {
    isBrowsing.value = false
  }
}

const createWorkspace = async () => {
  if (!workspaceName.value || !workspacePath.value) {
    errorMessage.value = t('wizard.new.errors.missingFields')
    return
  }

  try {
    isCreating.value = true
    errorMessage.value = ''

    const result = await invoke<{ path: string, message: string }>('create_workspace', {
      name: workspaceName.value,
      locationPath: workspacePath.value
    })

    await info(`Workspace created: ${result.message}`)

    if (result.path) {
      await preferencesStore.updateLastProject(result.path)
      await recentWorkspacesStore.addRecentWorkspace(workspaceName.value, result.path)

      if (openAfterCreation.value) {
        await router.push({
          name: 'editor',
          query: {path: result.path}
        })
        return
      }
    }

    goBack()
  } catch (err) {
    await error(`Failed to create workspace: ${err}`)
    errorMessage.value = t('wizard.new.errors.createFailed', { err: String(err) })
  } finally {
    isCreating.value = false
  }
}
</script>

<template>
  <div class="container mx-auto p-8 max-w-4xl">
    <header class="mb-8 text-center">
      <h1 class="text-3xl font-bold mb-2">{{ $t('welcome.routeTitles.newWorkspace') }}</h1>
      <p class="text-muted-foreground">{{ $t('wizard.new.description') }}</p>
    </header>

    <Card class="shadow-lg">
      <CardHeader>
        <CardTitle>Create New Workspace</CardTitle>
      </CardHeader>
      <CardContent>
        <NewWorkspaceWizard
          v-model:workspace-name="workspaceName"
          v-model:workspace-path="workspacePath"
          v-model:open-after-creation="openAfterCreation"
          :is-creating="isCreating"
          :is-browsing="isBrowsing"
          :error-message="errorMessage"
          @browse="browseDirectory"
          @create="createWorkspace"
          @cancel="goBack"
        />
      </CardContent>
    </Card>
  </div>
</template>
