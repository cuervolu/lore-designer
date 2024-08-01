<script setup lang="ts">
import { invoke } from "@tauri-apps/api/core";
import { getVersion } from '@tauri-apps/api/app'
import { ref, onMounted, computed } from 'vue'
import { Book, GithubIcon, TwitterIcon } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'

const version = ref('')
const releaseNotes = ref('')
const isLoading = ref(true)
const isDialogOpen = ref(false)
const repoUrl = 'https://github.com/cuervolu/lore-designer'
const authorUrl = 'https://bento.me/cuervolu'

const initialChangelogLines = 10

const changelogLines = computed(() => releaseNotes.value.split('\n'))
const initialChangelog = computed(() => changelogLines.value.slice(0, initialChangelogLines).join('\n'))
const hasMoreChangelog = computed(() => changelogLines.value.length > initialChangelogLines)

const fetchVersion = async () => {
  version.value = await getVersion()
}

const fetchReleaseNotes = async () => {
  try {
    releaseNotes.value = await invoke('get_release_notes')
  } catch (error) {
    console.error('Error fetching release notes:', error)
    releaseNotes.value = 'Unable to fetch release notes.'
  } finally {
    isLoading.value = false
  }
}

const openBrowser = async (url: string) => {
  try {
    await invoke('open_browser', { url })
  } catch (error) {
    console.error('Error opening browser:', error)
  }
}

onMounted(async () => {
  await fetchVersion()
  await fetchReleaseNotes()
})
</script>

<template>
  <div class="flex items-center justify-center min-h-[100dvh] bg-gradient-to-b from-background to-muted px-4 py-12 sm:px-6 lg:px-8">
    <Card class="w-full max-w-3xl">
      <CardHeader>
        <div class="flex flex-col items-center justify-center mb-6">
          <img src="~/assets/img/lore-designer.svg" alt="App Logo" class="w-24 h-auto mb-4">
          <CardTitle class="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">Lore Designer</CardTitle>
          <CardDescription>Version {{ version }}</CardDescription>
        </div>
      </CardHeader>
      <CardContent class="space-y-8">
        <div class="space-y-4">
          <div v-if="!isLoading" class="prose max-w-none dark:prose-invert">
            <MDC :value="initialChangelog" />
            <Dialog v-model:open="isDialogOpen" v-if="hasMoreChangelog">
              <DialogTrigger asChild>
                <Button variant="outline" class="mt-4">View Full Changelog</Button>
              </DialogTrigger>
              <DialogContent class="max-w-3xl max-h-[80vh] overflow-y-auto">
                <DialogHeader>
                  <DialogTitle>Changelog</DialogTitle>
                </DialogHeader>
                <MDC :value="releaseNotes" />
              </DialogContent>
            </Dialog>
          </div>
          <div v-else class="space-y-2">
            <Skeleton class="h-4 w-full" />
            <Skeleton class="h-4 w-[90%]" />
            <Skeleton class="h-4 w-[80%]" />
            <Skeleton class="h-4 w-[70%]" />
          </div>
        </div>
        <div class="space-y-4">
          <h2 class="text-2xl font-bold tracking-tight text-foreground">Useful Links</h2>
          <div class="flex flex-wrap gap-4 justify-center">
            <Button @click="openBrowser(repoUrl)" variant="outline" class="flex items-center px-4 py-2 rounded-full transition-colors hover:bg-primary hover:text-primary-foreground">
              <Book class="mr-2 h-4 w-4"/>
              Documentation
            </Button>
            <Button @click="openBrowser(repoUrl)" variant="outline" class="flex items-center px-4 py-2 rounded-full transition-colors hover:bg-primary hover:text-primary-foreground">
              <GithubIcon class="mr-2 h-4 w-4"/>
              GitHub
            </Button>
            <Button @click="openBrowser(repoUrl)" variant="outline" class="flex items-center px-4 py-2 rounded-full transition-colors hover:bg-primary hover:text-primary-foreground">
              <TwitterIcon class="mr-2 h-4 w-4"/>
              Twitter
            </Button>
          </div>
        </div>
      </CardContent>
      <CardFooter class="flex justify-center">
        <p class="text-sm text-muted-foreground">
          Made with ❤️ by
          <a
              @click="openBrowser(authorUrl)"
              class="font-medium text-primary hover:underline cursor-pointer"
          >
            Cuervolu
          </a>
        </p>
      </CardFooter>
    </Card>
  </div>
</template>

<style scoped>
:deep(.prose) {
  @apply text-foreground;
}
:deep(.prose h1, .prose h2, .prose h3) {
  @apply text-foreground;
}
:deep(.prose p, .prose li) {
  @apply text-muted-foreground;
}
</style>