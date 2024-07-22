<script setup lang="ts">
import { useCharacterStore } from '~/stores/character.store';
import { useRoute } from 'vue-router';
import { ref, onMounted } from 'vue';
import type { Character } from '~/interfaces/models';
import noPhoto from '~/assets/img/no_photo.webp';

const route = useRoute();
const characterStore = useCharacterStore();
const character = ref<Character | null>(null);

onMounted(async () => {
  const characterId = Number(route.params.id);
  if (!isNaN(characterId)) {
    character.value = await characterStore.getCharacterById(characterId);
  }
});
</script>

<template>
  <div v-if="character" class="container mx-auto p-4">
    <h1 class="text-2xl font-bold mb-4">{{ character.name }}</h1>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div>
        <img
            :src="character.imagePath || noPhoto"
            :alt="character.name"
            class="w-full h-auto rounded-lg shadow-lg"
        />
      </div>
      <div>
        <p class="mb-2"><strong>Role:</strong> {{ character.role }}</p>
        <p class="mb-2"><strong>Description:</strong> {{ character.description }}</p>
        <p class="mb-2"><strong>Additional Notes:</strong> {{ character.additionalNotes }}</p>
        <p class="mb-2"><strong>Created At:</strong> {{ new Date(character.createdAt).toLocaleString() }}</p>
        <p class="mb-2"><strong>Updated At:</strong> {{ new Date(character.updatedAt).toLocaleString() }}</p>
      </div>
    </div>
  </div>
  <div v-else class="container mx-auto p-4">
    <p>Loading character details...</p>
  </div>
</template>