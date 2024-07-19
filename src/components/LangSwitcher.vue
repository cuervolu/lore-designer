<script setup lang="ts">
import { ref, watch } from 'vue'
import type { LocaleObject } from "@nuxtjs/i18n";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

// Get active locale and supported locales
const { locale, locales } = useI18n()
// Cast to avoid TypeScript errors in template
const supportedLocales = locales.value as Array<LocaleObject>

const router = useRouter()
const switchLocalePath = useSwitchLocalePath()

const selectedLocale = ref(locale.value)

function onLocaleChanged(newLocale: string) {
  console.log('Switching locale to:', newLocale)
  selectedLocale.value = newLocale
}

watch(selectedLocale, (newLocale) => {
  if (newLocale !== locale.value) {
    router.push({ path: switchLocalePath(newLocale) })
  }
})
</script>

<template>
  <Select v-model="selectedLocale" @update:modelValue="onLocaleChanged">
    <SelectTrigger class="w-[180px]">
      <SelectValue :placeholder="$t('selectLanguage')" />
    </SelectTrigger>
    <SelectContent>
      <SelectGroup>
        <SelectItem v-for="loc in supportedLocales" :key="loc.code" :value="loc.code">
          {{ loc.name }}
        </SelectItem>
      </SelectGroup>
    </SelectContent>
  </Select>
</template>

<style scoped>
</style>