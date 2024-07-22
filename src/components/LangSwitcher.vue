<script setup lang="ts">
import {ref, watch, onMounted} from 'vue'
import type {LocaleObject} from "@nuxtjs/i18n";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle
} from "~/components/ui/card";
import {Button} from "~/components/ui/button";
import {useLanguageStore} from "~/stores/language.store";

const {t} = useI18n()
const {locale, locales} = useI18n()
const supportedLocales = locales.value as Array<LocaleObject>
const router = useRouter()
const switchLocalePath = useSwitchLocalePath()
const languageStore = useLanguageStore()

const selectedLocale = ref(locale.value)

async function onLocaleChanged(newLocale: string) {
  console.log('Switching locale to:', newLocale)
  selectedLocale.value = newLocale
  await languageStore.setLanguage(newLocale)
}

watch(selectedLocale, (newLocale) => {
  if (newLocale !== locale.value) {
    router.push({path: switchLocalePath(newLocale)})
  }
})

const restoreDefaultLanguage = async () => {
  await languageStore.restoreDefaultLanguage()
  selectedLocale.value = languageStore.defaultLanguage
  await router.push({path: switchLocalePath(languageStore.defaultLanguage)})
}

onMounted(async () => {
  await languageStore.initLanguage()
  selectedLocale.value = languageStore.currentLanguage
  if (selectedLocale.value !== locale.value) {
    await router.push({path: switchLocalePath(selectedLocale.value)})
  }
})
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>
        {{ t('settings.options.language.title') }}
      </CardTitle>
      <CardDescription>
        {{ t('settings.options.language.description') }}
      </CardDescription>
    </CardHeader>
    <CardContent class="flex items-center space-x-4">
      <Select v-model="selectedLocale" @update:modelValue="onLocaleChanged">
        <SelectTrigger class="w-[180px]">
          <SelectValue :placeholder="$t('selectLanguage')"/>
        </SelectTrigger>
        <SelectContent>
          <SelectGroup>
            <SelectItem v-for="loc in supportedLocales" :key="loc.code" :value="loc.code">
              {{ loc.name }}
            </SelectItem>
          </SelectGroup>
        </SelectContent>
      </Select>
      <Button @click="restoreDefaultLanguage" variant="outline">{{ t('settings.options.language.restore') }}</Button>
    </CardContent>
  </Card>
</template>