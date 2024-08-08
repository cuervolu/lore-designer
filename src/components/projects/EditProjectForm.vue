<script setup lang="ts">
import {ref} from 'vue'
import {useForm} from 'vee-validate'
import {toTypedSchema} from '@vee-validate/zod'
import * as z from 'zod'
import {useProjectStore} from '~/stores/project.store'
import {useToast} from '~/components/ui/toast'
import {Button} from '~/components/ui/button'
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '~/components/ui/form'
import {Input} from '~/components/ui/input'
import {Textarea} from '~/components/ui/textarea'
import ImageUploader from '~/components/ImageUploader.vue'
import type {Project} from '~/interfaces'
import {error} from "@tauri-apps/plugin-log";
import {useImageStore} from '~/stores'

const props = defineProps<{
  project: Project
}>()

const emit = defineEmits(['update:project'])

const {t} = useI18n()
const projectStore = useProjectStore()
const imageStore = useImageStore()
const {toast} = useToast()

const formSchema = toTypedSchema(z.object({
  name: z.string().min(1, t('projects.nameRequired')),
  description: z.string().optional().nullable(),
  budget: z.number().min(0).optional().nullable(),
  imageID: z.string().optional().nullable(),
}))

const form = useForm({
  validationSchema: formSchema,
  initialValues: {
    name: props.project.name,
    description: props.project.description,
    budget: props.project.budget,
    imageID: props.project.imageID,
  },
})

const isSubmitting = ref(false)

const onSubmit = form.handleSubmit(async (values) => {
  isSubmitting.value = true
  try {
    const success = await projectStore.updateProject(props.project.id, values)
    if (success) {
      toast({
        title: t('projects.updateSuccess'),
        description: t('projects.updateSuccessDescription'),
      })
      emit('update:project', {...props.project, ...values})
    } else {
      toast({
        title: t('projects.updateError'),
        description: t('projects.updateErrorDescription'),
        variant: 'destructive',
      })
    }
  } catch (e) {
    await error(`Error updating project: ${e}`)
    toast({
      title: t('projects.updateError'),
      description: t('projects.updateErrorDescription'),
      variant: 'destructive',
    })
  } finally {
    isSubmitting.value = false
  }
})

const handleImageUpdate = async (imageInfo: { id: string, path: string }) => {
  try {
    const savedImageId = await imageStore.saveImage(imageInfo)
    form.setFieldValue('imageID', savedImageId)
  } catch (e) {
    await error(`Error saving image: ${e}`)
    toast({
      title: t('projects.imageUpdateError'),
      description: t('projects.imageUpdateErrorDescription'),
      variant: 'destructive',
    })
  }
}
</script>

<template>
  <form @submit="onSubmit">
    <FormField v-slot="{ componentField }" name="name">
      <FormItem>
        <FormLabel>{{ t('projects.name') }}</FormLabel>
        <FormControl>
          <Input v-bind="componentField" :placeholder="t('projects.namePlaceholder')"/>
        </FormControl>
        <FormMessage/>
      </FormItem>
    </FormField>

    <FormField v-slot="{ componentField }" name="description">
      <FormItem>
        <FormLabel>{{ t('projects.description') }}</FormLabel>
        <FormControl>
          <Textarea v-bind="componentField" :placeholder="t('projects.descriptionPlaceholder')"/>
        </FormControl>
        <FormMessage/>
      </FormItem>
    </FormField>

    <FormField v-slot="{ componentField }" name="budget">
      <FormItem>
        <FormLabel>{{ t('projects.budget') }}</FormLabel>
        <FormControl>
          <Input v-bind="componentField" type="number"
                 :placeholder="t('projects.budgetPlaceholder')"/>
        </FormControl>
        <FormMessage/>
      </FormItem>
    </FormField>

    <FormField v-slot="{ field }" name="imageID">
      <FormItem>
        <FormLabel>{{ t('projects.image') }}</FormLabel>
        <FormControl>
          <ImageUploader
              :initialImage="props.project.imagePath"
              :altText="props.project.name"
              @update:image="handleImageUpdate"
          />
        </FormControl>
        <FormMessage/>
      </FormItem>
    </FormField>

    <Button type="submit" :disabled="isSubmitting">
      {{ isSubmitting ? t('submitting') : t('projects.update') }}
    </Button>
  </form>
</template>