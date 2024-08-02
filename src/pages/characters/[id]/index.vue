<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {formatDistanceToNow, parseISO, differenceInSeconds} from 'date-fns'
import {es, enUS} from 'date-fns/locale'
import {Button} from "~/components/ui/button"
import {Card, CardContent} from "~/components/ui/card"
import {Label} from "~/components/ui/label"
import {Textarea} from "~/components/ui/textarea"
import {Skeleton} from "~/components/ui/skeleton"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {useCharacterStore} from '~/stores/character.store'
import {useToast} from '@/components/ui/toast'
import type {Character} from '~/interfaces/models'
import noPhoto from 'assets/img/no_photo.webp'

const route = useRoute()
const router = useRouter()
const localePath = useLocalePath()
const characterStore = useCharacterStore()
const {toast} = useToast()
const {t, locale} = useI18n()
const { translateRole } = useCharacterRole()
const character = ref<Character | null>(null)
const isLoading = ref(true)
const showDeleteDialog = ref(false)

onMounted(async () => {
  const characterId = Number(route.params.id)
  if (!isNaN(characterId)) {
    character.value = await characterStore.getCharacterById(characterId)
    isLoading.value = false
  }
})

const handleEdit = async () => {
  try {
    await router.push(localePath(`/characters/${route.params.id}/edit`))
  } catch (error) {
    console.error('Navigation failed', error)
  }
}

const handleDelete = () => {
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  try {
    if (character.value) {
      await characterStore.deleteCharacter(character.value.id)
      toast({
        title: t('characters.deleteSuccess'),
        description: t('characters.deleteSuccessDescription', {name: character.value.name}),
      })
      await router.push(localePath('/characters'))
    }
  } catch (error) {
    console.error('Error deleting character:', error)
    toast({
      title: t('characters.deleteError'),
      description: t('characters.deleteErrorDescription'),
      variant: 'destructive',
    })
  } finally {
    showDeleteDialog.value = false
  }
}
const formattedDate = computed(() => {

  if (!character.value) return ''

  const createdAt = parseISO(character.value.createdAt)
  const updatedAt = parseISO(character.value.updatedAt)
  const now = new Date()

  const dateLocale = locale.value === 'es' ? es : enUS

  const formatDate = (date: Date) => {
    const secondsDiff = differenceInSeconds(now, date)

    if (secondsDiff < 60) {
      return t('time.justNow')
    } else {
      return formatDistanceToNow(date, {addSuffix: true, locale: dateLocale})
    }
  }

  if (differenceInSeconds(updatedAt, createdAt) <= 1) {
    return t('time.created', {time: formatDate(createdAt)})
  } else {
    return t('time.updated', {time: formatDate(updatedAt)})
  }
})

const translatedRole = computed(() => {
  return character.value ? translateRole(character.value.role) : ''
})



</script>

<template>
  <div class="container mx-auto p-4 max-w-4xl">
    <Card class="overflow-hidden">
      <CardContent class="p-0">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div class="p-6">
            <Skeleton v-if="isLoading" class="w-full h-64 rounded-lg"/>
            <img
                v-else
                :src="character?.imagePath || noPhoto"
                :alt="character?.name"
                class="w-full h-64 object-cover rounded-lg shadow-md"
            />
          </div>
          <div class="p-6 flex flex-col justify-between">
            <div>
              <Skeleton v-if="isLoading" class="h-9 w-3/4 mb-2"/>
              <h1 v-else class="text-3xl font-bold mb-2">{{ character?.name }}</h1>
              <Skeleton v-if="isLoading" class="h-6 w-1/2 mb-4"/>
              <p v-else class="text-lg text-muted-foreground mb-4">{{ translatedRole }}</p>
              <div class="mb-4">
                <Label class="text-sm font-medium">
                  {{ t('characters.description') }}
                </Label>
                <Skeleton v-if="isLoading" class="h-24 w-full mt-1"/>
                <Textarea
                    v-else
                    :placeholder="character?.description"
                    readonly
                    class="mt-1 bg-secondary"
                    rows="3"
                />
              </div>
              <div class="mb-4">
                <Label class="text-sm font-medium">
                  {{ t('characters.additionalNotes') }}
                </Label>
                <Skeleton v-if="isLoading" class="h-24 w-full mt-1"/>
                <Textarea
                    v-else
                    :placeholder="character?.additionalNotes"
                    readonly
                    class="mt-1 bg-secondary"
                    rows="3"
                />
              </div>
            </div>
            <div class="flex justify-between items-center mt-4">
              <div v-if="isLoading" class="flex flex-col gap-2">
                <Skeleton class="h-4 w-32"/>
              </div>
              <div v-else class="text-sm text-muted-foreground">
                <p>{{ formattedDate }}</p>
              </div>
              <div v-if="!isLoading" class="flex flex-col space-y-2 mt-4">
                <Button @click="handleEdit" variant="outline">
                  {{ t('characters.editCharacter') }}
                </Button>
                <Button variant="destructive" @click="handleDelete">
                  {{ t('characters.confirmDelete.confirm') }}
                </Button>
              </div>
              <Skeleton v-else class="h-20 w-32"/>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>

    <Dialog v-model:open="showDeleteDialog">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{{ t('characters.confirmDelete.title') }}</DialogTitle>
          <DialogDescription>
            {{ t('characters.confirmDelete.description') }}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" @click="showDeleteDialog = false">
            {{ t('characters.confirmDelete.cancel') }}
          </Button>
          <Button variant="destructive" @click="confirmDelete">
            {{ t('characters.confirmDelete.confirm') }}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>