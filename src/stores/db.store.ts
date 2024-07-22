import Database from '@tauri-apps/plugin-sql';
import { ref } from 'vue';
import { defineStore } from 'pinia';

export const useDbStore = defineStore('db', () => {
  const db = ref<Database | null>(null);
  let initializationPromise: Promise<Database> | null = null;

  const initDb = async (): Promise<Database> => {
    if (db.value) return db.value;

    if (!initializationPromise) {
      initializationPromise = Database.load('sqlite:loredesigner.db')
      .then((database) => {
        db.value = database;
        return database;
      })
      .catch((error) => {
        console.error('Failed to initialize database:', error);
        throw error;
      })
      .finally(() => {
        initializationPromise = null;
      });
    }

    return initializationPromise;
  };

  const closeDb = async () => {
    if (db.value) {
      await db.value.close();
      db.value = null;
    }
  };

  return {
    db,
    initDb,
    closeDb
  };
});