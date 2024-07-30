<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { useToast } from '@/components/ui/toast'
import { useCharacterStore } from '@/stores/character.store'
import type { CharacterRequest, Character } from '@/interfaces'
import ImageUploader from "~/components/ImageUploader.vue"
import {info} from "@tauri-apps/plugin-log";

const route = useRoute()
const router = useRouter()
const { toast } = useToast()
const characterStore = useCharacterStore()

const { t } = useI18n()

const schema = toTypedSchema(z.object({
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  role: z.enum(['Primary', 'Secondary', 'Tertiary', 'Undefined']),
  imageID: z.string().optional().nullable(),
  additionalNotes: z.string().optional().nullable(),
}))

const form = useForm({
  validationSchema: schema,
})

const isSubmitting = ref(false)
const imagePreview = ref<string | null>(null)
const originalCharacter = ref<Character | null>(null)
const showConfirmDialog = ref(false)
const characterId = ref<number | null>(null)

const hasChanges = computed(() => {
  if (!originalCharacter.value) return false
  const currentValues = form.values
  return Object.keys(currentValues).some(key => {
    return currentValues[key as keyof typeof currentValues] !==
        (originalCharacter.value as any)[key]
  })
})

onMounted(async () => {
  const id = Number(route.params.id)
  if (!isNaN(id)) {
    characterId.value = id
    originalCharacter.value = await characterStore.getCharacterById(id)
    if (originalCharacter.value) {
      form.setValues({
        name: originalCharacter.value.name,
        description: originalCharacter.value.description || '',
        role: originalCharacter.value.role,
        imageID: originalCharacter.value.imageID || undefined,
        additionalNotes: originalCharacter.value.additionalNotes || '',
      })
      if (originalCharacter.value.imagePath) {
        await info(`originalCharacter.value.imagePath: ${originalCharacter.value.imagePath}`)
        imagePreview.value = originalCharacter.value.imagePath
      }
    }
  }
})

const onSubmit = form.handleSubmit(async (values) => {
  isSubmitting.value = true
  try {
    if (values.imageID && imagePreview.value) {
      await characterStore.saveImage({id: values.imageID, path: imagePreview.value})
    }

    const characterId = Number(route.params.id)
    await characterStore.updateCharacter(characterId, values as Partial<CharacterRequest>)

    toast({
      title: t('characters.updateSuccess'),
      description: t('characters.updateSuccessDescription', {name: values.name}),
    })
    router.back()
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

const handleImageUpdate = async (image: { id: string, path: string }) => {
  if (characterId.value) {
    await characterStore.updateCharacterImage(characterId.value, {id: image.id, path: image.path})
    form.setFieldValue('imageID', image.id)
    imagePreview.value = image.path
  }
}

const handleCancel = () => {
  if (hasChanges.value) {
    showConfirmDialog.value = true
  } else {
    router.back()
  }
}

const confirmCancel = () => {
  showConfirmDialog.value = false
  router.back()
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
            :initial-image="imagePreview"
            :alt-text="t('characters.image.altText')"
            :character-id="characterId ?? undefined"
            @update:image="handleImageUpdate"
        />
        <div class="flex justify-between">
          <Button type="button" variant="outline" @click="handleCancel">
            {{ t('characters.cancel') }}
          </Button>
          <Button type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? t('characters.isSubmitting') : t('characters.updateCharacter') }}
          </Button>
        </div>
      </form>
    </div>
  </div>

  <Dialog v-model:open="showConfirmDialog">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>{{ t('characters.confirmCancel.title') }}</DialogTitle>
        <DialogDescription>
          {{ t('characters.confirmCancel.description') }}
        </DialogDescription>
      </DialogHeader>
      <DialogFooter>
        <Button variant="outline" @click="showConfirmDialog = false">
          {{ t('characters.confirmCancel.stay') }}
        </Button>
        <Button variant="destructive" @click="confirmCancel">
          {{ t('characters.confirmCancel.leave') }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>