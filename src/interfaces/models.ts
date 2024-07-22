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
 * @property {string} [imageID] - An optional URL to an image representing the character.
 * @property {string} [additionalNotes] - Any additional notes or comments about the character. Optional.
 * @property {string} createdAt - The date and time when the character was created.
 * @property {string} updatedAt - The date and time when the character information was last updated.
 * @property {string} [imagePath] - The path to the image file associated with the character. Optional.
 */
export interface Character {
  id: number;
  name: string;
  description: string;
  role: 'Primary' | 'Secondary' | 'Tertiary' | 'Undefined';
  imageID: string | null;
  additionalNotes: string;
  createdAt: string;
  updatedAt: string;
  imagePath?: string;
}

export interface ImageInfo {
  id: string
  path: string
}