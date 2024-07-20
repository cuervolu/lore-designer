import { Store } from '@tauri-apps/plugin-store';
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLanguageStore = defineStore('language', () => {
  const store = new Store('store.bin');
  const currentLanguage = ref('')
  const defaultLanguage = 'en' 

  const setLanguage = async (lang: string) => {
    currentLanguage.value = lang
    await store.set('selectedLanguage', lang)
    await store.save();
  }

  const initLanguage = async () => {
    const savedLanguage = await store.get<string>('selectedLanguage')
    if (savedLanguage) {
      await setLanguage(savedLanguage)
    } else {
      await setLanguage(defaultLanguage)
    }
  }

  const restoreDefaultLanguage = async () => {
    await setLanguage(defaultLanguage)
    await store.delete('selectedLanguage');
    await store.save();
  }

  return {
    currentLanguage,
    setLanguage,
    initLanguage,
    restoreDefaultLanguage,
    defaultLanguage
  }
})