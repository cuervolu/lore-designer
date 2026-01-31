<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { listen } from '@tauri-apps/api/event';
import { info } from '@fltsci/tauri-plugin-tracing';

interface LogEntry {
  timestamp: string;
  level: string;
  target: string;
  message: string;
}

const logs = ref<LogEntry[]>([]);
let unlisten: (() => void) | null = null;

onMounted(async () => {
  info('Listening for log events from backend...');
  unlisten = await listen<LogEntry>('lore://console/log', (event) => {
    info('New log received:', event.payload);
    logs.value.unshift(event.payload);
  });
});

onUnmounted(() => {
  if (unlisten) {
    unlisten();
  }
});
</script>

<template>
  <div class="container">
    <header>
      <h1>Lore Console</h1>
    </header>

    <div class="console-window">
      <div v-if="logs.length === 0" class="empty-state">
       Waiting for logs....
      </div>

      <div 
        v-for="(log, index) in logs" 
        :key="index" 
        class="log-line"
        :class="log.level.toLowerCase()"
      >
        <span class="timestamp">{{ log.timestamp }}</span>
        <span class="level">[{{ log.level }}]</span>
        <span class="target">{{ log.target }}</span>
        <span class="message">{{ log.message }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  font-family: 'Inter', sans-serif;
}

header {
  margin-bottom: 1rem;
}

.console-window {
  background-color: #1e1e1e;
  border-radius: 8px;
  padding: 1rem;
  height: 500px;
  overflow-y: auto;
  font-family: 'Fira Code', monospace;
  font-size: 0.9rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
  border: 1px solid #333;
}

.empty-state {
  color: #666;
  text-align: center;
  padding-top: 2rem;
}

.log-line {
  display: flex;
  gap: 10px;
  padding: 4px 0;
  border-bottom: 1px solid #2a2a2a;
  align-items: flex-start;
}

.timestamp {
  color: #888;
  min-width: 80px;
}

.level {
  font-weight: bold;
  min-width: 60px;
}

.target {
  color: #569cd6; 
  min-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message {
  color: #d4d4d4;
  white-space: pre-wrap;
  word-break: break-all;
}

.info .level { color: #4fc1ff; }  
.warn .level { color: #cca700; }   
.error .level { color: #f44747; }  
.debug .level { color: #b5cea8; }  
.trace .level { color: #888888; }  
</style>