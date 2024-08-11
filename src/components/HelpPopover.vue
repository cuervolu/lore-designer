<script setup lang="ts">
import {getVersion} from "@tauri-apps/api/app";
import {HelpCircle} from 'lucide-vue-next'
import {Popover, PopoverContent, PopoverTrigger} from '~/components/ui/popover'
import {Button} from '~/components/ui/button'
import {useUpdaterStore} from '~/stores/updater.store'
import {useToast} from "@/components/ui/toast";
import {open} from "@tauri-apps/plugin-shell";

const {t} = useI18n()
const updaterStore = useUpdaterStore()
const {toast} = useToast()
const appVersion = await getVersion()

const checkForUpdates = async () => {
  try {
    const updateAvailable = await updaterStore.checkForUpdates(true)
    if (!updateAvailable) {
      toast({
        title: t('helpPopover.noUpdatesTitle'),
        duration: 900,
      })
    }
  } catch (e) {
    toast({
      title: t('helpPopover.updateCheckError'),
      variant: 'destructive',
    })
  }
}

const reportBug = () => {
  open('https://github.com/cuervolu/lore-designer/issues/new')
}

const giveFeedback = () => {
  // TODO: Implement feedback form
}
</script>

<template>
  <Popover>
    <PopoverTrigger as-child>
      <Button variant="outline" size="icon" class="h-8 w-8 rounded-full">
        <HelpCircle class="h-4 w-4"/>
        <span class="sr-only">{{ t('helpPopover.open') }}</span>
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-80 mr-5">
      <div class="grid gap-2">
        <Button
            variant="ghost"
            size="sm"
            class="w-full justify-start"
            @click="checkForUpdates"
        >
          {{ t('helpPopover.checkUpdates') }}
        </Button>
        <Button
            variant="ghost"
            size="sm"
            class="w-full justify-start"
            @click="reportBug"
        >
          {{ t('helpPopover.reportBug') }}
        </Button>
        <Button
            variant="ghost"
            size="sm"
            class="w-full justify-start"
            @click="giveFeedback"
        >
          {{ t('helpPopover.giveFeedback') }}
        </Button>
        <div class="text-xs text-muted-foreground px-2 py-1">
          {{ t('helpPopover.version', {version: appVersion}) }}
        </div>
      </div>
    </PopoverContent>
  </Popover>
</template>