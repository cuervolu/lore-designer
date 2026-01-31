<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { listen } from '@tauri-apps/api/event';
import { info } from '@fltsci/tauri-plugin-tracing';

interface LogEntry {
  message: string;
  level: number;
}

const logs = ref<LogEntry[]>([]);
const debugInfo = ref<string[]>([]); 
let unlisten: (() => void) | null = null;
let testUnlisten: (() => void) | null = null;

const levelNames: Record<number, string> = {
  1: 'trace',
  2: 'debug',
  3: 'info',
  4: 'warn',
  5: 'error',
};

function getLevelName(level: number): string {
  return levelNames[level] || 'unknown';
}

onMounted(async () => {
  debugInfo.value.push(`[${new Date().toISOString()}] Mounting component...`);

  testUnlisten = await listen<string>('test-event', (event) => {
    debugInfo.value.push(
      `[${new Date().toISOString()}] Test event received: ${event.payload}`,
    );
  });

  debugInfo.value.push(
    `[${new Date().toISOString()}] Calling info() from frontend...`,
  );
  info('Console UI mounted, listening for log events...');

  debugInfo.value.push(
    `[${new Date().toISOString()}] Setting up tracing://log listener...`,
  );
  unlisten = await listen<LogEntry>('tracing://log', (event) => {
    debugInfo.value.push(
      `[${new Date().toISOString()}] LOG RECEIVED: ${JSON.stringify(event.payload)}`,
    );
    logs.value.unshift(event.payload);
  });

  debugInfo.value.push(`[${new Date().toISOString()}] Listener setup complete`);
});

onUnmounted(() => {
  if (unlisten) {
    info('Console UI unmounting, cleaning up listener...');
    unlisten();
  }
  if (testUnlisten) {
    testUnlisten();
  }
});
</script>

<template>
  <div class="container">
    <header>
      <h1>Lore Console</h1>
    </header>

    <div class="debug-panel">
      <h3>Debug Info:</h3>
      <div v-for="(msg, i) in debugInfo" :key="i" class="debug-line">
        {{ msg }}
      </div>
    </div>

    <div class="console-window">
      <div v-if="logs.length === 0" class="empty-state">
        Waiting for logs... ({{ logs.length }} logs received)
      </div>

      <div
        v-for="(log, index) in logs"
        :key="index"
        class="log-line"
        :class="getLevelName(log.level)"
      >
        <span class="level">[{{ getLevelName(log.level).toUpperCase() }}]</span>
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

.debug-panel {
  background-color: #2d2d2d;
  border: 1px solid #444;
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
  max-height: 200px;
  overflow-y: auto;
}

.debug-panel h3 {
  margin: 0 0 0.5rem 0;
  color: #4fc1ff;
  font-size: 0.9rem;
}

.debug-line {
  font-family: 'Fira Code', monospace;
  font-size: 0.8rem;
  color: #aaa;
  padding: 2px 0;
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

.level {
  font-weight: bold;
  min-width: 80px;
}

.message {
  color: #d4d4d4;
  white-space: pre-wrap;
  word-break: break-all;
}

.trace .level {
  color: #888888;
}
.debug .level {
  color: #b5cea8;
}
.info .level {
  color: #4fc1ff;
}
.warn .level {
  color: #cca700;
}
.error .level {
  color: #f44747;
}
</style>
