<script setup lang="ts">
import { ref } from 'vue';
import { Button } from '@/components/ui/button';
import { AlertCircle, Info, ChevronUp, X } from 'lucide-vue-next';

const activeTab = ref('console');

// Mock console data
const consoleMessages = [
  { timestamp: '2025-04-16T20:04:01.301Z', server: 'server01', service: 'DatabaseConnector', type: 'ERROR', message: 'Operation completed for user 305' },
  { timestamp: '2025-04-16T19:29:36.625Z', server: 'server01', service: 'PaymentGateway', type: 'ERROR', message: 'Operation completed for user 404' },
  { timestamp: '2025-04-16T09:21:37.024Z', server: 'server01', service: 'PaymentGateway', type: 'ERROR', message: 'Operation completed for user 42' },
  { timestamp: '2025-04-12T16:45:12.830Z', server: 'server01', service: 'PaymentGateway', type: 'ERROR', message: 'Operation completed for user 87' },
  { timestamp: '2025-04-12T18:25:19.658Z', server: 'server01', service: 'UserService', type: 'DEBUG', message: 'Operation pending for user 950' },
];

// Mock problem data
const problems = [
  { type: 'error', message: 'Unable to open database connection', file: 'database.ts', line: 42 },
  { type: 'warning', message: 'Unused variable "result"', file: 'main.ts', line: 156 },
  { type: 'error', message: 'Reference to undefined variable', file: 'character.ts', line: 23 },
];

// Mock output data
const outputs = [
  { timestamp: '2025-04-16T20:05:13', message: 'Exporting dialogue tree...' },
  { timestamp: '2025-04-16T20:05:14', message: 'Generated 24 dialogue nodes' },
  { timestamp: '2025-04-16T20:05:15', message: 'Export completed successfully' },
];

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
        <Button variant="ghost" size="sm" class="h-7 w-7 p-0">
          <ChevronUp class="h-4 w-4" />
        </Button>
        <Button @click="handleClose" variant="ghost" size="sm" class="h-7 w-7 p-0">
          <X class="h-4 w-4" />
        </Button>
      </div>
    </div>

    <!-- Console content -->
    <div class="flex-1 overflow-auto font-mono text-xs p-1">
      <!-- Console tab -->
      <div v-if="activeTab === 'console'" class="h-full">
        <div v-for="(message, index) in consoleMessages" :key="index" class="my-1 border-b border-muted/30 pb-1">
          <span class="text-muted-foreground">{{ new Date(message.timestamp).toLocaleTimeString() }}</span>
          <span class="ml-2">{{ message.server }}</span>
          <span class="ml-2">{{ message.service }}</span>
          <span
            class="ml-2 font-medium px-1 rounded"
            :class="{
              'bg-destructive/10 text-destructive': message.type === 'ERROR',
              'bg-blue-500/10 text-blue-500': message.type === 'DEBUG'
            }"
          >
            [{{ message.type }}]
          </span>
          <span class="ml-2">{{ message.message }}</span>
        </div>
      </div>

      <!-- Problems tab -->
      <div v-else-if="activeTab === 'problems'" class="h-full">
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
            <div class="text-muted-foreground text-xs">{{ problem.file }}:{{ problem.line }}</div>
          </div>
        </div>
      </div>

      <!-- Output tab -->
      <div v-else class="h-full">
        <div v-for="(output, index) in outputs" :key="index" class="my-1">
          <span class="text-muted-foreground">{{ new Date(output.timestamp).toLocaleTimeString() }}:</span>
          <span class="ml-2">{{ output.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
