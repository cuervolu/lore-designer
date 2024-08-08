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
const localeRoute = useLocaleRoute()
const projectStore = useProjectStore()
const {toast} = useToast()

const formSchema = toTypedSchema(z.object({
  name: z.string({required_error: t('createProjectDialog.name.required_error')}),
}))

const form = useForm({
  validationSchema: formSchema,
})

const isSubmitting = ref(false)

const onSubmit = form.handleSubmit(async (values) => {
  isSubmitting.value = true
  try {
    const projectId = await projectStore.createProject(values)
    if (projectId) {
      toast({
        title: t('projects.createSuccess'),
        description: t('projects.createSuccessDescription', {name: values.name}),
      })
      const route = localeRoute({
        name: 'projects',
        params: {projectId},
      })
      if (route) {
        return navigateTo(route.fullPath)
      }
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
</script>

<template>
  <Dialog>
    <DialogTrigger as-child>
      <Button class="flex mx-auto mt-16">
        {{ t('getStarted') }}
      </Button>
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
          <Button type="submit" :disabled="isSubmitting">
            {{ isSubmitting ? t('submitting') : t('createProjectDialog.submit') }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>

<style scoped>
</style>