<script setup lang="ts">
import {invoke} from '@tauri-apps/api/core'
import {getVersion} from '@tauri-apps/api/app'
import {appLogDir} from '@tauri-apps/api/path'
import {error} from "@tauri-apps/plugin-log";
import {open} from '@tauri-apps/plugin-shell'
import {writeText} from '@tauri-apps/plugin-clipboard-manager'
import {ref, onMounted} from 'vue'
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger
} from '@/components/ui/accordion'
import {Button} from '@/components/ui/button'
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent
} from '@/components/ui/card'
import {Input} from '@/components/ui/input'
import {Search} from 'lucide-vue-next'
import {useToast} from "@/components/ui/toast"

const {t} = useI18n()

const faqQuestions = computed(() => {
  const questions = t('help.faq.questions') as unknown as Record<string, { q: string; a: string }>
  return Object.entries(questions).map(([key, value]) => ({
    key,
    ...value
  }))
})

const {toast} = useToast()
const appVersion = ref('')
const systemInfo = ref('')
const logPath = ref('')
const searchQuery = ref('')

const copyToClipboard = async () => {
  const text = `${t('help.reportBug.sysInfo.appVersion')}: ${appVersion.value}\n${systemInfo.value}\n${t('help.reportBug.sysInfo.logDirectory')}: ${logPath.value}`
  try {
    await writeText(text)
    toast({
      title: t('help.toasts.copySuccess.title'),
      description: t('help.toasts.copySuccess.description'),
    })
  } catch (e) {
    await error(`Failed to copy to clipboard: ${e}`)
    toast({
      title: t('help.toasts.copyError.title'),
      description: t('help.toasts.copyError.description'),
      variant: "destructive",
    })
  }
}

const openLogDirectory = async () => {
  try {
    await open(logPath.value)
  } catch (e) {
    await error(`Failed to open log directory: ${e}`)
    toast({
      title: t('help.toasts.openLogError.title'),
      description: t('help.toasts.openLogError.description'),
      variant: "destructive",
    })
  }
}

const openGitHubIssues = () => {
  open('https://github.com/cuervolu/lore-designer/issues/new')
}

onMounted(async () => {
  try {
    console.log('Translations:', t('help.faq.questions'))
    appVersion.value = await getVersion()
    systemInfo.value = await invoke('get_system_info')
    logPath.value = await appLogDir()
  } catch (e) {
    await error(`Failed to load system information: ${e}`)
    toast({
      title: t('help.toasts.loadInfoError.title'),
      description: t('help.toasts.loadInfoError.description'),
      variant: "destructive",
    })
  }
})
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">{{ t('help.title') }}</h1>

    <div class="mb-6">
      <div class="relative">
        <Search class="absolute left-2 top-1/2 transform -translate-y-1/2 text-gray-400"/>
        <Input
            v-model="searchQuery"
            :placeholder="t('help.searchPlaceholder')"
            class="pl-10"
        />
      </div>
    </div>

    <Accordion type="single" collapsible class="w-full mb-8">
      <AccordionItem value="usage">
        <AccordionTrigger>{{ t('help.usage.title') }}</AccordionTrigger>
        <AccordionContent>
          <p>{{ t('help.usage.intro') }}</p>
          <ol class="list-decimal list-inside mt-2">
            <li>{{ t('help.usage.steps.1') }}</li>
            <li>{{ t('help.usage.steps.2') }}</li>
            <li>{{ t('help.usage.steps.3') }}</li>
            <li>{{ t('help.usage.steps.4') }}</li>
            <li>{{ t('help.usage.steps.5') }}</li>
          </ol>
          <p class="mt-4">{{ t('help.usage.moreInfo', [t('help.usage.userGuide')]) }}</p>
        </AccordionContent>
      </AccordionItem>

      <AccordionItem value="report-bug">
        <AccordionTrigger>{{ t('help.reportBug.title') }}</AccordionTrigger>
        <AccordionContent>
          <p class="mb-4">{{ t('help.reportBug.intro') }}</p>
          <ol class="list-decimal list-inside mb-4">
            <li>{{ t('help.reportBug.steps.1') }}</li>
            <li>{{ t('help.reportBug.steps.2') }}</li>
            <li>{{ t('help.reportBug.steps.3') }}</li>
            <li>{{ t('help.reportBug.steps.4') }}</li>
            <li>{{ t('help.reportBug.steps.5') }}</li>
          </ol>
          <Card>
            <CardHeader>
              <CardTitle>{{ t('help.reportBug.sysInfo.title') }}</CardTitle>
              <CardDescription>{{ t('help.reportBug.sysInfo.description') }}</CardDescription>
            </CardHeader>
            <CardContent>
              <div class="space-y-2">
                <p><strong>{{ t('help.reportBug.sysInfo.appVersion') }}:</strong> {{ appVersion }}
                </p>
                <p v-for="line in systemInfo.split('\n')" :key="line">{{ line }}</p>
                <p><strong>{{ t('help.reportBug.sysInfo.logDirectory') }}:</strong> {{ logPath }}
                </p>
              </div>
              <div class="mt-4 space-x-2">
                <Button @click="copyToClipboard">{{ t('help.reportBug.buttons.copy') }}</Button>
                <Button @click="openLogDirectory">{{ t('help.reportBug.buttons.openLog') }}</Button>
                <Button @click="openGitHubIssues">{{ t('help.reportBug.buttons.report') }}</Button>
              </div>
            </CardContent>
          </Card>
        </AccordionContent>
      </AccordionItem>

      <AccordionItem value="faq">
        <AccordionTrigger>{{ t('help.faq.title') }}</AccordionTrigger>
        <AccordionContent>
          <div class="space-y-4">
            <div v-for="question in faqQuestions" :key="question.key">
              <h3 class="font-semibold">{{ question.q }}</h3>
              <p>{{ question.a }}</p>
            </div>
          </div>
        </AccordionContent>
      </AccordionItem>

      <AccordionItem value="tips">
        <AccordionTrigger>{{ t('help.tips.title') }}</AccordionTrigger>
        <AccordionContent>
          <ul class="list-disc list-inside space-y-2">
            <li>{{ t('help.tips.list.0') }}</li>
            <li>{{ t('help.tips.list.1') }}</li>
            <li>{{ t('help.tips.list.2') }}</li>
            <li>{{ t('help.tips.list.3') }}</li>
            <li>{{ t('help.tips.list.4') }}</li>
          </ul>
        </AccordionContent>
      </AccordionItem>
    </Accordion>

    <div class="mt-8">
      <h2 class="text-2xl font-bold mb-4">{{ t('help.moreHelp.title') }}</h2>
      <p>{{ t('help.moreHelp.description') }}</p>
      <ul class="list-disc list-inside mt-2">
        <li>{{ t('help.moreHelp.contact.email') }}</li>
        <li>{{ t('help.moreHelp.contact.twitter') }}</li>
      </ul>
    </div>
  </div>
</template>