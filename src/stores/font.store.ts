import {Store} from '@tauri-apps/plugin-store';
import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useFontStore = defineStore('font', () => {
  const store = new Store('store.bin');
  const currentFont = ref('')
  const defaultFont = "'Inter Variable', sans-serif"

  const setFont = async (font: string) => {
    currentFont.value = font
    document.body.style.fontFamily = font
    await store.set('selectedFont', font)
    await store.save();
  }

  const initFont = async () => {
    const savedFont = await store.get<string>('selectedFont')
    if (savedFont) {
      await setFont(savedFont)
    } else {
      await setFont(defaultFont)
    }
  }

  const restoreDefaultFont = async () => {
    await setFont(defaultFont)
    await store.delete('selectedFont')
    await store.save();
  }

  return {
    currentFont,
    setFont,
    initFont,
    restoreDefaultFont,
    defaultFont
  }
})