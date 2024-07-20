import Database from '@tauri-apps/plugin-sql';
import {defineStore} from 'pinia';


interface DbState {
  db: Database | null;
}

export const useDbStore = defineStore('db', () => {
  const state = reactive<DbState>({
    db: null,
  });

  const initDb = async () => {
    if (!state.db) {
      state.db = await Database.load('sqlite:loredesigner.db');
    }
  };

  const closeDb = async () => {
    if (state.db) {
      await state.db.close();
      state.db = null;
    }
  };

  return {
    get db() { return state.db; },
    initDb,
    closeDb
  };
});