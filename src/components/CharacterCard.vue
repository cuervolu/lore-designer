
<script setup lang="ts">
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import type {Character} from "~/interfaces/models";

defineProps<Character>()
</script>

<template>
  <Card class="h-full transition-all duration-300 ease-in-out hover:scale-105 hover:shadow-lg">
    <div class="relative h-32 overflow-hidden rounded-t-lg">
      <img
          src="https://picsum.photos/seed/picsum/200/300"
          alt="Character"
          class="absolute inset-0 h-full w-full object-cover"
      />
    </div>
    <CardHeader class="p-3">
      <CardTitle class="text-lg">{{ name }}</CardTitle>
      <CardDescription class="text-xs">
        {{ description }}
      </CardDescription>
    </CardHeader>
    <CardContent class="p-3">
      <p class="text-xs text-gray-600 line-clamp-2">
        {{ additionalNotes }}
      </p>
    </CardContent>
    <CardFooter class="p-3 text-xs text-gray-500">
      {{ role }}
    </CardFooter>
  </Card>
</template>
