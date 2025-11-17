<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Button } from '@/components/ui/button'
import { AlertCircle, Info, ChevronUp, X, Trash2 } from 'lucide-vue-next'
import { DynamicScroller, DynamicScrollerItem } from 'vue-virtual-scroller'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import {
  listenToLogs,
  getHistory,
  clearHistory,
  type LogEntry as PluginLogEntry
} from 'tauri-plugin-tracing'

const { t } = useI18n()

const activeTab = ref('console')
const consoleScrollerRef = ref<InstanceType<typeof DynamicScroller> | null>(null)
const problemsScrollerRef = ref<InstanceType<typeof DynamicScroller> | null>(null)
const outputScrollerRef = ref<InstanceType<typeof DynamicScroller> | null>(null)
let unlisten: (() => void) | null = null

type MessageType = 'ERROR' | 'DEBUG' | 'INFO' | 'WARNING'

interface LogMessage {
  id: string
  timestamp: string
  type: MessageType
  message: string
  source?: string
}

interface Problem {
  id: string
  type: 'error' | 'warning'
  message: string
  file?: string
  line?: number
}

interface OutputMessage {
  id: string
  timestamp: string
  message: string
}

const consoleMessages = ref<LogMessage[]>([])
const problems = ref<Problem[]>([])
const outputs = ref<OutputMessage[]>([])

let messageIdCounter = 0
const generateId = () => `msg-${Date.now()}-${messageIdCounter++}`

const scrollToBottom = async () => {
  await nextTick()

  let scroller: InstanceType<typeof DynamicScroller> | null = null

  if (activeTab.value === 'console') {
    scroller = consoleScrollerRef.value
  } else if (activeTab.value === 'problems') {
    scroller = problemsScrollerRef.value
  } else if (activeTab.value === 'output') {
    scroller = outputScrollerRef.value
  }

  if (scroller) {
    try {
      scroller.scrollToBottom()
    } catch (e) {
      const itemCount =
        activeTab.value === 'console' ? consoleMessages.value.length :
        activeTab.value === 'problems' ? problems.value.length :
        outputs.value.length

      if (itemCount > 0) {
        scroller.scrollToItem(itemCount - 1)
      }
    }
  }
}

const transformLogEntry = (entry: PluginLogEntry): LogMessage => {
  let messageType: MessageType
  switch (entry.level.toUpperCase()) {
    case 'ERROR':
      messageType = 'ERROR'
      break
    case 'WARN':
      messageType = 'WARNING'
      break
    case 'INFO':
      messageType = 'INFO'
      break
    case 'DEBUG':
    case 'TRACE':
      messageType = 'DEBUG'
      break
    default:
      messageType = 'INFO'
  }

  return {
    id: generateId(),
    timestamp: entry.timestamp,
    type: messageType,
    message: entry.message,
    source: entry.target
  }
}

const addConsoleMessage = (type: MessageType, message: string, source?: string) => {
  const newMessage: LogMessage = {
    id: generateId(),
    timestamp: new Date().toLocaleTimeString(),
    type,
    message,
    source
  }

  consoleMessages.value.push(newMessage)

  if (type === 'ERROR' || type === 'WARNING') {
    problems.value.push({
      id: generateId(),
      type: type === 'ERROR' ? 'error' : 'warning',
      message,
      file: source,
      line: undefined
    })

    if (problems.value.length > 50) {
      problems.value.shift()
    }
  }

  if (consoleMessages.value.length > 100) {
    consoleMessages.value.shift()
  }

  scrollToBottom()
}

const addOutput = (message: string) => {
  outputs.value.push({
    id: generateId(),
    timestamp: new Date().toLocaleTimeString(),
    message
  })

  if (outputs.value.length > 100) {
    outputs.value.shift()
  }

  scrollToBottom()
}

const clearCurrentTab = async () => {
  if (activeTab.value === 'console') {
    consoleMessages.value = []
    await clearHistory()
  } else if (activeTab.value === 'problems') {
    problems.value = []
  } else if (activeTab.value === 'output') {
    outputs.value = []
  }
}

const consoleSizeDependencies = computed(() =>
  consoleMessages.value.map(m => m.message.length)
)
const problemsSizeDependencies = computed(() =>
  problems.value.map(p => p.message.length)
)
const outputsSizeDependencies = computed(() =>
  outputs.value.map(o => o.message.length)
)

onMounted(async () => {
  try {
    const history = await getHistory()
    consoleMessages.value = history.map(transformLogEntry)

    consoleMessages.value.forEach(msg => {
      if (msg.type === 'ERROR' || msg.type === 'WARNING') {
        problems.value.push({
          id: generateId(),
          type: msg.type === 'ERROR' ? 'error' : 'warning',
          message: msg.message,
          file: msg.source,
          line: undefined
        })
      }
    })

    scrollToBottom()
  } catch (e) {
    console.error('Failed to load log history:', e)
    addConsoleMessage('ERROR', `Failed to load log history: ${e}`, 'ConsolePanel')
  }

  try {
    unlisten = await listenToLogs((event) => {
      const newEntries = event.payload.map(transformLogEntry)

      newEntries.forEach(entry => {
        consoleMessages.value.push(entry)

        if (entry.type === 'ERROR' || entry.type === 'WARNING') {
          problems.value.push({
            id: generateId(),
            type: entry.type === 'ERROR' ? 'error' : 'warning',
            message: entry.message,
            file: entry.source,
            line: undefined
          })
        }
      })

      if (consoleMessages.value.length > 100) {
        consoleMessages.value = consoleMessages.value.slice(-100)
      }

      if (problems.value.length > 50) {
        problems.value = problems.value.slice(-50)
      }

      scrollToBottom()
    })
  } catch (e) {
    console.error('Failed to listen to logs:', e)
    addConsoleMessage('ERROR', `Failed to listen to logs: ${e}`, 'ConsolePanel')
  }
})

onUnmounted(() => {
  if (unlisten) {
    unlisten()
  }
})

const emit = defineEmits(['close'])

const handleClose = () => {
  emit('close')
}
</script>

<template>
  <div class="border-t h-48 flex flex-col">
    <div class="flex items-center border-b bg-muted/30">
      <div class="flex">
        <button
          @click="activeTab = 'console'"
          class="px-4 py-2 text-sm border-r hover:bg-muted/50"
          :class="{ 'bg-muted/50 font-medium': activeTab === 'console' }"
        >
          {{ t('console.tabs.console') }}
        </button>
        <button
          @click="activeTab = 'problems'"
          class="px-4 py-2 text-sm border-r hover:bg-muted/50"
          :class="{ 'bg-muted/50 font-medium': activeTab === 'problems' }"
        >
          {{ t('console.tabs.problems') }}
        </button>
        <button
          @click="activeTab = 'output'"
          class="px-4 py-2 text-sm hover:bg-muted/50"
          :class="{ 'bg-muted/50 font-medium': activeTab === 'output' }"
        >
          {{ t('console.tabs.output') }}
        </button>
      </div>

      <div class="ml-auto flex items-center">
        <Button
          @click="clearCurrentTab"
          variant="ghost"
          size="sm"
          class="h-7 w-7 p-0"
          :title="t('console.actions.clear')"
        >
          <Trash2 class="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          class="h-7 w-7 p-0"
          :title="t('console.actions.maximize')"
        >
          <ChevronUp class="h-4 w-4" />
        </Button>
        <Button
          @click="handleClose"
          variant="ghost"
          size="sm"
          class="h-7 w-7 p-0"
          :title="t('console.actions.close')"
        >
          <X class="h-4 w-4" />
        </Button>
      </div>
    </div>

    <DynamicScroller
      v-if="activeTab === 'console'"
      ref="consoleScrollerRef"
      :items="consoleMessages"
      :min-item-size="40"
      class="flex-1 font-mono text-xs"
      key-field="id"
    >
      <template #default="{ item, index, active }">
        <DynamicScrollerItem
          :item="item"
          :active="active"
          :data-index="index"
          :size-dependencies="[item.message.length]"
          class="p-1"
        >
          <div class="my-1 border-b border-muted/30 pb-1">
            <span class="text-muted-foreground">{{ item.timestamp }}</span>
            <span v-if="item.source" class="ml-2 text-purple-400">[{{ item.source }}]</span>
            <span
              class="ml-2 font-medium px-1 rounded"
              :class="{
                'bg-destructive/10 text-destructive': item.type === 'ERROR',
                'bg-blue-500/10 text-blue-500': item.type === 'DEBUG',
                'bg-green-500/10 text-green-500': item.type === 'INFO',
                'bg-amber-500/10 text-amber-500': item.type === 'WARNING'
              }"
            >
              [{{ item.type }}]
            </span>
            <span class="ml-2">{{ item.message }}</span>
          </div>
        </DynamicScrollerItem>
      </template>

      <template #after>
        <div v-if="consoleMessages.length === 0" class="p-2 text-muted-foreground text-center">
          {{ t('console.empty.console') }}
        </div>
      </template>
    </DynamicScroller>

    <!-- Problems Tab -->
    <DynamicScroller
      v-else-if="activeTab === 'problems'"
      ref="problemsScrollerRef"
      :items="problems"
      :min-item-size="60"
      class="flex-1 font-mono text-xs"
      key-field="id"
    >
      <template #default="{ item, index, active }">
        <DynamicScrollerItem
          :item="item"
          :active="active"
          :data-index="index"
          :size-dependencies="[item.message.length, item.file?.length || 0]"
          class="p-1"
        >
          <div class="my-1 flex items-start border-b border-muted/30 pb-1">
            <AlertCircle
              v-if="item.type === 'error'"
              class="h-4 w-4 text-destructive mr-2 mt-0.5 shrink-0"
            />
            <Info
              v-else
              class="h-4 w-4 text-amber-500 mr-2 mt-0.5 shrink-0"
            />
            <div>
              <div>{{ item.message }}</div>
              <div v-if="item.file" class="text-muted-foreground text-xs">
                {{ item.file }}{{ item.line ? `:${item.line}` : '' }}
              </div>
            </div>
          </div>
        </DynamicScrollerItem>
      </template>

      <template #after>
        <div v-if="problems.length === 0" class="p-2 text-muted-foreground text-center">
          {{ t('console.empty.problems') }}
        </div>
      </template>
    </DynamicScroller>

    <!-- Output Tab -->
    <DynamicScroller
      v-else
      ref="outputScrollerRef"
      :items="outputs"
      :min-item-size="30"
      class="flex-1 font-mono text-xs"
      key-field="id"
    >
      <template #default="{ item, index, active }">
        <DynamicScrollerItem
          :item="item"
          :active="active"
          :data-index="index"
          :size-dependencies="[item.message.length]"
          class="p-1"
        >
          <div class="my-1">
            <span class="text-muted-foreground">{{ item.timestamp }}:</span>
            <span class="ml-2">{{ item.message }}</span>
          </div>
        </DynamicScrollerItem>
      </template>

      <template #after>
        <div v-if="outputs.length === 0" class="p-2 text-muted-foreground text-center">
          {{ t('console.empty.output') }}
        </div>
      </template>
    </DynamicScroller>
  </div>
</template>

<style scoped>
:deep(.vue-recycle-scroller__item-wrapper) {
  overflow: visible;
}

:deep(.vue-recycle-scroller__item-view) {
  overflow: visible;
}
</style>
