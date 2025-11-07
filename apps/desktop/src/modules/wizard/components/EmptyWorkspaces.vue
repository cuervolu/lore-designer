<script setup lang="ts">
import { Sparkles } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

defineProps<{
  onCreateClick: () => void
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 px-4 text-center">
    <div class="mb-6 relative">
      <img
        src="https://placehold.co/400x300/6366f1/ffffff/png?text=Your+Stories+Start+Here"
        alt="Empty workspace illustration"
        class="w-64 h-48 object-cover rounded-lg opacity-80"
      />
      <div class="absolute -top-2 -right-2 bg-primary rounded-full p-2">
        <Sparkles class="w-5 h-5 text-primary-foreground" />
      </div>
    </div>

    <h2 class="text-2xl font-bold mb-2">{{ $t('wizard.workspaces.empty.title') }}</h2>
    <p class="text-muted-foreground mb-6 max-w-md">
      {{ $t('wizard.workspaces.empty.description') }}
    </p>

    <Button size="lg" @click="onCreateClick" class="gap-2">
      <Sparkles class="w-4 h-4" />
      {{ $t('wizard.workspaces.empty.createButton') }}
    </Button>
  </div>
</template>
