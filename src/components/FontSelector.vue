<script setup lang="ts">
import {ref, onMounted} from 'vue';
import {invoke} from '@tauri-apps/api/core';
import {useI18n} from "vue-i18n";
import {Button} from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle
} from '@/components/ui/card'
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select'
import {useFontStore} from '~/stores/font.store'

const {t} = useI18n()
const fontStore = useFontStore()
const fonts = ref<string[]>([])

const getFonts = async () => {
  fonts.value = await invoke('get_fonts');
}

const changeFont = (font: string) => {
  fontStore.setFont(font)
}

const restoreDefault = () => {
  fontStore.restoreDefaultFont()
}

onMounted(() => {
  getFonts();
  fontStore.initFont();
})
</script>

<template>
  <div class="grid gap-6">
    <Card>
      <CardHeader>
        <CardTitle>
          {{ t('settings.options.fontFamily.title') }}
        </CardTitle>
        <CardDescription>
          {{ t('settings.options.fontFamily.description') }}
        </CardDescription>
      </CardHeader>
      <CardContent class="flex items-center space-x-4">
        <Select v-model="fontStore.currentFont" @update:modelValue="changeFont">
          <SelectTrigger class="w-[180px]">
            <SelectValue placeholder="Select a font"/>
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="font in fonts" :key="font" :value="font">
              {{ font }}
            </SelectItem>
          </SelectContent>
        </Select>
        <Button @click="restoreDefault" variant="outline">
          {{ t('settings.options.fontFamily.restore') }}
        </Button>
      </CardContent>
    </Card>
  </div>
</template>