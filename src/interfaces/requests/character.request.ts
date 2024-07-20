import  type { Character } from '../models';

/**
 * Defines the structure for a `CharacterRequest` object.
 * 
 * This type is derived from the `Character` model by omitting certain properties
 * that are not required or should not be manually set for a character creation or update request.
 * Specifically, `id`, `createdAt`, and `updatedAt` fields are omitted to ensure they are
 * handled automatically by the system and not included in the request.
 * 
 * @type {CharacterRequest} - Represents a request to create or update a character without including system-managed fields.
 */
export type CharacterRequest = Omit<Character, 'id' | 'createdAt' | 'updatedAt'>;
