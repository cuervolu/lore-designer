import Database from '@tauri-apps/plugin-sql';
import {info, error} from '@tauri-apps/plugin-log';
import {ref} from 'vue';
import {defineStore} from 'pinia';

export const useDbStore = defineStore('db', () => {
  const db = ref<Database | null>(null);
  let initializationPromise: Promise<Database> | null = null;

  const initDb = async (): Promise<Database> => {
    await info('Initializing database');
    if (db.value) return db.value;

    if (!initializationPromise) {
      initializationPromise = Database.load('sqlite:loredesigner.db')
      .then((database) => {
        info('Database initialized');
        db.value = database;
        return database;
      })
      .catch(async (e) => {
        await error(`Failed to initialize database: ${e}`);
        console.error('Failed to initialize database:', e);
        throw e;
      })
      .finally(() => {
        initializationPromise = null;
      });
    }

    return initializationPromise;
  };

  const closeDb = async () => {
    await info('Closing database');
    if (db.value) {
      await db.value.close();
      db.value = null;
      await info('Database closed');
    }
  };

  return {
    db,
    initDb,
    closeDb
  };
});