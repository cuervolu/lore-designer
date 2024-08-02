<script setup lang="ts">
import {ref, onMounted, computed, watch} from 'vue';
import CharacterCard from "~/components/CharacterCard.vue";
import CustomBreadcrumb from "~/components/CustomBreadcrumb.vue";
import {Skeleton} from '@/components/ui/skeleton';
import {Button} from '@/components/ui/button';
import {
  Pagination,
  PaginationEllipsis,
  PaginationList,
  PaginationListItem,
  PaginationNext,
  PaginationPrev,
} from '@/components/ui/pagination'
import {useCharacterStore} from "~/stores";
import {useErrorHandler} from '~/composables/useErrorHandler';
import {DatabaseError} from '~/exceptions/db.error';


const {t} = useI18n()
const localeRoute = useLocaleRoute()
const characterStore = useCharacterStore();
const {characters, totalCharacters} = storeToRefs(characterStore);
const {handleError} = useErrorHandler();
const router = useRouter();
const localePath = useLocalePath()


const isLoading = ref(true);
const currentPage = ref(1);
const pageSize = ref(20);
const totalPages = computed(() => Math.ceil(totalCharacters.value / pageSize.value));

const fetchCharacters = async (force = false) => {
  isLoading.value = true;
  try {
    await characterStore.fetchCharacters(currentPage.value, pageSize.value, force);
  } catch (error) {
    handleError(new DatabaseError({
      name: 'DB_QUERY_ERROR',
      message: 'Failed to fetch characters',
      cause: error
    }));
  } finally {
    isLoading.value = false;
  }
};

onMounted(async () => {
  await fetchCharacters();
});

watch(currentPage, () => fetchCharacters(true));


const navigateToCreateCharacter = () => {
  const route = localeRoute('characters-create');
  if (route) {
    return navigateTo(route.fullPath)
  }
};

const changePage = (page: number) => {
  currentPage.value = page;
};


type PageItem = number | '...';

const visiblePages = computed((): PageItem[] => {
  const delta = 2;
  const range: PageItem[] = [];
  for (
      let i = Math.max(2, currentPage.value - delta);
      i <= Math.min(totalPages.value - 1, currentPage.value + delta);
      i++
  ) {
    range.push(i);
  }

  if (currentPage.value - delta > 2) {
    range.unshift("...");
  }
  if (currentPage.value + delta < totalPages.value - 1) {
    range.push("...");
  }

  range.unshift(1);
  if (totalPages.value !== 1) {
    range.push(totalPages.value);
  }

  return range;
});
</script>


<template>
  <div class="p-6 max-w-7xl mx-auto">
    <custom-breadcrumb new-route="/characters" from="home" to="characters.title" old-route="/"/>

    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold">{{ t('characters.title') }}</h1>
      <Button @click="navigateToCreateCharacter">{{ t('characters.createCharacter') }}</Button>
    </div>

    <div v-if="isLoading"
         class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
      <div v-for="i in pageSize" :key="i" class="space-y-3">
        <Skeleton class="h-[200px] w-full rounded-xl"/>
        <div class="space-y-2">
          <Skeleton class="h-4 w-3/4"/>
          <Skeleton class="h-4 w-1/2"/>
        </div>
      </div>
    </div>

    <div v-else-if="characters.length === 0"
         class="flex flex-col items-center justify-center h-[50vh]">
      <p class="text-xl mb-4">
        {{ t('characters.noCharacters') }}
      </p>
      <Button>
        <NuxtLink :to="localePath('/characters/create')">
          {{ t('characters.createCharacter') }}
        </NuxtLink>
      </Button>
    </div>

    <div v-else
         class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
      <CharacterCard
          v-for="character in characters"
          :key="character.id"
          :id="character.id"
          :name="character.name"
          :image-path="character.imagePath"
          :description="character.description"
          :additional-notes="character.additionalNotes"
          :role="character.role"
          :footer="character.role"
      />
    </div>

    <Pagination v-if="totalPages > 1" class="mt-6">
      <PaginationPrev
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
      />

      <PaginationList>
        <template v-for="(page, index) in visiblePages" :key="index">
          <PaginationListItem
              v-if="typeof page === 'number'"
              :value="page"
              :active="page === currentPage"
              @click="changePage(page)"
          >
            {{ page }}
          </PaginationListItem>
          <PaginationEllipsis v-else/>
        </template>
      </PaginationList>

      <PaginationNext
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
      />
    </Pagination>
  </div>
</template>