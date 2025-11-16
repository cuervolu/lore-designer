<script setup lang="ts">
import {computed} from 'vue';
import {useI18n} from 'vue-i18n';
import {useEditorStore} from '@editor/stores/editor.store';
import {AlertCircle, FileQuestion, ImageOff} from 'lucide-vue-next';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {Badge} from '@/components/ui/badge';
import {ScrollArea} from '@/components/ui/scroll-area';
import {Button} from '@/components/ui/button';

const props = defineProps<{
  open: boolean;
}>();

const emit = defineEmits<{
  'update:open': [value: boolean];
  openFile: [path: string];
}>();

const {t} = useI18n();
const editorStore = useEditorStore();

const report = computed(() => editorStore.imageValidationReport);

const hasMissingImages = computed(() => 
  report.value && report.value.missing_images.length > 0
);

const hasOrphanedImages = computed(() => 
  report.value && report.value.orphaned_images.length > 0
);
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="max-w-2xl max-h-[80vh] flex flex-col">
      <DialogHeader>
        <DialogTitle class="flex items-center gap-2">
          <AlertCircle class="h-5 w-5 text-warning"/>
          {{ t('workspace.health.title') }}
        </DialogTitle>
        <DialogDescription>
          {{ t('workspace.health.description') }}
        </DialogDescription>
      </DialogHeader>

      <ScrollArea class="flex-1 pr-4">
        <div class="space-y-4">
          <!-- Missing Images Section -->
          <div v-if="hasMissingImages" class="space-y-2">
            <div class="flex items-center gap-2">
              <ImageOff class="h-4 w-4 text-destructive"/>
              <h3 class="font-semibold">
                {{ t('workspace.health.missingImages', {count: report?.missing_images.length}) }}
              </h3>
              <Badge variant="destructive">{{ report?.missing_images.length }}</Badge>
            </div>

            <div class="space-y-2 pl-6">
              <div
                v-for="img in report?.missing_images"
                :key="img.path"
                class="border rounded-lg p-3 space-y-2"
              >
                <div class="flex items-center justify-between">
                  <code class="text-xs bg-muted px-2 py-1 rounded">
                    {{ img.original_name }}
                  </code>
                  <Badge variant="outline" class="text-xs">
                    {{ t('workspace.health.usedBy', {count: img.used_by.length}) }}
                  </Badge>
                </div>

                <div class="space-y-1">
                  <p class="text-xs text-muted-foreground">
                    {{ t('workspace.health.affectedFiles') }}:
                  </p>
                  <div class="space-y-1 pl-2">
                    <button
                      v-for="filePath in img.used_by"
                      :key="filePath"
                      @click="emit('openFile', filePath)"
                      class="text-xs text-primary hover:underline block"
                    >
                      {{ filePath }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Orphaned Images Section -->
          <div v-if="hasOrphanedImages" class="space-y-2">
            <div class="flex items-center gap-2">
              <FileQuestion class="h-4 w-4 text-muted-foreground"/>
              <h3 class="font-semibold">
                {{ t('workspace.health.orphanedImages', {count: report?.orphaned_images.length}) }}
              </h3>
              <Badge variant="secondary">{{ report?.orphaned_images.length }}</Badge>
            </div>

            <p class="text-xs text-muted-foreground pl-6">
              {{ t('workspace.health.orphanedImagesDescription') }}
            </p>

            <div class="space-y-1 pl-6">
              <code
                v-for="path in report?.orphaned_images"
                :key="path"
                class="text-xs bg-muted px-2 py-1 rounded block"
              >
                {{ path }}
              </code>
            </div>

            <div class="pl-6 pt-2">
              <Button size="sm" variant="outline" disabled>
                {{ t('workspace.health.cleanupOrphans') }}
                <span class="text-xs ml-2 text-muted-foreground">({{ t('common.comingSoon') }})</span>
              </Button>
            </div>
          </div>

          <!-- No Issues -->
          <div v-if="!hasMissingImages && !hasOrphanedImages" class="text-center py-8">
            <p class="text-muted-foreground">
              {{ t('workspace.health.noIssues') }}
            </p>
          </div>
        </div>
      </ScrollArea>
    </DialogContent>
  </Dialog>
</template>