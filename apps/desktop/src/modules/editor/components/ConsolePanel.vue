<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Button } from '@/components/ui/button'
import { AlertCircle, Info, ChevronUp, X, Trash2 } from 'lucide-vue-next'
import { ScrollArea } from '@/components/ui/scroll-area'
import {
  listenToLogs,
  getHistory,
  clearHistory,
  type LogEntry as PluginLogEntry
} from 'tauri-plugin-tracing'

const activeTab = ref('console')
let unlisten: (() => void) | null = null

// Message types (tu tipo local)
type MessageType = 'ERROR' | 'DEBUG' | 'INFO' | 'WARNING';

interface LogMessage {
  timestamp: string;
  type: MessageType;
  message: string;
  source?: string;
}

interface Problem {
  type: 'error' | 'warning';
  message: string;
  file?: string;
  line?: number;
}

// Output message interface
interface OutputMessage {
  timestamp: string;
  message: string;
}

const consoleMessages = ref<LogMessage[]>([])

const problems = ref<Problem[]>([])

const outputs = ref<OutputMessage[]>([])

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
    timestamp: entry.timestamp,
    type: messageType,
    message: entry.message,
    source: entry.target
  }
}

const addConsoleMessage = (type: MessageType, message: string, source?: string) => {
  consoleMessages.value.unshift({
    timestamp: new Date().toLocaleTimeString(),
    type,
    message,
    source
  })

  if (consoleMessages.value.length > 100) {
    consoleMessages.value.pop()
  }
}

const addProblem = (type: 'error' | 'warning', message: string, file?: string, line?: number) => {
  problems.value.unshift({
    type,
    message,
    file,
    line
  })

  if (problems.value.length > 50) {
    problems.value.pop()
  }
}

const addOutput = (message: string) => {
  outputs.value.unshift({
    timestamp: new Date().toLocaleTimeString(),
    message
  })

  // Limit to 100 messages
  if (outputs.value.length > 100) {
    outputs.value.pop()
  }
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

onMounted(async () => {
  try {
    const history = await getHistory()
    consoleMessages.value = history.map(transformLogEntry).reverse()
  } catch (e) {
    console.error('Failed to load log history:', e)
    addConsoleMessage('ERROR', `Failed to load log history: ${e}`, 'ConsolePanel')
  }

  try {
    unlisten = await listenToLogs((event) => {
      const newEntries = event.payload.map(transformLogEntry)
      consoleMessages.value.unshift(...newEntries.reverse())

      if (consoleMessages.value.length > 100) {
        consoleMessages.value = consoleMessages.value.slice(0, 100)
      }
    })
  } catch (e) {
    console.error('Failed to listen to logs:', e)
    addConsoleMessage('ERROR', `Failed to listen to logs: ${e}`, 'ConsolePanel')
  }

  addProblem('warning', 'Missing reference in character relationship', 'Characters/Hero.character', 42)
  addOutput('Workspace loaded successfully')
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
          Console
        </button>
        <button
          @click="activeTab = 'problems'"
          class="px-4 py-2 text-sm border-r hover:bg-muted/50"
          :class="{ 'bg-muted/50 font-medium': activeTab === 'problems' }"
        >
          Problems
        </button>
        <button
          @click="activeTab = 'output'"
          class="px-4 py-2 text-sm hover:bg-muted/50"
          :class="{ 'bg-muted/50 font-medium': activeTab === 'output' }"
        >
          Output
        </button>
      </div>

      <div class="ml-auto flex items-center">
        <Button @click="clearCurrentTab" variant="ghost" size="sm" class="h-7 w-7 p-0"
                title="Clear">
          <Trash2 class="h-4 w-4" />
        </Button>
        <Button variant="ghost" size="sm" class="h-7 w-7 p-0" title="Maximize">
          <ChevronUp class="h-4 w-4" />
        </Button>
        <Button @click="handleClose" variant="ghost" size="sm" class="h-7 w-7 p-0" title="Close">
          <X class="h-4 w-4" />
        </Button>
      </div>
    </div>

    <ScrollArea class="flex-1">
      <div v-if="activeTab === 'console'" class="font-mono text-xs p-1">
        <div v-for="(message, index) in consoleMessages" :key="index"
             class="my-1 border-b border-muted/30 pb-1">
          <span class="text-muted-foreground">{{ message.timestamp }}</span>
          <span v-if="message.source" class="ml-2 text-purple-400">[{{ message.source }}]</span>
          <span
            class="ml-2 font-medium px-1 rounded"
            :class="{
              'bg-destructive/10 text-destructive': message.type === 'ERROR',
              'bg-blue-500/10 text-blue-500': message.type === 'DEBUG',
              'bg-green-500/10 text-green-500': message.type === 'INFO',
              'bg-amber-500/10 text-amber-500': message.type === 'WARNING'
            }"
          >
            [{{ message.type }}]
          </span>
          <span class="ml-2">{{ message.message }}</span>
        </div>

        <div v-if="consoleMessages.length === 0" class="p-2 text-muted-foreground text-center">
          No console messages
        </div>
      </div>

      <div v-else-if="activeTab === 'problems'" class="font-mono text-xs p-1">
        <div v-for="(problem, index) in problems" :key="index"
             class="my-1 flex items-start border-b border-muted/30 pb-1">
          <AlertCircle
            v-if="problem.type === 'error'"
            class="h-4 w-4 text-destructive mr-2 mt-0.5 flex-shrink-0"
          />
          <Info
            v-else
            class="h-4 w-4 text-amber-500 mr-2 mt-0.5 flex-shrink-0"
          />
          <div>
            <div>{{ problem.message }}</div>
            <div v-if="problem.file" class="text-muted-foreground text-xs">
              {{ problem.file }}{{ problem.line ? `:${problem.line}` : '' }}
            </div>
          </div>
        </div>

        <div v-if="problems.length === 0" class="p-2 text-muted-foreground text-center">
          No problems detected
        </div>
      </div>

      <div v-else class="font-mono text-xs p-1">
        <div v-for="(output, index) in outputs" :key="index" class="my-1">
          <span class="text-muted-foreground">{{ new Date(output.timestamp).toLocaleTimeString()
            }}:</span>
          <span class="ml-2">{{ output.message }}</span>
        </div>

        <div v-if="outputs.length === 0" class="p-2 text-muted-foreground text-center">
          No output messages
        </div>
      </div>
    </ScrollArea>
  </div>
</template>
