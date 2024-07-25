<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {formatDistanceToNow, isEqual, parseISO} from 'date-fns'
import {Button} from "~/components/ui/button"
import {Card, CardContent} from "~/components/ui/card"
import {Label} from "~/components/ui/label"
import {Textarea} from "~/components/ui/textarea"
import {Skeleton} from "~/components/ui/skeleton"
import {useCharacterStore} from '~/stores/character.store'
import type {Character} from '~/interfaces/models'
import noPhoto from 'assets/img/no_photo.webp'

const route = useRoute()
const router = useRouter()
const localePath = useLocalePath()
const characterStore = useCharacterStore()
const character = ref<Character | null>(null)
const isLoading = ref(true)

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

const formattedDate = computed(() => {
  if (!character.value) return ''

  const createdAt = parseISO(character.value.createdAt)
  const updatedAt = parseISO(character.value.updatedAt)

  if (isEqual(createdAt, updatedAt)) {
    return `Created ${formatDistanceToNow(createdAt, {addSuffix: true})}`
  } else {
    return `Updated ${formatDistanceToNow(updatedAt, {addSuffix: true})}`
  }
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
              <p v-else class="text-lg text-muted-foreground mb-4">{{ character?.role }}</p>

              <div class="mb-4">
                <Label class="text-sm font-medium">Description</Label>
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
                <Label class="text-sm font-medium">Additional Notes</Label>
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
              <Skeleton v-if="isLoading" class="h-10 w-32"/>
              <Button v-else @click="handleEdit">Edit Character</Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>