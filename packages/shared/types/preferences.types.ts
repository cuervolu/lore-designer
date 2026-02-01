

/**
 * Types related to application preferences and settings.
 * For the moment, language is not used, but it is planned for future internationalization support.
 */
export interface AppPreferences {
  theme: 'dark' | 'light' | 'auto';
  language: string;
  last_project_path: string | null;
  last_file_path: string | null;
  sidebar_width: number | null;
  sidebar_expanded: boolean;
  font_size: number;
  auto_save: boolean;
  auto_save_interval_seconds: number;
  default_character_template: string | null;
  default_location_template: string | null;
  custom_settings: Record<string, never>;
}
