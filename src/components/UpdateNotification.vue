<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useUpdaterStore } from '~/stores/updater.store'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog'

const updaterStore = useUpdaterStore()
const maxChangelogHeight = ref(300) // Maximum height for the changelog in pixels

onMounted(() => {
  updaterStore.checkForUpdates()
})
</script>

<template>
  <Dialog :open="updaterStore.isUpdateAvailable" @update:open="updaterStore.isUpdateAvailable = $event">
    <DialogContent class="sm:max-w-[500px]">
      <DialogHeader>
        <DialogTitle>Update Available</DialogTitle>
        <DialogDescription>
          A new version ({{ updaterStore.updateVersion }}) of Lore Designer is available.
        </DialogDescription>
      </DialogHeader>
      <div v-if="updaterStore.updateNotes" class="mt-4">
        <h4 class="text-sm font-semibold mb-2">What's new:</h4>
        <div :style="{ maxHeight: `${maxChangelogHeight}px`, overflowY: 'auto' }" class="pr-2">
          <div class="text-sm text-muted-foreground prose prose-sm dark:prose-invert">
            <MDC :value="updaterStore.updateNotes" />
          </div>
        </div>
      </div>
      <DialogFooter class="mt-6">
        <Button variant="outline" @click="updaterStore.isUpdateAvailable = false">Later</Button>
        <Button @click="updaterStore.installUpdate" :disabled="updaterStore.isUpdating">
          {{ updaterStore.isUpdating ? 'Updating...' : 'Update Now' }}
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