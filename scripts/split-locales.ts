import fs from 'fs/promises';
import path from 'path';

const basePath = path.join('apps', 'desktop', 'src');
const localesDir = path.join(basePath, 'locales');
const outputDir = localesDir; 

const sourceFiles = [
  path.join(basePath, 'locales/en.json'),
  path.join(basePath, 'locales/es.json'),
];

async function splitLocales() {
  console.log('Starting locale splitting process...');

  for (const filePath of sourceFiles) {
    const lang = path.basename(filePath, '.json'); 
    const langDir = path.join(outputDir, lang);

    try {
      await fs.mkdir(langDir, { recursive: true });

      const content = await fs.readFile(filePath, 'utf-8');
      const data = JSON.parse(content);

      for (const key in data) {
        if (Object.prototype.hasOwnProperty.call(data, key)) {
          const moduleName = key;
          const moduleData = data[key];
          const outputFilePath = path.join(langDir, `${moduleName}.json`);

        
          await fs.writeFile(
            outputFilePath,
            JSON.stringify(moduleData, null, 2) + '\n' 
          );
          console.log(`[${lang}] Creado -> ${outputFilePath}`);
        }
      }

      console.log(`Completed division for ${lang}.`);
    } catch (err) {
      console.error(`There was an error for ${filePath}:`, err);
    }
  }

  console.log('Locale splitting process completed.');
}

splitLocales();
