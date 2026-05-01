import { setTheme as setAppTheme } from '@tauri-apps/api/app';
import { useEffect } from 'react';
import { useTheme } from '@lore/ui/components/theme-provider';

export function useSyncNativeTheme() {
  const { resolvedTheme, theme } = useTheme();

  useEffect(() => {
    document.documentElement.style.colorScheme = resolvedTheme;

    void setAppTheme(theme === 'system' ? undefined : resolvedTheme).catch(() => {
      // Ignore failures when the API is unavailable during tests or non-Tauri renders.
    });
  }, [resolvedTheme, theme]);
}
