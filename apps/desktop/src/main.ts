import { createApp } from 'vue';
import { createPinia } from 'pinia';
import VueVirtualScroller from 'vue-virtual-scroller';
import App from './App.vue';
import router from './router';
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css';
import './assets/global.css';
import '@fontsource-variable/inter';
import '@fontsource-variable/fira-code';

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(VueVirtualScroller);

app.mount('#app');
