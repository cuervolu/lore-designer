import './assets/global.css'

import { error } from '@tauri-apps/plugin-log'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@fontsource-variable/inter'
import '@fontsource-variable/fira-code';

import App from './App.vue'
import router from './router'
import { usePreferencesStore } from '@common/stores/preferences.store'

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(router)

  app.mount('#app')

  // Load preferences after the app is mounted
  const preferencesStore = usePreferencesStore()
  await preferencesStore.loadPreferences()
}

initializeApp().catch(async (err) => await error(`Error initializing app: ${err}`))
