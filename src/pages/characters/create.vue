<script setup lang="ts">
import { invoke, convertFileSrc } from "@tauri-apps/api/core";
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { useForm } from 'vee-validate'
import {
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useToast } from '@/components/ui/toast'
import { useCharacterStore } from '@/stores/character.store'
import type { CharacterRequest } from '@/interfaces'

const router = useRouter()
const { toast } = useToast()
const characterStore = useCharacterStore()

const { t } = useI18n()

const schema = toTypedSchema(z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  role: z.enum(['Primary', 'Secondary', 'Tertiary', 'Undefined']),
  imageID: z.string().optional(),
  additionalNotes: z.string().optional(),
}));

const form = useForm({
  validationSchema: schema,
})

const isSubmitting = ref(false)
const imagePreview = ref<string | null>(null)

const onSubmit = form.handleSubmit(async (values) => {
  isSubmitting.value = true
  try {
    if (values.imageID) {
      await characterStore.saveImage({id: values.imageID, path: imagePreview.value!});
    }

    const characterId = await characterStore.createCharacter(values as CharacterRequest)
    if (characterId) {
      toast({
        title: 'Character Created',
        description: `${values.name} has been successfully created.`,
      })
      await router.push('/characters')
    }
  } catch (error) {
    toast({
      title: 'Error',
      description: 'Failed to create character. Please try again.',
      variant: 'destructive',
    })
  } finally {
    isSubmitting.value = false
  }
})

const uploadImage = async () => {
  try {
    const result = await invoke('save_image') as { id: string, path: string }
    form.setFieldValue('imageID', result.id)
    imagePreview.value = convertFileSrc(result.path)
    toast({
      title: 'Image Uploaded',
      description: 'Image has been successfully uploaded.',
    })
  } catch (error) {
    console.error('Error uploading image:', error)
    toast({
      title: 'Error',
      description: 'Failed to upload image. Please try again.',
      variant: 'destructive',
    })
  }
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-muted/60">
    <div class="w-full max-w-2xl p-6 space-y-8">
      <div class="space-y-2 text-center">
        <h1 class="text-2xl font-bold">{{ t('characters.createCharacter') }}</h1>
        <p class="text-muted-foreground">{{ t('characters.createCharacterDescription') }}</p>
      </div>

      <form @submit="onSubmit" class="space-y-6">
        <div class="grid gap-6 sm:grid-cols-2">
          <FormField v-slot="{ componentField }" name="name">
            <FormItem>
              <FormLabel>{{ t("characters.name") }}</FormLabel>
              <FormControl>
                <Input type="text" :placeholder="t('characters.namePlaceholder')" v-bind="componentField"/>
              </FormControl>
              <FormDescription>{{ t("characters.nameDescription") }}</FormDescription>
              <FormMessage />
            </FormItem>
          </FormField>

          <FormField v-slot="{ componentField }" name="role">
            <FormItem>
              <FormLabel>{{ t("characters.role.name") }}</FormLabel>
              <Select v-bind="componentField">
                <FormControl>
                  <SelectTrigger>
                    <SelectValue :placeholder="t('characters.rolePlaceholder')" />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value="Primary">{{ t("characters.role.primary") }}</SelectItem>
                    <SelectItem value="Secondary">{{ t("characters.role.secondary") }}</SelectItem>
                    <SelectItem value="Tertiary">{{ t("characters.role.tertiary") }}</SelectItem>
                    <SelectItem value="Undefined">{{ t("characters.role.undefined") }}</SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>
              <FormDescription>{{ t("characters.role.description") }}</FormDescription>
              <FormMessage />
            </FormItem>
          </FormField>
        </div>

        <FormField v-slot="{ componentField }" name="description">
          <FormItem>
            <FormLabel>{{ t("characters.description") }}</FormLabel>
            <FormControl>
              <Textarea class="min-h-[100px]" :placeholder="t('characters.descriptionPlaceholder')" v-bind="componentField"/>
            </FormControl>
            <FormDescription>{{ t("characters.textAreaDescription") }}</FormDescription>
            <FormMessage />
          </FormItem>
        </FormField>

        <FormField v-slot="{ componentField }" name="additionalNotes">
          <FormItem>
            <FormLabel>{{ t("characters.additionalNotes") }}</FormLabel>
            <FormControl>
              <Textarea class="min-h-[100px]" :placeholder="t('characters.additionalNotesPlaceholder')" v-bind="componentField"/>
            </FormControl>
            <FormDescription>{{ t("characters.additionalNotesDescription") }}</FormDescription>
            <FormMessage />
          </FormItem>
        </FormField>

        <div class="space-y-4">
          <h3 class="text-lg font-medium">{{ t('characters.image.title') }}</h3>
          <p class="text-sm text-muted-foreground">{{ t('characters.image.description') }}</p>
          <div v-if="!imagePreview" class="flex items-center justify-center rounded-md border border-dashed p-6">
            <div class="text-center">
              <div class="mt-4 font-medium">{{ t('characters.image.click') }}</div>
              <p class="mt-2 text-sm text-muted-foreground">{{ t('characters.image.size') }}</p>
              <Button @click="uploadImage" class="mt-4">{{ t('characters.image.upload') }}</Button>
            </div>
          </div>
          <div v-else class="relative">
            <img :src="imagePreview" alt="Character Preview" class="w-full h-auto rounded-md"/>
            <Button @click="uploadImage" class="absolute bottom-2 right-2">{{ t('characters.image.change') }}</Button>
          </div>
        </div>

        <Button type="submit" class="w-full" :disabled="isSubmitting">
          {{ isSubmitting ? t('characters.isSubmitting') : t('characters.createCharacter') }}
        </Button>
      </form>
    </div>
  </div>
</template>