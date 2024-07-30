import {emit} from '@tauri-apps/api/event';
import {defineStore} from 'pinia';
import {useDbStore} from './db.store';
import type {Character, CharacterForNode, CharacterRequest, ImageInfo} from '~/interfaces';
import {useErrorHandler} from '~/composables/useErrorHandler';
import {DatabaseError} from '~/exceptions/db.error';
import {error} from "@tauri-apps/plugin-log";

export const useCharacterStore = defineStore('character', () => {
  const dbStore = useDbStore();
  const {handleError} = useErrorHandler();

  const characters = ref<Character[]>([]);
  const totalCharacters = ref(0);
  const lastFetchTimestamp = ref(0);

  const CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

  const isCacheValid = computed(() => {
    return Date.now() - lastFetchTimestamp.value < CACHE_DURATION;
  });

  const saveImage = async (imageInfo: ImageInfo): Promise<string> => {
    try {
      const updateResult = await dbStore.executeQuery(
          'UPDATE Images SET Path = $1 WHERE UUID = $2',
          [imageInfo.path, imageInfo.id]
      );

      if (updateResult.rowsAffected === 0) {
        await dbStore.executeQuery(
            'INSERT INTO Images (UUID, Path) VALUES ($1, $2)',
            [imageInfo.id, imageInfo.path]
        );
      }

      return imageInfo.id;
    } catch (e) {
      await error(`Error saving image: ${e}`);
      throw new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to save image',
        cause: e
      });
    }
  };

  const updateCharacterImage = async (characterId: number, imageInfo: ImageInfo): Promise<void> => {
    try {
      await dbStore.executeQuery(
          'INSERT INTO Images (UUID, Path, CharacterID) VALUES ($1, $2, $3)',
          [imageInfo.id, imageInfo.path, characterId]
      );

      await dbStore.executeQuery(
          'UPDATE Characters SET ImageID = $1 WHERE ID = $2',
          [imageInfo.id, characterId]
      );

      await emit('character-image-updated', {
        characterId,
        imageId: imageInfo.id,
        path: imageInfo.path
      });
    } catch (e) {
      await error(`Error updating character image: ${e}`);
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update character image',
        cause: e
      }));
      throw e;
    }
  };

  const createCharacter = async (character: CharacterRequest): Promise<number | null> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT INTO Characters (Name, Description, Role, ImageID, AdditionalNotes) VALUES ($1, $2, $3, $4, $5)',
          [character.name, character.description, character.role, character.imageID, character.additionalNotes]
      );

      const id = result.lastInsertId as number;

      if (character.imageID) {
        await dbStore.executeQuery(
            'UPDATE Images SET CharacterID = $1 WHERE UUID = $2',
            [id, character.imageID]
        );
      }

      // Invalidate cache
      lastFetchTimestamp.value = 0;

      return id;
    } catch (e) {
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create character',
        cause: e
      }));
      return null;
    }
  };

  const fetchCharacters = async (page: number = 1, pageSize: number = 20, forceRefresh: boolean = false) => {
    if (!forceRefresh && isCacheValid.value && characters.value.length > 0) {
      return {characters: characters.value, total: totalCharacters.value};
    }

    try {
      const result = await dbStore.select<any[]>(
          `SELECT c.*, i.Path as ImagePath
           FROM Characters c
                    LEFT JOIN Images i ON c.ImageID = i.UUID
           ORDER BY c.CreatedAt DESC
           LIMIT $1 OFFSET $2`,
          [pageSize, (page - 1) * pageSize]
      );

      const fetchedCharacters = result.map(row => ({
        id: row.ID,
        name: row.Name,
        description: row.Description,
        role: row.Role,
        imageID: row.ImageID,
        additionalNotes: row.AdditionalNotes,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt,
        imagePath: row.ImagePath
      }));

      const countResult = await dbStore.select<[{
        count: number
      }]>('SELECT COUNT(*) as count FROM Characters');
      const total = countResult[0].count;

      characters.value = fetchedCharacters;
      totalCharacters.value = total;
      lastFetchTimestamp.value = Date.now();

      return {characters: fetchedCharacters, total};
    } catch (e) {
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch characters',
        cause: e
      }));
      return {characters: [], total: 0};
    }
  };

  const getCharacterById = async (id: number): Promise<Character | null> => {
    try {
      const result = await dbStore.select<any[]>(
          `SELECT c.*,
                  i.Path as ImagePath
           FROM Characters c
                    LEFT JOIN Images i ON c.ImageID = i.UUID
           WHERE c.ID = $1`,
          [id]
      );

      if (result.length === 0) {
        return null;
      }

      const row = result[0];
      return {
        id: row.ID,
        name: row.Name,
        description: row.Description,
        role: row.Role,
        imageID: row.ImageID,
        additionalNotes: row.AdditionalNotes,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt,
        imagePath: row.ImagePath
      };
    } catch (e) {
      await error(`Error fetching character by ID: ${e}`);
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch character by ID',
        cause: e
      }));
      return null;
    }
  };

  const updateCharacter = async (id: number, character: Partial<CharacterRequest>): Promise<void> => {
    try {
      const setClause = Object.keys(character).map((key, index) => `${key} = $${index + 2}`).join(', ');
      const values = Object.values(character);
      await dbStore.executeQuery(
          `UPDATE Characters
           SET ${setClause}
           WHERE ID = $1`,
          [id, ...values]
      );

      // Invalidate cache
      lastFetchTimestamp.value = 0;
    } catch (e) {
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update character',
        cause: e
      }));
      throw e;
    }
  };

  const deleteCharacter = async (id: number): Promise<void> => {
    try {
      await dbStore.executeQuery('DELETE FROM characters WHERE id = $1', [id]);
      await emit('character-deleted', {id});
    } catch (e) {
      await error(`Error deleting character: ${e}`);
      throw e;
    }
  };

  const getCharactersName = async (): Promise<CharacterForNode[]> => {
    try {
      const results = await dbStore.select<any[]>(
          `SELECT c.ID, c.Name, i.Path as ImagePath
           FROM Characters c
                    LEFT JOIN Images i ON c.ImageID = i.UUID
           ORDER BY c.CreatedAt DESC`
      );

      return results.map(row => ({
        id: row.ID,
        name: row.Name,
        imagePath: row.ImagePath
      }));
    } catch (e) {
      await error(`Error fetching characters: ${e}`);
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch characters',
        cause: e
      }));
      return [];
    }
  }


  return {
    characters,
    totalCharacters,
    createCharacter,
    fetchCharacters,
    getCharacterById,
    getCharactersName,
    saveImage,
    updateCharacterImage,
    updateCharacter,
    deleteCharacter
  };
});