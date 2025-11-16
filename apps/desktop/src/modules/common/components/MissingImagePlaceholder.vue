<script setup lang="ts">
import {computed} from 'vue';
import {useI18n} from 'vue-i18n';
import {ImageOff, MapPin, Trash2} from 'lucide-vue-next';
import {Button} from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

const props = defineProps<{
  relativePath: string;
}>();

const emit = defineEmits<{
  relocate: [];
  remove: [];
}>();

const {t} = useI18n();

const displayPath = computed(() => {
  const parts = props.relativePath.split('/');
  return parts[parts.length - 1] || props.relativePath;
});
</script>

<template>
  <Card class="border-destructive/50 bg-destructive/5">
    <CardHeader class="pb-3">
      <div class="flex items-center gap-2">
        <ImageOff class="h-5 w-5 text-destructive"/>
        <CardTitle class="text-sm">{{ t('editor.imageMissing') }}</CardTitle>
      </div>
      <CardDescription class="text-xs font-mono">
        {{ displayPath }}
      </CardDescription>
    </CardHeader>
    <CardContent>
      <div class="flex gap-2">
        <Button
          size="sm"
          variant="outline"
          @click="emit('relocate')"
          class="flex-1"
        >
          <MapPin class="h-3 w-3 mr-1"/>
          {{ t('editor.relocateImage') }}
        </Button>
        <Button
          size="sm"
          variant="destructive"
          @click="emit('remove')"
        >
          <Trash2 class="h-3 w-3 mr-1"/>
          {{ t('editor.removeReference') }}
        </Button>
      </div>
    </CardContent>
  </Card>
</template>