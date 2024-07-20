/**
 * Represents a character within the application.
 * 
 * This interface defines the structure of a character object, including both mandatory and optional fields.
 * Characters are central entities in the application, used across various features and functionalities.
 * 
 * @interface
 * @property {number} id - The unique identifier for the character.
 * @property {string} name - The name of the character.
 * @property {string} [description] - An optional description of the character.
 * @property {'Primary' | 'Secondary' | 'Tertiary' | 'Undefined'} [role] - The role of the character within the story or application context. Optional.
 * @property {string} [imageUrl] - An optional URL to an image representing the character.
 * @property {string} [additionalNotes] - Any additional notes or comments about the character. Optional.
 * @property {Date} createdAt - The date and time when the character was created.
 * @property {Date} updatedAt - The date and time when the character information was last updated.
 */
export interface Character {
  id: number;
  name: string;
  description?: string;
  role?: 'Primary' | 'Secondary' | 'Tertiary' | 'Undefined';
  imageUrl?: string;
  additionalNotes?: string;
  createdAt: Date;
  updatedAt: Date;
}