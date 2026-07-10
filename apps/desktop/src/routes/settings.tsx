import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { useTheme } from '@lore/ui/components/theme-provider';
import { useEditorShellStore } from '@/store/editor-shell';
import type { EditorFontSize } from '@/types/editor';

export const Route = createFileRoute('/settings')({ component: SettingsRoute });

function SettingsRoute() {
  const navigate = useNavigate();
  const { resolvedTheme, setTheme } = useTheme();
  const { focusModeEnabled, fontSize, setFocusModeEnabled, setFontSize, settingsReturnPath } =
    useEditorShellStore();

  const fontSizes: EditorFontSize[] = ['small', 'medium', 'large'];

  return (
    <main className="settings-screen">
      <section className="settings-panel" aria-labelledby="settings-title">
        <button
          className="settings-back"
          onClick={() => void navigate({ to: settingsReturnPath })}
          type="button"
        >
          ‹ Back
        </button>
        <h1 id="settings-title">Settings</h1>
        <div className="settings-row">
          <span>Theme</span>
          <div className="segmented-control" aria-label="Theme">
            <button
              className={resolvedTheme === 'light' ? 'is-active' : ''}
              onClick={() => setTheme('light')}
              type="button"
            >
              Parchment
            </button>
            <button
              className={resolvedTheme === 'dark' ? 'is-active' : ''}
              onClick={() => setTheme('dark')}
              type="button"
            >
              Ink
            </button>
          </div>
        </div>
        <div className="settings-row">
          <span>Default text size</span>
          <div className="segmented-control" aria-label="Default text size">
            {fontSizes.map((size) => (
              <button
                className={fontSize === size ? 'is-active' : ''}
                key={size}
                onClick={() => setFontSize(size)}
                type="button"
              >
                {size.charAt(0).toUpperCase()}
              </button>
            ))}
          </div>
        </div>
        <div className="settings-row">
          <span>Focus mode</span>
          <button
            aria-checked={focusModeEnabled}
            aria-label="Focus mode"
            className={`switch${focusModeEnabled ? ' is-on' : ''}`}
            onClick={() => setFocusModeEnabled(!focusModeEnabled)}
            role="switch"
            type="button"
          >
            <span />
          </button>
        </div>
        <p className="settings-footnote">More settings coming soon.</p>
      </section>
    </main>
  );
}
