<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Button } from '@/components/ui/button';
import { AlertCircle, Info, ChevronUp, X, Trash2 } from 'lucide-vue-next';
import { ScrollArea } from '@/components/ui/scroll-area';

const activeTab = ref('console');

// Message types
type MessageType = 'ERROR' | 'DEBUG' | 'INFO' | 'WARNING';

// Message interface
interface LogMessage {
  timestamp: string;
  type: MessageType;
  message: string;
  source?: string;
}

// Problem interface
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

// Log messages
const consoleMessages = ref<LogMessage[]>([]);

// Problems
const problems = ref<Problem[]>([]);

// Output messages
const outputs = ref<OutputMessage[]>([]);

// Add a console message
const addConsoleMessage = (type: MessageType, message: string, source?: string) => {
  consoleMessages.value.unshift({
    timestamp: new Date().toISOString(),
    type,
    message,
    source
  });

  // Limit to 100 messages
  if (consoleMessages.value.length > 100) {
    consoleMessages.value.pop();
  }
};

// Add a problem
const addProblem = (type: 'error' | 'warning', message: string, file?: string, line?: number) => {
  problems.value.unshift({
    type,
    message,
    file,
    line
  });

  // Limit to 50 problems
  if (problems.value.length > 50) {
    problems.value.pop();
  }
};

// Add an output message
const addOutput = (message: string) => {
  outputs.value.unshift({
    timestamp: new Date().toISOString(),
    message
  });

  // Limit to 100 messages
  if (outputs.value.length > 100) {
    outputs.value.pop();
  }
};

// Clear the current tab
const clearCurrentTab = () => {
  if (activeTab.value === 'console') {
    consoleMessages.value = [];
  } else if (activeTab.value === 'problems') {
    problems.value = [];
  } else if (activeTab.value === 'output') {
    outputs.value = [];
  }
};

// Add some initial messages for demonstration
onMounted(() => {
  // Console messages
  addConsoleMessage('INFO', 'Editor initialized successfully');
  addConsoleMessage('DEBUG', 'Loading workspace configuration');
  addConsoleMessage('INFO', 'File indexing complete');

  // Sample problems
  addProblem('warning', 'Missing reference in character relationship', 'Characters/Hero.character', 42);

  // Sample outputs
  addOutput('Workspace loaded successfully');
});

const emit = defineEmits(['close']);

const handleClose = () => {
  emit('close');
};
</script>

<template>
  <div class="border-t h-48 flex flex-col">
    <!-- Console header with tabs -->
    <div class="flex items-center border-b bg-muted/30">
      <!-- Tabs -->
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

      <!-- Controls -->
      <div class="ml-auto flex items-center">
        <Button @click="clearCurrentTab" variant="ghost" size="sm" class="h-7 w-7 p-0" title="Clear">
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

    <!-- Console content -->
    <ScrollArea class="flex-1">
      <!-- Console tab -->
      <div v-if="activeTab === 'console'" class="font-mono text-xs p-1">
        <div v-for="(message, index) in consoleMessages" :key="index" class="my-1 border-b border-muted/30 pb-1">
          <span class="text-muted-foreground">{{ new Date(message.timestamp).toLocaleTimeString() }}</span>
          <span v-if="message.source" class="ml-2">{{ message.source }}</span>
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

      <!-- Problems tab -->
      <div v-else-if="activeTab === 'problems'" class="font-mono text-xs p-1">
        <div v-for="(problem, index) in problems" :key="index" class="my-1 flex items-start border-b border-muted/30 pb-1">
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

      <!-- Output tab -->
      <div v-else class="font-mono text-xs p-1">
        <div v-for="(output, index) in outputs" :key="index" class="my-1">
          <span class="text-muted-foreground">{{ new Date(output.timestamp).toLocaleTimeString() }}:</span>
          <span class="ml-2">{{ output.message }}</span>
        </div>

        <div v-if="outputs.length === 0" class="p-2 text-muted-foreground text-center">
          No output messages
        </div>
      </div>
    </ScrollArea>
  </div>
</template>
