import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { useTheme } from '@lore/ui/components/theme-provider';
import { SegmentedControl, SegmentedControlItem, Switch } from '@lore/ui';
import { useEditorShellStore } from '@/store/editor-shell';
import type { EditorFontSize } from '@/types/editor';
import { ChevronLeft } from 'lucide-react';

export const Route = createFileRoute('/settings')({ component: SettingsRoute });

function SettingsRoute() {
  const navigate = useNavigate();
  const { resolvedTheme, setTheme } = useTheme();
  const { focusModeEnabled, fontSize, setFocusModeEnabled, setFontSize, settingsReturnPath } =
    useEditorShellStore();

  const fontSizes: EditorFontSize[] = ['small', 'medium', 'large'];

  return (
    <main className="h-full w-full overflow-y-auto">
      <section
        className="mx-auto w-full max-w-[560px] px-6 pt-[14vh] pb-[100px]"
        aria-labelledby="settings-title"
      >
        <button
          className="mb-10 flex cursor-pointer items-center text-[12px] font-semibold tracking-[0.04em] text-faint hover:text-primary"
          onClick={() => void navigate({ to: settingsReturnPath })}
          type="button"
        >
          <ChevronLeft size={14} strokeWidth={2} /> Back
        </button>
        <h1 className="mb-12 text-[34px] font-semibold" id="settings-title">
          Settings
        </h1>
        <div className="flex items-center justify-between border-b border-border py-[22px] text-[16px] font-medium">
          <span>Theme</span>
          <SegmentedControl
            aria-label="Theme"
            onValueChange={(values) => {
              const next = values[0];
              if (next) setTheme(next as 'light' | 'dark');
            }}
            value={[resolvedTheme]}
          >
            <SegmentedControlItem value="light">Parchment</SegmentedControlItem>
            <SegmentedControlItem value="dark">Ink</SegmentedControlItem>
          </SegmentedControl>
        </div>
        <div className="flex items-center justify-between border-b border-border py-[22px] text-[16px] font-medium">
          <span>Default text size</span>
          <SegmentedControl
            aria-label="Default text size"
            onValueChange={(values) => {
              const next = values[0] as EditorFontSize | undefined;
              if (next) setFontSize(next);
            }}
            value={[fontSize]}
          >
            {fontSizes.map((size) => (
              <SegmentedControlItem key={size} value={size}>
                {size.charAt(0).toUpperCase()}
              </SegmentedControlItem>
            ))}
          </SegmentedControl>
        </div>
        <div className="flex items-center justify-between border-b border-border py-[22px] text-[16px] font-medium">
          <span>Focus mode</span>
          <Switch
            aria-label="Focus mode"
            checked={focusModeEnabled}
            onCheckedChange={setFocusModeEnabled}
          />
        </div>
        <p className="mt-6 text-[13px] text-faint italic">More settings coming soon.</p>
      </section>
    </main>
  );
}
