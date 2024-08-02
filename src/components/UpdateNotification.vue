<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useUpdaterStore } from '~/stores/updater.store'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog'

const updaterStore = useUpdaterStore()
const maxChangelogHeight = ref(300) // Maximum height for the changelog in pixels

const { t } = useI18n()

const isDev = computed(() => import.meta.dev)

const showDialog = computed(() => isDev.value ? true : updaterStore.isUpdateAvailable)

const closeDialog = () => {
  if (!isDev.value) {
    updaterStore.isUpdateAvailable = false
  }
}

onMounted(() => {
  if (!isDev.value) {
    updaterStore.checkForUpdates()
  } else {
    // Simulate update data for development
    updaterStore.isUpdateAvailable = true
    updaterStore.updateVersion = '0.2.0'
    updaterStore.updateNotes = `
# Version 0.2.0

## New Features
- Added GlobalCommand component for enhanced navigation and actions
- Updated help page with system information module

## Improvements
- Improved button layout in character details view
- Updated character buttons with translated text

## Bug Fixes
- Fixed missing translations for characters

## Other Changes
- Updated npm dependencies to latest versions
- Updated clipboard manager capabilities
    `
  }
})
</script>

<template>
  <Dialog :open="showDialog" @update:open="closeDialog">
    <DialogContent class="sm:max-w-[500px]">
      <DialogHeader>
        <DialogTitle>{{ t('updateNotification.title') }}</DialogTitle>
        <DialogDescription>
          {{ t('updateNotification.description', { version: updaterStore.updateVersion }) }}
        </DialogDescription>
      </DialogHeader>
      <div v-if="updaterStore.updateNotes" class="mt-4">
        <h4 class="text-sm font-semibold mb-2">{{ t('updateNotification.whatsNew') }}</h4>
        <div :style="{ maxHeight: `${maxChangelogHeight}px`, overflowY: 'auto' }" class="pr-2">
          <div class="text-sm text-muted-foreground prose prose-sm dark:prose-invert">
            <MDC :value="updaterStore.updateNotes" />
          </div>
        </div>
      </div>
      <DialogFooter class="mt-6">
        <Button variant="outline" @click="closeDialog">
          {{ t('updateNotification.later') }}
        </Button>
        <Button @click="updaterStore.installUpdate" :disabled="updaterStore.isUpdating">
          {{ updaterStore.isUpdating ? t('updateNotification.updating') : t('updateNotification.updateNow') }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>


<style scoped>
.prose :deep(h1, h2, h3, h4, h5, h6) {
  margin-top: 1em;
  margin-bottom: 0.5em;
}

.prose :deep(p, ul, ol) {
  margin-bottom: 0.5em;
}

.prose :deep(ul, ol) {
  padding-left: 1.5em;
}

.prose :deep(li) {
  margin-bottom: 0.25em;
}

.prose :deep(code) {
  @apply bg-muted px-1 py-0.5 rounded text-sm;
}

.prose :deep(pre) {
  @apply bg-muted p-2 rounded overflow-x-auto;
}

.prose :deep(a) {
  @apply text-primary hover:underline;
}

/* Custom scrollbar styles */
div[style*="overflow-y: auto"] {
  scrollbar-width: thin;
  scrollbar-color: rgba(155, 155, 155, 0.5) transparent;
}

div[style*="overflow-y: auto"]::-webkit-scrollbar {
  width: 6px;
}

div[style*="overflow-y: auto"]::-webkit-scrollbar-track {
  background: transparent;
}

div[style*="overflow-y: auto"]::-webkit-scrollbar-thumb {
  background-color: rgba(155, 155, 155, 0.5);
  border-radius: 20px;
  border: transparent;
}
</style>