import Database from '@tauri-apps/plugin-sql';
import { ref } from 'vue';
import { defineStore } from 'pinia';
import { log } from '~/config/log.config';

export const useDbStore = defineStore('db', () => {
  const db = ref<Database | null>(null);
  let initializationPromise: Promise<Database> | null = null;

  const initDb = async (): Promise<Database> => {
    if (db.value) return db.value;
    await log.info('Initializing database');
    if (!initializationPromise) {
      initializationPromise = Database.load('sqlite:loredesigner.db')
      .then((database) => {
        log.info('Database initialized');
        db.value = database;
        return database;
      })
      .catch(async (e) => {
        await log.error(`Failed to initialize database: ${e}`);
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
    await log.info('Closing database');
    if (db.value) {
      await db.value.close();
      db.value = null;
      await log.info('Database closed');
    }
  };

  return {
    db,
    initDb,
    closeDb
  };
});