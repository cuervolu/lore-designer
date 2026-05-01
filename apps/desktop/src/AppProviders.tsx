import type { PropsWithChildren } from 'react';
import { ThemeProvider } from '@lore/ui/components/theme-provider';
import { TooltipProvider } from '@lore/ui/components/tooltip';
import { useSyncNativeTheme } from './hooks/use-sync-native-theme';

function ThemeSyncBridge() {
  useSyncNativeTheme();
  return null;
}

export function AppProviders({ children }: PropsWithChildren) {
  return (
    <ThemeProvider defaultTheme="system" storageKey="lore-designer-theme">
      <ThemeSyncBridge />
      <TooltipProvider>{children}</TooltipProvider>
    </ThemeProvider>
  );
}
