<script setup lang="ts">
import {ref, onMounted} from 'vue'
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
import {useProjectStore} from '@/stores/project.store'
import {useImageStore} from '@/stores/image.store'
import type {Character, CharacterRequest} from '@/interfaces'
import ImageUploader from "~/components/ImageUploader.vue"

const route = useRoute()
const router = useRouter()
const localePath = useLocalePath()
const { toast } = useToast()
const characterStore = useCharacterStore()
const imageStore = useImageStore()
const projectStore = useProjectStore()

const { t } = useI18n()

const schema = toTypedSchema(z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  role: z.enum(['Primary', 'Secondary', 'Tertiary', 'Undefined']),
  imageID: z.string().optional(),
  additionalNotes: z.string().optional().nullable(),
  projectId: z.string().min(1, 'Project is required'),
}))

const form = useForm({
  validationSchema: schema,
})

const isSubmitting = ref(false)
const imagePreview = ref<string | null>(null)
const projects = ref<{ id: number; name: string }[]>([])
const character = ref<Character | null>(null)

onMounted(async () => {
  const characterId = Number(route.params.id)
  if (!isNaN(characterId)) {
    character.value = await characterStore.getCharacterById(characterId)
    if (character.value) {
      form.setValues({
        name: character.value.name,
        description: character.value.description,
        role: character.value.role,
        imageID: character.value.imageID || undefined,
        additionalNotes: character.value.additionalNotes,
        projectId: '', // We'll set this once we fetch the projects
      })
      imagePreview.value = character.value.imagePath || null
    }
  }

  projects.value = await projectStore.getProjectsForSelection()
  if (projects.value.length > 0) {
    // Set the project ID if we have it
    const characterProject = await projectStore.getProjectForCharacter(characterId)
    if (characterProject) {
      form.setFieldValue('projectId', characterProject.id.toString())
    } else {
      form.setFieldValue('projectId', projects.value[0].id.toString())
    }
  }
})

const onSubmit = form.handleSubmit(async (values) => {
  if (!character.value) return

  isSubmitting.value = true
  try {
    if (values.imageID && imagePreview.value) {
      await imageStore.saveImage({id: values.imageID, path: imagePreview.value})
    }

    const {projectId, ...characterData} = values
    await characterStore.updateCharacter(character.value.id, characterData as CharacterRequest)
    await projectStore.updateCharacterProject(character.value.id, +projectId)

    toast({
      title: t('characters.updateSuccess'),
      description: t('characters.updateSuccessDescription', {name: values.name}),
    })
    await router.push(localePath(`/characters/${character.value.id}`))
  } catch (error) {
    console.error('Error updating character:', error)
    toast({
      title: t('characters.updateError'),
      description: t('characters.updateErrorDescription'),
      variant: 'destructive',
    })
  } finally {
    isSubmitting.value = false
  }
})

const handleImageUpdate = (image: { id: string, path: string }) => {
  form.setFieldValue('imageID', image.id)
  imagePreview.value = image.path
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-muted/60">
    <div class="w-full max-w-2xl p-6 space-y-8">
      <div class="space-y-2 text-center">
        <h1 class="text-2xl font-bold">{{ t('characters.editCharacter') }}</h1>
        <p class="text-muted-foreground">{{ t('characters.editCharacterDescription') }}</p>
      </div>

      <form @submit="onSubmit" class="space-y-6">
        <div class="grid gap-6 sm:grid-cols-2">
          <FormField v-slot="{ componentField }" name="name">
            <FormItem>
              <FormLabel>{{ t("characters.name") }}</FormLabel>
              <FormControl>
                <Input type="text" :placeholder="t('characters.namePlaceholder')"
                       v-bind="componentField"/>
              </FormControl>
              <FormDescription>{{ t("characters.nameDescription") }}</FormDescription>
              <FormMessage/>
            </FormItem>
          </FormField>

          <FormField v-slot="{ componentField }" name="role">
            <FormItem>
              <FormLabel>{{ t("characters.role.name") }}</FormLabel>
              <Select v-bind="componentField">
                <FormControl>
                  <SelectTrigger>
                    <SelectValue :placeholder="t('characters.rolePlaceholder')"/>
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
              <FormMessage/>
            </FormItem>
          </FormField>
        </div>

        <FormField v-slot="{ componentField }" name="projectId">
          <FormItem>
            <FormLabel>{{ t("characters.project") }}</FormLabel>
            <Select v-bind="componentField">
              <FormControl>
                <SelectTrigger>
                  <SelectValue :placeholder="t('characters.projectPlaceholder')"/>
                </SelectTrigger>
              </FormControl>
              <SelectContent>
                <SelectGroup>
                  <SelectItem v-for="project in projects" :key="project.id"
                              :value="project.id.toString()">
                    {{ project.name }}
                  </SelectItem>
                </SelectGroup>
              </SelectContent>
            </Select>
            <FormDescription>{{ t("characters.projectDescription") }}</FormDescription>
            <FormMessage/>
          </FormItem>
        </FormField>

        <FormField v-slot="{ componentField }" name="description">
          <FormItem>
            <FormLabel>{{ t("characters.description") }}</FormLabel>
            <FormControl>
              <Textarea class="min-h-[100px]" :placeholder="t('characters.descriptionPlaceholder')"
                        v-bind="componentField"/>
            </FormControl>
            <FormDescription>{{ t("characters.textAreaDescription") }}</FormDescription>
            <FormMessage/>
          </FormItem>
        </FormField>

        <FormField v-slot="{ componentField }" name="additionalNotes">
          <FormItem>
            <FormLabel>{{ t("characters.additionalNotes") }}</FormLabel>
            <FormControl>
              <Textarea class="min-h-[100px]"
                        :placeholder="t('characters.additionalNotesPlaceholder')"
                        v-bind="componentField"/>
            </FormControl>
            <FormDescription>{{ t("characters.additionalNotesDescription") }}</FormDescription>
            <FormMessage/>
          </FormItem>
        </FormField>

        <ImageUploader
            @update:image="handleImageUpdate"
            :alt-text="t('imageUploader.defaultAlt')"
            :initial-image="imagePreview"
        />

        <Button type="submit" class="w-full" :disabled="isSubmitting">
          {{ isSubmitting ? t('characters.isSubmitting') : t('characters.updateCharacter') }}
        </Button>
      </form>
    </div>
  </div>
</template>