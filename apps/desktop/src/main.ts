import './assets/global.css'

import { error } from '@tauri-apps/plugin-log'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createI18n } from 'vue-i18n'
import '@fontsource-variable/inter'
import '@fontsource-variable/fira-code';

import esMessages from './locales/es.json'
import enMessages from './locales/en.json'

import App from './App.vue'
import router from './router'
import { usePreferencesStore } from '@common/stores/preferences.store'

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'es',
  messages: {
    es: esMessages,
    en: enMessages,
  },
})

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(router)
  app.use(i18n)

  app.mount('#app')

  // Load preferences after the app is mounted
  const preferencesStore = usePreferencesStore()
  await preferencesStore.loadPreferences()
}

initializeApp().catch(async (err) => await error(`Error initializing app: ${err}`))
