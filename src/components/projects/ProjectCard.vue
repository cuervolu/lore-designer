<script setup lang="ts">
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import type {Project} from "~/interfaces";
import noProjectImage from '~/assets/img/no_photo.webp';

const {t} = useI18n();
const localePath = useLocalePath()
const router = useRouter();

const props = defineProps<Partial<Project>>();

const navigateToProjectDetails = () => {
  if (props.id) {
    router.push(localePath(`/projects/${props.id}`));
  }
};

const formattedCreatedDate = computed(() => {
  if (props.createdAt) {
    return new Date(props.createdAt).toLocaleDateString();
  }
  return '';
});

const formattedUpdatedDate = computed(() => {
  if (props.updatedAt) {
    return new Date(props.updatedAt).toLocaleDateString();
  }
  return '';
});

const showLastUpdated = computed(() => {
  return props.createdAt !== props.updatedAt && props.updatedAt;
});
</script>

<template>
  <Card class="h-full transition-all duration-300 ease-in-out hover:scale-105 hover:shadow-lg"
        @click="navigateToProjectDetails">
    <div class="relative h-32 overflow-hidden rounded-t-lg">
      <img
          :src="props.imageID || noProjectImage"
          :alt="props.name || 'Project'"
          class="absolute inset-0 h-full w-full object-cover"
      />
    </div>
    <CardHeader class="p-3">
      <CardTitle class="text-lg">{{ props.name || t('projects.noName') }}</CardTitle>
    </CardHeader>
    <CardContent class="p-3">
      <CardDescription class="text-xs">
        {{ props.description || t('projects.noDescription') }}
      </CardDescription>
    </CardContent>
    <CardFooter>
      <p class="text-xs text-gray-600">
        {{ t('projects.created') }}: {{ formattedCreatedDate }}
      </p>
      <p v-if="showLastUpdated" class="text-xs text-gray-600 mt-1">
        {{ t('projects.lastUpdated') }}: {{ formattedUpdatedDate }}
      </p>
    </CardFooter>
  </Card>
</template>