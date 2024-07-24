import { useLanguageStore } from '~/stores/language.store'

export default defineNuxtPlugin(() => {
  return {
    provide: {
      initI18n: async () => {
        const languageStore = useLanguageStore()
        const { locale } = useI18n()

        // Initialize the language store
        await languageStore.initLanguage()

        // Set the locale
        locale.value = languageStore.currentLanguage

        // Watch for changes in the store and update the locale
        watch(() => languageStore.currentLanguage, (newLang) => {
          locale.value = newLang
        })
      }
    }
  }
})