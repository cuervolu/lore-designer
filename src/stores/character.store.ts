import { emit } from '@tauri-apps/api/event';
import { defineStore } from 'pinia';
import { useDbStore } from './db.store';
import type { Character, CharacterRequest } from '~/interfaces';
import { useErrorHandler } from '~/composables/useErrorHandler';
import { DatabaseError } from '~/exceptions/db.error';

export const useCharacterStore = defineStore('character', () => {
  const dbStore = useDbStore();
  const { handleError } = useErrorHandler();

  const createCharacter = async (character: CharacterRequest): Promise<number | null> => {
    try {
      await dbStore.initDb();
      const result = await dbStore.db!.execute(
          'INSERT INTO characters (name, description, role, ImageURL, AdditionalNotes) VALUES ($1, $2, $3, $4, $5)',
          [character.name, character.description, character.role, character.imageUrl, character.additionalNotes]
      );
      const id = result.lastInsertId as number;
      await emit('character-created', { id, ...character });
      return id;
    } catch (error) {
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create character',
        cause: error
      }));
      return null;
    }
  };

  const getCharacters = async (page: number = 1, pageSize: number = 20): Promise<Character[]> => {
    try {
      await dbStore.initDb();
      const offset = (page - 1) * pageSize;
      return await dbStore.db!.select<Character[]>(
          'SELECT * FROM characters ORDER BY CreatedAt DESC LIMIT $1 OFFSET $2',
          [pageSize, offset]
      );
    } catch (error) {
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch characters',
        cause: error
      }));
      return [];
    }
  };


  const updateCharacter = async (id: number, character: Partial<CharacterRequest>): Promise<void> => {
    try {
      await dbStore.initDb();
      const setClause = Object.keys(character).map((key, index) => `${key} = $${index + 2}`).join(', ');
      const values = Object.values(character);
      await dbStore.db!.execute(
          `UPDATE characters
         SET ${setClause},
             UpdatedAt = CURRENT_TIMESTAMP
         WHERE id = $1`,
          [id, ...values]
      );
      await emit('character-updated', { id, ...character });
    } catch (error) {
      console.error('Error updating character:', error);
      throw error;
    }
  };

  const deleteCharacter = async (id: number): Promise<void> => {
    try {
      await dbStore.initDb();
      await dbStore.db!.execute('DELETE FROM characters WHERE id = $1', [id]);
      await emit('character-deleted', { id });
    } catch (error) {
      console.error('Error deleting character:', error);
      throw error;
    }
  };

  return {
    createCharacter,
    getCharacters,
    updateCharacter,
    deleteCharacter
  };
});