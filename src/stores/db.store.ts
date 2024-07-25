import Database from '@tauri-apps/plugin-sql';
import {error, info, warn} from "@tauri-apps/plugin-log";
import { ref } from 'vue';
import { defineStore } from 'pinia';

export const useDbStore = defineStore('db', () => {
  const db = ref<Database | null>(null);

  const getDb = (): Database => {
    if (!db.value) {
      db.value = Database.get('sqlite:loredesigner.db');
    }
    return db.value;
  };

  const executeQuery = async (query: string, bindValues: unknown[] = []): Promise<any> => {
    try {
      await info(`Executing query: ${query}`);
      const result = await getDb().execute(query, bindValues);
      await info('Query executed successfully');
      return result;
    } catch (e) {
      await error(`Error executing query: ${e}`);
      throw e;
    }
  };

  const select = async <T>(query: string, bindValues: unknown[] = []): Promise<T> => {
    try {
      await info(`Executing select query: ${query}`);
      const result = await getDb().select<T>(query, bindValues);
      await info('Select query executed successfully');
      return result;
    } catch (e) {
      await error(`Error executing select query: ${e}`);
      throw e;
    }
  };

  const closeDb = async (): Promise<boolean> => {
    if (db.value) {
      await info('Closing database connection');
      const result = await db.value.close();
      if (result) {
        db.value = null;
        await info('Database connection closed successfully');
      } else {
        await warn('Failed to close database connection');
      }
      return result;
    }
    return false;
  };

  return {
    executeQuery,
    select,
    closeDb
  };
});