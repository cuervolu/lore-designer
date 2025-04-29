<!-- apps/desktop/src/modules/wizard/views/NewWorkspaceView.vue -->
<script setup lang="ts">
import {invoke} from '@tauri-apps/api/core'
import {open} from '@tauri-apps/plugin-dialog'
import {debug, error, info} from '@tauri-apps/plugin-log'

import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {ChevronLeft} from 'lucide-vue-next'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'

import {Card, CardContent, CardFooter, CardHeader, CardTitle} from '@/components/ui/card'
import {usePreferencesStore} from '@common/stores/preferences.store'
import {useRecentWorkspacesStore} from "@wizard/stores/recent-workspaces.store.ts";

const router = useRouter()
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

    const selectedPath = await open({directory: true, title: 'Select Workspace Directory'})

    if (selectedPath) {
      workspacePath.value = selectedPath
      await debug(`Selected directory: ${selectedPath}`)
    } else {
      await debug('Directory selection canceled by user')
    }
  } catch (err) {
    await error(`Error selecting directory: ${err}`)
    errorMessage.value = `Failed to select directory: ${err}`
  } finally {
    isBrowsing.value = false
  }
}

const createWorkspace = async () => {
  if (!workspaceName.value || !workspacePath.value) {
    errorMessage.value = 'Please provide both a name and location'
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
        return // Return early to avoid goBack()
      }
    }

    goBack()
  } catch (err) {
    await error(`Failed to create workspace: ${err}`)
    errorMessage.value = `Failed to create workspace: ${err}`
  } finally {
    isCreating.value = false
  }
}
</script>

<template>
  <div class="container mx-auto p-8 max-w-2xl">
    <header class="mb-6">
      <Button variant="ghost" @click="goBack" class="mb-2">
        <chevron-left class="w-4 h-4 mr-2"/>
        Back
      </Button>
      <h1 class="text-2xl font-bold">Create a New Workspace</h1>
      <p class="text-muted-foreground">Set up a new workspace for your project</p>
    </header>

    <Card>
      <CardHeader>
        <CardTitle>Workspace Details</CardTitle>
      </CardHeader>
      <CardContent>
        <form class="space-y-4" @submit.prevent="createWorkspace">
          <div class="space-y-2">
            <Label for="workspace-name">Workspace Name</Label>
            <Input
              id="workspace-name"
              v-model="workspaceName"
              placeholder="The Gray Garden"
              required
            />
            <p class="text-xs text-muted-foreground mt-1">
              The display name for your workspace. Spaces and special characters will be converted
              to underscores for the folder name.
            </p>
          </div>

          <div class="space-y-2">
            <Label for="workspace-location">Location</Label>
            <div class="flex gap-2">
              <Input
                id="workspace-location"
                v-model="workspacePath"
                placeholder="Select a folder"
                required
                readonly
              />
              <Button
                variant="outline"
                type="button"
                @click="browseDirectory"
                :disabled="isBrowsing"
              >
                <span v-if="isBrowsing">Browsing...</span>
                <span v-else>Browse</span>
              </Button>
            </div>
          </div>

          <div class="flex items-center space-x-2">
            <input
              id="open-after-creation"
              type="checkbox"
              v-model="openAfterCreation"
              class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
            />
            <Label for="open-after-creation">Open in editor after creation</Label>
          </div>

          <div v-if="errorMessage" class="text-destructive text-sm mt-2">
            {{ errorMessage }}
          </div>
        </form>
      </CardContent>
      <CardFooter class="flex justify-end gap-2">
        <Button variant="outline" @click="goBack" :disabled="isCreating">Cancel</Button>
        <Button
          @click="createWorkspace"
          :disabled="!workspaceName || !workspacePath || isCreating || isBrowsing"
          :class="{ 'opacity-50 cursor-not-allowed': isCreating }"
        >
          <span v-if="isCreating">Creating...</span>
          <span v-else>Create Workspace</span>
        </Button>
      </CardFooter>
    </Card>
  </div>
</template>
