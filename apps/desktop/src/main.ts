import './assets/global.css'
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'

import { error } from 'tauri-plugin-tracing'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import VueVirtualScroller from 'vue-virtual-scroller'
import '@fontsource-variable/inter'
import '@fontsource-variable/fira-code';


import App from './App.vue'
import router from './router'
import { usePreferencesStore } from '@common/stores/preferences.store'
import { i18n } from '@/i18n.ts'

async function initializeApp() {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(router)
  app.use(i18n)
  app.use(VueVirtualScroller)

  app.mount('#app')

  // Load preferences after the app is mounted
  const preferencesStore = usePreferencesStore()
  await preferencesStore.loadPreferences()
}

initializeApp().catch(async (err) => await error(`Error initializing app: ${err}`))
