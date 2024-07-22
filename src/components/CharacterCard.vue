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
import noPhoto from '~/assets/img/no_photo.webp';

const props = defineProps<Partial<Character>>();
const router = useRouter();

const navigateToCharacterDetails = () => {
  if (props.id) {
    router.push(`/characters/${props.id}`);
  }
};
</script>

<template>
  <Card class="h-full transition-all duration-300 ease-in-out hover:scale-105 hover:shadow-lg"
        @click="navigateToCharacterDetails">
    <div class="relative h-32 overflow-hidden rounded-t-lg">
      <img
          :src="props.imagePath || noPhoto"
          :alt="props.name || 'Character'"
          class="absolute inset-0 h-full w-full object-cover"
      />
    </div>
    <CardHeader class="p-3">
      <CardTitle class="text-lg">{{ props.name || 'Unnamed Character' }}</CardTitle>
      <CardDescription class="text-xs">
        {{ props.description || 'No description available' }}
      </CardDescription>
    </CardHeader>
    <CardContent class="p-3">
      <p class="text-xs text-gray-600 line-clamp-2">
        {{ props.additionalNotes || 'No additional notes' }}
      </p>
    </CardContent>
    <CardFooter class="p-3 text-xs text-gray-500">
      {{ props.role || 'Undefined' }}
    </CardFooter>
  </Card>
</template>