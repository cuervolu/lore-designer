import { error } from '@fltsci/tauri-plugin-tracing';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import VueVirtualScroller from 'vue-virtual-scroller';

import App from '@/App.vue';
import router from '@/router';

import 'vue-virtual-scroller/dist/vue-virtual-scroller.css';
import './assets/global.css';
import '@fontsource-variable/inter';
import '@fontsource-variable/fira-code';
import { usePreferencesStore } from '@/core/preferences/preferences.store';

async function initializeApp() {
  const app = createApp(App);

  app.use(createPinia());
  app.use(router);
  app.use(VueVirtualScroller);

  app.mount('#app');

  // Load preferences after the app is mounted
  const preferencesStore = usePreferencesStore();
  await preferencesStore.loadPreferences();
}

initializeApp().catch((err) => {
  error('Failed to initialize the app:', err);
});
