<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { invoke, convertFileSrc } from "@tauri-apps/api/core"
import { useToast } from '@/components/ui/toast'
import { Button } from '@/components/ui/button'
import {useImageStore} from '@/stores/image.store'
import type { ImageInfo } from '~/interfaces'

const props = defineProps<{
  initialImage?: string | null
  altText?: string
  characterId?: number
}>()

const emit = defineEmits<{
  (e: 'update:image', value: ImageInfo): void
}>()

const { toast } = useToast()
const imageStore = useImageStore()
const { t } = useI18n()

const imagePreview = ref<string | null>(null)

watchEffect(() => {
  if (props.initialImage) {
    imagePreview.value = props.initialImage
  }
})

const uploadImage = async (event: Event) => {
  event.preventDefault()
  try {
    const result = await invoke('save_image', { characterId: props.characterId }) as { id: string, path: string }
    const convertedPath = convertFileSrc(result.path)

    await imageStore.saveImage({id: result.id, path: convertedPath})
    imagePreview.value = convertedPath
    emit('update:image', {
      id: result.id,
      path: convertedPath
    })
    toast({
      title: t('imageUploader.successTitle'),
      description: t('imageUploader.successDescription'),
    })
  } catch (error) {
    toast({
      title: t('imageUploader.errorTitle'),
      description: t('imageUploader.errorDescription'),
      variant: 'destructive',
    })
  }
}

const revertImage = async (originalImageId: string, originalImagePath: string) => {
  if (imagePreview.value !== originalImagePath) {
    await imageStore.revertImage(originalImageId, originalImagePath)
    imagePreview.value = originalImagePath
  }
}

defineExpose({revertImage})
</script>
<template>
  <div class="space-y-4">
    <h3 class="text-lg font-medium">{{ t('imageUploader.title') }}</h3>
    <p class="text-sm text-muted-foreground">{{ t('imageUploader.description') }}</p>
    <div v-if="!imagePreview" class="flex items-center justify-center rounded-md border border-dashed p-6">
      <div class="text-center">
        <div class="mt-4 font-medium">{{ t('imageUploader.click') }}</div>
        <p class="mt-2 text-sm text-muted-foreground">{{ t('imageUploader.size') }}</p>
        <Button @click="uploadImage" type="button" class="mt-4">{{ t('imageUploader.upload') }}</Button>
      </div>
    </div>
    <div v-else class="relative">
      <img :src="imagePreview" :alt="altText || t('imageUploader.defaultAlt')" class="w-full h-auto rounded-md"/>
      <Button @click="uploadImage" type="button" class="absolute bottom-2 right-2">
        {{ t('imageUploader.change') }}
      </Button>
    </div>
  </div>
</template>