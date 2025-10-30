<script setup lang="ts">
import {invoke} from '@tauri-apps/api/core'
import {open} from '@tauri-apps/plugin-dialog'
import {debug, error, info} from '@tauri-apps/plugin-log'

import {ref} from 'vue'
import {useRouter} from 'vue-router'
import { useI18n } from 'vue-i18n'
import {ChevronLeft} from 'lucide-vue-next'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'

import {Card, CardContent, CardFooter, CardHeader, CardTitle} from '@/components/ui/card'
import {usePreferencesStore} from '@common/stores/preferences.store'
import {useRecentWorkspacesStore} from "@wizard/stores/recent-workspaces.store.ts";

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
      await debug(`Selected directory: ${selectedPath}`)
    } else {
      await debug('Directory selection canceled by user')
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
        return // Return early to avoid goBack()
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
  <div class="container mx-auto p-8 max-w-2xl">
    <header class="mb-6">
      <Button variant="ghost" @click="goBack" class="mb-2">
        <chevron-left class="w-4 h-4 mr-2"/>
        {{ $t('common.back') }}
      </Button>
      <h1 class="text-2xl font-bold">{{ $t('welcome.routeTitles.newWorkspace') }}</h1>
      <p class="text-muted-foreground">{{ $t('wizard.new.description') }}</p>
    </header>

    <Card>
      <CardHeader>
        <CardTitle>{{ $t('wizard.new.detailsTitle') }}</CardTitle>
      </CardHeader>
      <CardContent>
        <form class="space-y-4" @submit.prevent="createWorkspace">
          <div class="space-y-2">
            <Label for="workspace-name">{{ $t('wizard.new.nameLabel') }}</Label>
            <Input
              id="workspace-name"
              v-model="workspaceName"
              :placeholder="$t('wizard.new.namePlaceholder')"
              required
            />
            <p class="text-xs text-muted-foreground mt-1">
              {{ $t('wizard.new.nameDescription') }}
            </p>
          </div>

          <div class="space-y-2">
            <Label for="workspace-location">{{ $t('wizard.new.locationLabel') }}</Label>
            <div class="flex gap-2">
              <Input
                id="workspace-location"
                v-model="workspacePath"
                :placeholder="$t('wizard.new.locationPlaceholder')"
                required
                readonly
              />
              <Button
                variant="outline"
                type="button"
                @click="browseDirectory"
                :disabled="isBrowsing"
              >
                <span v-if="isBrowsing">{{ $t('wizard.new.browsing') }}</span>
                <span v-else>{{ $t('common.browse') }}</span>
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
            <Label for="open-after-creation">{{ $t('wizard.new.openAfterCreationLabel') }}</Label>
          </div>

          <div v-if="errorMessage" class="text-destructive text-sm mt-2">
            {{ errorMessage }}
          </div>
        </form>
      </CardContent>
      <CardFooter class="flex justify-end gap-2">
        <Button variant="outline" @click="goBack" :disabled="isCreating">{{ $t('common.cancel') }}</Button>
        <Button
          @click="createWorkspace"
          :disabled="!workspaceName || !workspacePath || isCreating || isBrowsing"
          :class="{ 'opacity-50 cursor-not-allowed': isCreating }"
        >
          <span v-if="isCreating">{{ $t('wizard.new.creating') }}</span>
          <span v-else>{{ $t('wizard.new.createButton') }}</span>
        </Button>
      </CardFooter>
    </Card>
  </div>
</template>
