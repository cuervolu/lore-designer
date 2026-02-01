<script setup lang="ts">
import { Sparkles } from 'lucide-vue-next';
import { Button } from '@/components/ui/button';

const emit = defineEmits<{
  createWorkspace: [];
  openWorkspace: [];
}>();
</script>

<template>
  <div class="flex flex-col items-center justify-center h-full py-16 px-4">
    <Sparkles :size="64" class="text-primary mb-6" />

    <h2 class="text-2xl font-semibold mb-2">Your next story begins here</h2>

    <p class="text-muted-foreground text-center max-w-md mb-8">
      Lore Designer helps you organize characters, locations, and narrative
      elements in one unified workspace
    </p>

    <div class="flex flex-col gap-3 items-center">
      <Button size="lg" @click="emit('createWorkspace')">
        <Sparkles :size="20" />
        Create Your First Workspace
      </Button>

      <button
        class="text-sm text-muted-foreground hover:text-foreground hover:underline"
        @click="emit('openWorkspace')"
      >
        or open an existing workspace
      </button>
    </div>
  </div>
</template>
