<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import type { Component } from 'vue'

defineProps<{
  title: string
  description?: string
  icon?: Component
}>()
</script>

<template>
  <Card class="transition-all duration-300 hover:shadow-md">
    <CardHeader>
      <div class="flex items-center gap-3">
        <div v-if="icon" class="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center">
          <component :is="icon" class="w-5 h-5 text-primary" />
        </div>
        <div class="flex-1">
          <CardTitle class="text-xl">{{ title }}</CardTitle>
          <CardDescription v-if="description">{{ description }}</CardDescription>
        </div>
      </div>
    </CardHeader>
    <CardContent class="space-y-6">
      <slot />
    </CardContent>
  </Card>
</template>
