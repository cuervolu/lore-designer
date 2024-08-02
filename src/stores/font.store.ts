import {Store} from '@tauri-apps/plugin-store';
import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useFontStore = defineStore('font', () => {
  const store = new Store('store.bin');
  const currentFont = ref('')
  const defaultFont = "'Inter Variable', sans-serif"
  const availableFonts = ref<string[]>([])

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

  const setAvailableFonts = (fonts: string[]) => {
    availableFonts.value = fonts
  }

  const getAvailableFonts = async (page: number = 1, pageSize: number = 10): Promise<{ fonts: string[], total: number }> => {
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const fonts = availableFonts.value.slice(start, end);
    return { fonts, total: availableFonts.value.length };
  }
  return {
    currentFont,
    setFont,
    initFont,
    restoreDefaultFont,
    defaultFont,
    availableFonts,
    getAvailableFonts,
    setAvailableFonts
  }
})