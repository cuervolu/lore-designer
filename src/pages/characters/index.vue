<script setup lang="ts">
import {ref, onMounted} from 'vue';
import CharacterCard from "~/components/CharacterCard.vue";
import CustomBreadcrumb from "~/components/CustomBreadcrumb.vue";
import {Skeleton} from '@/components/ui/skeleton';
import type {Character} from "~/interfaces/models";
import {useCharacterStore} from "~/stores";
import {useErrorHandler} from '~/composables/useErrorHandler';
import {DatabaseError} from '~/exceptions/db.error';

const characterStore = useCharacterStore();
const {handleError} = useErrorHandler();
const characters = ref<Character[]>([]);
const isLoading = ref(true);
const router = useRouter();
onMounted(async () => {
  try {
    characters.value = await characterStore.getCharacters();
  } catch (error) {
    handleError(new DatabaseError({
      name: 'DB_QUERY_ERROR',
      message: 'Failed to fetch characters',
      cause: error
    }));
  } finally {
    isLoading.value = false;
  }
});

const navigateToCreateCharacter = () => {
  router.push('/characters/create');
};
</script>

<template>
  <div class="p-6">
    <custom-breadcrumb new-route="/characters" from="home" to="characters.title" old-route="/"/>
    <div v-if="isLoading" class="py-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
      <div v-for="i in 6" :key="i" class="space-y-3">
        <Skeleton class="h-[125px] w-[200px] rounded-xl"/>
        <div class="space-y-2">
          <Skeleton class="h-4 w-[150px]"/>
          <Skeleton class="h-4 w-[100px]"/>
        </div>
      </div>
    </div>
    <div v-else-if="characters.length === 0" class="flex flex-col items-center justify-center h-[50vh]">
      <p class="text-xl mb-4">No characters found</p>
      <Button @click="navigateToCreateCharacter">Create a character</Button>
    </div>
    <div v-else class="py-3 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-4">
      <CharacterCard
          v-for="character in characters"
          :id="character.id"
          :key="character.id"
          :name="character.name"
          :image-path="character.imagePath"
          :description="character.description"
          :additionalNotes="character.additionalNotes"
          :role="character.role"
          :footer="character.role"
      />
      <Button @click="navigateToCreateCharacter">Create a character</Button>
    </div>
  </div>
</template>