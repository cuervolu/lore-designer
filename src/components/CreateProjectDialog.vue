<script setup lang="ts">
import {h} from 'vue'
import {useForm} from 'vee-validate'
import {toTypedSchema} from '@vee-validate/zod'
import * as z from 'zod'
import {Button} from '@/components/ui/button'
import {
  Dialog,
  DialogFooter,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {toast} from '@/components/ui/toast'
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage
} from '@/components/ui/form'
import {Input} from '@/components/ui/input'


const {t} = useI18n()

const formSchema = toTypedSchema(z.object({
  name: z.string({required_error: t('createProjectDialog.name.required_error')}),
}))

const {handleSubmit} = useForm({
  validationSchema: formSchema,
})

const onSubmit = handleSubmit((values) => {
  toast({
    title: 'You submitted the following values:',
    description: h('pre', {class: 'mt-2 w-[340px] rounded-md bg-slate-950 p-4'}, h('code', {class: 'text-white'}, JSON.stringify(values, null, 2))),
  })
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
                <Input type="text" :placeholder=" t('createProjectDialog.name.placeholder')"
                       v-bind="componentField"/>
              </FormControl>
              <FormMessage/>
            </FormItem>
          </FormField>
        </div>
        <DialogFooter>
          <Button type="submit">
            {{ t('createProjectDialog.submit') }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>

</template>

<style scoped>

</style>