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
    // La imagen ya se ha subido previamente si existe values.imageID
    if (values.imageID) {
      // Guardamos la información de la imagen en la base de datos
      await characterStore.saveImage({ id: values.imageID, path: imagePreview.value! });
    }

    const characterId = await characterStore.createCharacter(values as CharacterRequest)
    if (characterId) {
      toast({
        title: 'Character Created',
        description: `${values.name} has been successfully created.`,
      })
      // await router.push('/characters')
    } else {
      throw new Error('Failed to create character')
    }
  } catch (error) {
    console.error('Error creating character:', error);
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
  <div class="flex w-full max-w-4xl gap-6">
    <div class="flex-1 space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Create Character</CardTitle>
          <CardDescription>Fill out the form to create a new character.</CardDescription>
        </CardHeader>
        <form @submit="onSubmit">
          <CardContent class="grid gap-4">
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">

              <FormField v-slot="{ componentField }" name="name" class="space-y-2">
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input type="text" placeholder="Enter character name" v-bind="componentField"/>
                  </FormControl>
                  <FormDescription>
                    The name of the character.
                  </FormDescription>
                  <FormMessage/>
                </FormItem>
              </FormField>

              <FormField v-slot="{ componentField }" name="role" class="space-y-2">
                <FormItem>
                  <FormLabel>Role</FormLabel>

                  <Select v-bind="componentField">
                    <FormControl>
                      <SelectTrigger>
                        <SelectValue placeholder="Select a role"/>
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectGroup>
                        <SelectItem value="Primary">Primary</SelectItem>
                        <SelectItem value="Secondary">Secondary</SelectItem>
                        <SelectItem value="Tertiary">Tertiary</SelectItem>
                        <SelectItem value="Undefined">Undefined</SelectItem>
                      </SelectGroup>
                    </SelectContent>
                  </Select>
                  <FormDescription>
                    The role of the character.
                  </FormDescription>
                  <FormMessage/>
                </FormItem>
              </FormField>
            </div>
            <FormField v-slot="{ componentField }" name="description" class="space-y-2">
              <FormItem>
                <FormLabel>Description</FormLabel>
                <FormControl>
                  <Textarea placeholder="A brief description of the character" class="min-h-[100px]"
                            v-bind="componentField"/>
                </FormControl>
                <FormDescription>
                  A brief description of the character. (Optional)
                </FormDescription>
                <FormMessage/>
              </FormItem>
            </FormField>
            <FormField v-slot="{ componentField }" name="additionalNotes" class="space-y-2">
              <FormItem>
                <FormLabel>Additional Note</FormLabel>
                <FormControl>
                  <Textarea placeholder="Some other details of the character" class="min-h-[100px]"
                            v-bind="componentField"/>
                </FormControl>
                <FormDescription>
                  Some other details of the character. (Optional)
                </FormDescription>
                <FormMessage/>
              </FormItem>
            </FormField>
          </CardContent>
          <CardFooter class="flex justify-end">
            <Button type="submit" :disabled="isSubmitting">
              {{ isSubmitting ? 'Creating...' : 'Create Character' }}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
    <div class="flex-1 space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Character Image</CardTitle>
          <CardDescription>Upload an image to represent your character.</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="grid gap-4">
            <div v-if="!imagePreview" class="flex items-center justify-center rounded-md border border-dashed p-12">
              <div class="text-center">
                <div class="mx-auto h-12 w-12 text-muted-foreground"/>
                <div class="mt-4 font-medium">Click to upload</div>
                <p class="mt-2 text-sm text-muted-foreground">PNG, JPG, GIF up to 10MB</p>
                <Button @click="uploadImage" class="mt-4">Upload Image</Button>
              </div>
            </div>
            <div v-else class="relative">
              <img :src="imagePreview" alt="Character Preview" class="w-full h-auto rounded-md" />
              <Button @click="uploadImage" class="absolute bottom-2 right-2">Change Image</Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<style scoped>

</style>