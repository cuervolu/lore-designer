<script setup lang="ts">
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import type { Character } from "~/interfaces/models";
import { useCharacterRole } from '~/composables/useCharacterRole'
import noPhoto from '~/assets/img/no_photo.webp';

const { t } = useI18n();
const localePath = useLocalePath()
const router = useRouter();

const props = defineProps<Partial<Character>>();

const { translateRole } = useCharacterRole();

const navigateToCharacterDetails = () => {
  if (props.id) {
    router.push(localePath(`/characters/${props.id}`));
  }
};

const translatedRole = computed(() => {
  return props.role ? translateRole(props.role) : t('characters.noRole');
});
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
      <CardTitle class="text-lg">{{ props.name || t('characters.noName') }}</CardTitle>
      <CardDescription class="text-xs">
        {{ props.description || t('characters.noDescription') }}
      </CardDescription>
    </CardHeader>
    <CardContent class="p-3">
      <p class="text-xs text-gray-600 line-clamp-2">
        {{ props.additionalNotes || t('characters.noAdditionalNotes') }}
      </p>
    </CardContent>
    <CardFooter class="p-3 text-xs text-gray-500">
      {{ translatedRole }}
    </CardFooter>
  </Card>
</template>