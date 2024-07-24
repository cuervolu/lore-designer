import { useFontStore } from '~/stores/font.store'
import { invoke } from '@tauri-apps/api/core'

export default defineNuxtPlugin(() => {
  return {
    provide: {
      initFonts: async () => {
        const fontStore = useFontStore()

        // Initialize the font store
        await fontStore.initFont()

        // Get fonts from the system
        const fonts = await invoke('get_fonts') as string[]
        fontStore.setAvailableFonts(fonts)

        // Apply the current font
        document.body.style.fontFamily = fontStore.currentFont
      }
    }
  }
})