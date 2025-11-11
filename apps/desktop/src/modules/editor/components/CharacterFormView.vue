<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { parse, stringify } from 'yaml'
import { error as logError } from '@tauri-apps/plugin-log'
import { useEditorStore } from '@editor/stores/editor.store'
import { useAutoSave } from '@editor/composables/useAutoSave'
import type { EditorFile } from '@editor/types/editor.types'
import { useFormConfig } from '@editor/composables/useFormConfig'

import MilkdownEditor from '@editor/components/MilkdownEditor.vue'
import StatusFooter from '@editor/components/StatusFooter.vue'
import DynamicField from './dynamic-form/DynamicField.vue'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Separator } from '@/components/ui/separator'
import { Skeleton } from '@/components/ui/skeleton'

const props = defineProps<{
  file: EditorFile
}>()

const editorStore = useEditorStore()
const { saveNow, isSaving } = useAutoSave()
const {
  config,
  isLoading: isConfigLoading,
  error: configError,
  loadConfig,
} = useFormConfig()

const formData = ref<Record<string, unknown>>({})
const bodyContent = ref('')
const isUpdatingFromStore = ref(false)

function parseFileContent(content: string) {
  isUpdatingFromStore.value = true
  try {
    const parts = content.split('---')
    if (parts.length >= 3 && parts[0] === '') {
      const yamlBlock = parts[1]
      formData.value = parse(yamlBlock) || {}
      bodyContent.value = parts.slice(2).join('---').trim()
    }
    else {
      formData.value = {}
      bodyContent.value = content
    }
  }
  catch (e) {
    logError(`Failed to parse frontmatter: ${e}`)
    formData.value = {}
    bodyContent.value = content
  }
  finally {
    setTimeout(() => (isUpdatingFromStore.value = false), 10)
  }
}

function saveChanges() {
  if (isUpdatingFromStore.value)
    return

  try {
    const yamlBlock = stringify(formData.value)
    const newContent = `---\n${yamlBlock}---\n\n${bodyContent.value}`
    saveNow(props.file, newContent)
  }
  catch (e) {
    logError(`Failed to stringify frontmatter: ${e}`)
  }
}

onMounted(() => {
  loadConfig()
})

watch(() => props.file, (newFile) => {
  if (newFile)
    parseFileContent(newFile.content)
}, { immediate: true })

watch(formData, saveChanges, { deep: true })
watch(bodyContent, saveChanges)
</script>

<template>
  <div class="flex flex-col h-full bg-card">
    <ScrollArea class="flex-1">
      <div class="p-6 space-y-6">
        <div v-if="isConfigLoading" class="space-y-4">
          <Skeleton class="h-8 w-1/3" />
          <Skeleton class="h-10 w-full" />
          <Skeleton class="h-8 w-1/3" />
          <Skeleton class="h-24 w-full" />
        </div>

        <div v-else-if="configError" class="text-destructive">
          Failed to load form configuration: {{ configError }}
        </div>

        <div v-else-if="config" class="space-y-4">
          <DynamicField
            v-for="field in config.fields"
            :key="field.key"
            :field-definition="field"
            v-model="formData[field.key]"
          />
        </div>

        <Separator />

        <div class="prose prose-sm dark:prose-invert max-w-none">
          <MilkdownEditor v-model="bodyContent" />
        </div>
      </div>
    </ScrollArea>

  </div>
</template>
