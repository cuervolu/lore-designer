import { invoke } from '@tauri-apps/api/core'
import {error as logError} from '@tauri-apps/plugin-log'
import { defineStore } from 'pinia'
import { useColorMode } from '@vueuse/core'
import { i18n } from '@/i18n'
import type { AppLocale } from '@/i18n'

export interface AppPreferences {
  // Appearance
  theme: 'dark' | 'light' | 'auto'
  language: string
  // Last project
  last_project_path: string | null
  last_file_path: string | null
  // User interface
  sidebar_width: number | null
  sidebar_expanded: boolean
  font_size: number
  // Application behavior
  auto_save: boolean
  auto_save_interval_seconds: number
  // Lore Designer specific settings
  default_character_template: string | null
  default_location_template: string | null
  // Additional settings
  custom_settings: Record<string, never>
}

export const usePreferencesStore = defineStore('preferences', {
  state: (): AppPreferences => ({
    theme: 'auto',
    language: 'en',
    last_project_path: null,
    last_file_path: null,
    sidebar_width: 250,
    sidebar_expanded: true,
    font_size: 14,
    auto_save: true,
    auto_save_interval_seconds: 60,
    default_character_template: null,
    default_location_template: null,
    custom_settings: {},
  }),

  actions: {
    async loadPreferences() {
      try {
        const preferences = await invoke<AppPreferences>('get_preferences')
        this.$patch(preferences)
        this.applyTheme(preferences.theme)

        if (preferences.language) {
          i18n.global.locale = preferences.language as AppLocale
        }

        return preferences
      } catch (error) {
        await logError(`Failed to load preferences: ${error}`)
        throw error
      }
    },

    async savePreferences() {
      try {
        await invoke('save_preferences', { preferences: this.$state })
      } catch (error) {
        await logError(`Failed to save preferences: ${error}`)
        throw error
      }
    },

    async setTheme(theme: 'dark' | 'light' | 'auto') {
      try {
        await invoke('set_theme', { theme })
        this.theme = theme
        this.applyTheme(theme)
      } catch (error) {
        await logError(`Failed to set theme: ${error}`)
        throw error
      }
    },

    async setLanguage(language: string) {
      try {
        await invoke('set_language', { language })
        this.language = language
        i18n.global.locale = language as AppLocale
      } catch (error) {
        await logError(`Failed to set language: ${error}`)
        throw error
      }
    },

    async updateLastProject(path: string) {
      try {
        await invoke('update_last_project', { path })
        this.last_project_path = path
      } catch (error) {
        await logError(`Failed to update last project: ${error}`)
        throw error
      }
    },

    async updateFontSize(size: number) {
      this.font_size = size
      await this.savePreferences()
    },

    async updateSidebarWidth(width: number) {
      this.sidebar_width = width
      await this.savePreferences()
    },

    async updateSidebarExpanded(expanded: boolean) {
      this.sidebar_expanded = expanded
      await this.savePreferences()
    },

    async updateAutoSave(enabled: boolean, interval?: number) {
      this.auto_save = enabled
      if (interval !== undefined) {
        this.auto_save_interval_seconds = interval
      }
      await this.savePreferences()
    },

    applyTheme(theme: 'dark' | 'light' | 'auto') {
      const colorMode = useColorMode()
      colorMode.value = theme
    },
  },
})
