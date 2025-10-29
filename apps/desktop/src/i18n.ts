import { createI18n } from 'vue-i18n'
import esMessages from './locales/es.json'
import enMessages from './locales/en.json'

const messages = {
  es: esMessages,
  en: enMessages,
}

export const i18n = createI18n({
  locale: 'en',
  fallbackLocale: 'en',
  messages,
})

export type AppLocale = keyof typeof messages
