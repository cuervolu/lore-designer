<script setup lang="ts">
import { onMounted } from 'vue'
import { useUpdaterStore } from '~/stores/updater.store'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog'

const updaterStore = useUpdaterStore()

onMounted(() => {
  updaterStore.checkForUpdates()
})
</script>

<template>
  <Dialog :open="updaterStore.isUpdateAvailable" @update:open="updaterStore.isUpdateAvailable = $event">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Update Available</DialogTitle>
        <DialogDescription>
          A new version ({{ updaterStore.updateVersion }}) of Lore Designer is available.
        </DialogDescription>
      </DialogHeader>
      <div v-if="updaterStore.updateNotes" class="mt-2 text-sm text-muted-foreground">
        <h4 class="font-semibold">What's new:</h4>
        <p>{{ updaterStore.updateNotes }}</p>
      </div>
      <DialogFooter>
        <Button @click="updaterStore.isUpdateAvailable = false">Later</Button>
        <Button @click="updaterStore.installUpdate" :disabled="updaterStore.isUpdating">
          {{ updaterStore.isUpdating ? 'Updating...' : 'Update Now' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>