// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-04-03',
  devtools: {enabled: true},
  ssr: false,
  alias: {
    '@vue/devtools-api': '@vue/devtools-api',
  },
  vite: {
    // Better support for Tauri CLI output
    clearScreen: false,
    // https://github.com/tauri-apps/tauri/security/advisories/GHSA-2rcp-jvr4-r259
    define: {
      TAURI_PLATFORM: JSON.stringify(process.env.TAURI_PLATFORM),
      TAURI_ARCH: JSON.stringify(process.env.TAURI_ARCH),
      TAURI_FAMILY: JSON.stringify(process.env.TAURI_FAMILY),
      TAURI_PLATFORM_VERSION: JSON.stringify(process.env.TAURI_PLATFORM_VERSION),
      TAURI_PLATFORM_TYPE: JSON.stringify(process.env.TAURI_PLATFORM_TYPE),
      TAURI_DEBUG: JSON.stringify(process.env.TAURI_DEBUG),
    },
    // Dev server configurations
    server: {
      // Tauri requires a consistent port
      strictPort: true,
      // Enables the development server to be discoverable by other devices for mobile development
      host: '0.0.0.0',
      hmr: {
        // Use websocket for mobile hot reloading
        protocol: 'ws',
        // Make sure it's available on the network
        host: '0.0.0.0',
        // Use a specific port for hmr
        port: 5183,
      },
    },
    optimizeDeps: {
      exclude: ['vee-validate']
    }
  },
  srcDir: 'src',
  modules: [
    "@pinia/nuxt",
    "@nuxt/eslint",
    "@nuxt/fonts",
    "@vee-validate/nuxt",
    "@vueuse/nuxt",
    "@nuxtjs/color-mode",
    "@nuxtjs/partytown",
    "@nuxtjs/tailwindcss",
    "shadcn-nuxt",
    "@nuxtjs/i18n",
    "@nuxtjs/mdc"
  ],
  plugins: [
    '~/plugins/i18n.ts',
    '~/plugins/fonts.ts'
  ],
  i18n: {
    vueI18n: 'src/config/i18n.config.ts',
    defaultLocale: 'en',
    locales: [
      {
        code: 'en',
        file: 'en.ts',
        name: 'English'
      },
      {
        code: 'es',
        file: 'es.ts',
        name: 'Español'
      }
    ],
    langDir: 'locales/',
  },
  shadcn: {
    /**
     * Prefix for all the imported component
     */
    prefix: '',
    /**
     * Directory that the component lives in.
     * @default "./components/ui"
     */
    componentDir: 'src/components/ui',
  },
  pinia: {
    storesDirs: ['src/stores/**'],
  },
  tailwindcss: {
    cssPath: ['~/assets/css/tailwind.css', {injectPosition: 'first'}],
    configPath: 'tailwind.config.js',
  },
  colorMode: {
    classSuffix: '',
  },
  fonts: {
    families: [{
      name: 'Geist Mono',
      provider: 'fontsource',
      weights: ['100', '200', '300', '400', '500', '600', '700', '800', '900'],
    }, {
      name: 'Inter',
      provider: 'fontsource',
      weights: ['400', '500', '600', '700'],
    }],
  },
})