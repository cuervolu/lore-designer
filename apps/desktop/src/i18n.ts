import { createI18n } from "vue-i18n";

/**
 * Loads all messages from .json files within the 'locales' folder.
 *
 * This uses Vite's 'import.meta.glob' function to import
 * all matching files eagerly (synchronously).
 *
 * The 'modules' structure will look like this:
 * {
 * './locales/en/app.json': { ...content... },
 * './locales/en/common.json': { ...content... },
 * './locales/es/app.json': { ...content... }
 * }
 *
 * We need to transform this into the structure vue-i18n expects:
 * {
 * en: {
 * app: { ...content... },
 * common: { ...content... }
 * },
 * es: {
 * app: { ...content... }
 * }
 * }
 */
function loadMessages() {
  // Specify the module type. `import.meta.glob` returns a record
  // where values can be `unknown` or, in this case, `Record<string, any>`.
  const modules = import.meta.glob<Record<string, any>>("./locales/**/*.json", { eager: true });
  const messages: Record<string, Record<string, any>> = {};

  for (const path in modules) {
    // Extracts 'en' or 'es' from the path
    const langMatch = path.match(/\/locales\/(?<lang>[^/]+)\//);
    const lang = langMatch?.groups?.lang;

    // Extracts 'app', 'common', etc. from the filename
    const moduleNameMatch = path.match(/\/([^/]+)\.json$/);
    const moduleName = moduleNameMatch?.[1];

    const moduleContent = modules[path];

    if (!lang || !moduleName || !moduleContent) {
      console.warn(`Could not parse lang, module, or content from path: ${path}`);
      continue;
    }

    if (!messages[lang]) {
      messages[lang] = {};
    }

    messages[lang][moduleName] = moduleContent.default || moduleContent;
  }

  return messages;
}

const messages = loadMessages();

export const i18n = createI18n({
  legacy: false,
  locale: "en",
  fallbackLocale: "en",
  messages,
});

export type AppLocale = keyof typeof messages;
