<script setup lang="ts">
import {ref} from 'vue'
import {useForm} from 'vee-validate'
import {toTypedSchema} from '@vee-validate/zod'
import * as z from 'zod'
import {Button} from '~/components/ui/button'
import {
  Dialog,
  DialogFooter,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '~/components/ui/dialog'
import {useToast} from '~/components/ui/toast'
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '~/components/ui/form'
import {Input} from '~/components/ui/input'
import {useProjectStore} from '~/stores/project.store'

const {t} = useI18n()
const projectStore = useProjectStore()
const {toast} = useToast()

const emit = defineEmits(['projectCreated'])

const formSchema = toTypedSchema(z.object({
  name: z.string({required_error: t('createProjectDialog.name.required_error')}),
}))

const form = useForm({
  validationSchema: formSchema,
})

const isSubmitting = ref(false)
const isOpen = ref(false)

const onSubmit = form.handleSubmit(async (values) => {
  isSubmitting.value = true
  try {
    const projectId = await projectStore.createProject(values)
    if (projectId) {
      toast({
        title: t('projects.createSuccess'),
        description: t('projects.createSuccessDescription', {name: values.name}),
      })
      isOpen.value = false
      emit('projectCreated', projectId)
    } else {
      toast({
        title: t('projects.createError'),
        description: t('projects.createErrorDescription'),
        variant: 'destructive',
      })
    }
  } catch (error) {
    console.error('Error creating project:', error)
    toast({
      title: t('projects.createError'),
      description: t('projects.createErrorDescription'),
      variant: 'destructive',
    })
  } finally {
    isSubmitting.value = false
  }
})

const openDialog = () => {
  isOpen.value = true
}

const closeDialog = () => {
  isOpen.value = false
  form.resetForm()
}
</script>

<template>
  <Dialog v-model:open="isOpen">
    <DialogTrigger as-child @click="openDialog">
      <slot>
        <Button>
          {{ t('projects.createProject') }}
        </Button>
      </slot>
    </DialogTrigger>
    <DialogContent class="sm:max-w-[425px]">
      <form @submit="onSubmit">
        <DialogHeader>
          <DialogTitle>{{ t('createProjectDialog.title') }}</DialogTitle>
          <DialogDescription>
            {{ t('createProjectDialog.description') }}
          </DialogDescription>
        </DialogHeader>
        <div class="grid gap-4 py-4">
          <FormField v-slot="{ componentField }" name="name"
                     class="grid grid-cols-4 items-center gap-4">
            <FormItem>
              <FormLabel>{{ t('createProjectDialog.name.label') }}</FormLabel>
              <FormControl>
                <Input type="text" :placeholder="t('createProjectDialog.name.placeholder')"
                       v-bind="componentField"/>
              </FormControl>
              <FormMessage/>
            </FormItem>
          </FormField>
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" @click="closeDialog">
            {{ t('cancel') }}
          </Button>
          <Button type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? t('submitting') : t('createProjectDialog.submit') }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>