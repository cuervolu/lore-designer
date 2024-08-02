<script setup lang="ts">
import { invoke } from '@tauri-apps/api/core'
import { relaunch, exit } from '@tauri-apps/plugin-process'
import { ref, watch, computed } from 'vue'
import { useMagicKeys } from '@vueuse/core'
import { useRouter } from 'vue-router'
import { useCharacterStore } from '~/stores/character.store'
import { useFontStore } from '~/stores/font.store'
import { useLanguageStore } from '~/stores/language.store'
import {
  CommandDialog,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  CommandSeparator,
} from '@/components/ui/command'
import type { CharacterForNode } from "~/interfaces"

const open = ref(false)
const router = useRouter()
const { t } = useI18n()
const localePath = useLocalePath()
const characterStore = useCharacterStore()
const fontStore = useFontStore()
const languageStore = useLanguageStore()

const { Meta_P, Ctrl_P } = useMagicKeys({
  passive: false,
  onEventFired(e) {
    if (e.key === 'p' && (e.metaKey || e.ctrlKey))
      e.preventDefault()
  },
})

watch([Meta_P, Ctrl_P], (v) => {
  if (v[0] || v[1])
    handleOpenChange()
})

function handleOpenChange() {
  open.value = !open.value
}

const navigateTo = (path: string) => {
  router.push(localePath(path))
  open.value = false
}

const changeLanguage = (lang: string) => {
  languageStore.setLanguage(lang)
  open.value = false
}

const changeFont = (font: string) => {
  fontStore.setFont(font)
  open.value = false
}

const relaunchApp = async () => {
  await relaunch()
}

const exitApp = async () => {
  await exit(0)
}

const openExternalLink = async (url: string) => {
  await invoke('open_browser', { url })
  open.value = false
}

const characters = ref<CharacterForNode[]>([])
const fonts = ref<string[]>([])
const searchQuery = ref('')
const charactersPage = ref(1)
const fontsPage = ref(1)
const itemsPerPage = 20
const totalCharacters = ref(0)
const totalFonts = ref(0)

const loadCharacters = async () => {
  const { characters: loadedCharacters, total } = await characterStore.getCharactersNames(charactersPage.value, itemsPerPage)
  characters.value = [...characters.value, ...loadedCharacters]
  totalCharacters.value = total
}

const loadFonts = async () => {
  const { fonts: loadedFonts, total } = await fontStore.getAvailableFonts(fontsPage.value, itemsPerPage)
  fonts.value = [...fonts.value, ...loadedFonts]
  totalFonts.value = total
}

const filteredCharacters = computed(() => {
  return characters.value.filter(character =>
      character.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const filteredFonts = computed(() => {
  return fonts.value.filter(font =>
      font.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

const loadMoreCharacters = async () => {
  charactersPage.value++
  await loadCharacters()
}

const loadMoreFonts = async () => {
  fontsPage.value++
  await loadFonts()
}

const hasMoreCharacters = computed(() => characters.value.length < totalCharacters.value)
const hasMoreFonts = computed(() => fonts.value.length < totalFonts.value)

watch(open, async (newValue) => {
  if (newValue) {
    charactersPage.value = 1
    fontsPage.value = 1
    characters.value = []
    fonts.value = []
    await Promise.all([loadCharacters(), loadFonts()])
  }
})

watch(searchQuery, () => {
  charactersPage.value = 1
  fontsPage.value = 1
})
</script>

<template>
  <CommandDialog v-model:open="open">
    <CommandInput v-model="searchQuery" :placeholder="t('globalCommand.searchPlaceholder')" />
    <CommandList class="max-h-[300px] overflow-y-auto">
      <CommandEmpty>{{ t('globalCommand.noResults') }}</CommandEmpty>

      <CommandGroup :heading="t('globalCommand.characters')">
        <CommandItem
            v-for="character in filteredCharacters"
            :key="character.id"
            :value="character.id.toString()"
            @select="() => navigateTo(`/characters/${character.id}`)"
        >
          <div class="flex items-center">
            <img v-if="character.imagePath" :src="character.imagePath" alt="Character" class="w-6 h-6 rounded-full mr-2" />
            <span>{{ character.name }}</span>
          </div>
        </CommandItem>
        <CommandItem v-if="hasMoreCharacters" :value="'load-more-characters'" @select="loadMoreCharacters">
          {{ t('globalCommand.loadMore') }}
        </CommandItem>
      </CommandGroup>

      <CommandSeparator />

      <CommandGroup :heading="t('globalCommand.fonts')">
        <CommandItem
            v-for="font in filteredFonts"
            :key="font"
            :value="font"
            @select="() => changeFont(font)"
        >
          {{ font }}
        </CommandItem>
        <CommandItem v-if="hasMoreFonts" :value="'load-more-fonts'" @select="loadMoreFonts">
          {{ t('globalCommand.loadMore') }}
        </CommandItem>
      </CommandGroup>

      <CommandSeparator />

      <CommandGroup :heading="t('globalCommand.navigation')">
        <CommandItem value="dialogue" @select="() => navigateTo('/dialogue')">
          {{ t('sidebar.dialogue') }}
        </CommandItem>
        <CommandItem value="projects" @select="() => navigateTo('/projects')">
          {{ t('sidebar.projects') }}
        </CommandItem>
        <CommandItem value="settings" @select="() => navigateTo('/settings')">
          {{ t('sidebar.settings') }}
        </CommandItem>
      </CommandGroup>

      <CommandSeparator />

      <CommandGroup :heading="t('globalCommand.language')">
        <CommandItem value="en" @select="() => changeLanguage('en')">
          {{ t('globalCommand.english') }}
        </CommandItem>
        <CommandItem value="es" @select="() => changeLanguage('es')">
          {{ t('globalCommand.spanish') }}
        </CommandItem>
      </CommandGroup>

      <CommandSeparator />

      <CommandGroup :heading="t('globalCommand.actions')">
        <CommandItem value="relaunch" @select="relaunchApp">
          {{ t('globalCommand.relaunch') }}
        </CommandItem>
        <CommandItem value="exit" @select="exitApp">
          {{ t('globalCommand.exit') }}
        </CommandItem>
      </CommandGroup>

      <CommandSeparator />

      <CommandGroup :heading="t('globalCommand.external')">
        <CommandItem value="github" @select="() => openExternalLink('https://github.com/cuervolu/lore-designer')">
          {{ t('globalCommand.github') }}
        </CommandItem>
        <CommandItem value="docs" @select="() => openExternalLink('https://github.com/cuervolu/lore-designer/wiki')">
          {{ t('globalCommand.documentation') }}
        </CommandItem>
      </CommandGroup>
    </CommandList>
  </CommandDialog>
</template>