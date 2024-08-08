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

// Only used for the nodes in the dialogue page
export interface CharacterForNode {
  id: number;
  name: string;
  imagePath: string;
}

export interface Project {
  id: number;
  name: string;
  description?: string;
  imageID?: string;
  budget?: number;
  createdAt: string;
  updatedAt: string;
  imagePath?: string;
}

export interface ProjectRequest {
  name: string;
  description?: string;
  imageID?: string;
  budget?: number;
}

export interface ProjectGoal {
  id: number;
  projectID: number;
  description: string;
  isCompleted: boolean;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectResource {
  id: number;
  projectID: number;
  name: string;
  type: string;
  description?: string;
  quantity?: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectAsset {
  id: number;
  projectID: number;
  name: string;
  type: string;
  status: 'Pending' | 'InProgress' | 'Completed';
  filePath?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectNote {
  id: number;
  projectID: number;
  title: string;
  content?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectMetric {
  id: number;
  projectID: number;
  name: string;
  description?: string;
  target?: number;
  currentValue?: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectGoalRequest {
  projectID: number;
  description: string;
  isCompleted?: boolean;
  dueDate?: string;
}

export interface ProjectResourceRequest {
  projectID: number;
  name: string;
  type: string;
  description?: string;
  quantity?: number;
}

export interface ProjectAssetRequest {
  projectID: number;
  name: string;
  type: string;
  status?: 'Pending' | 'InProgress' | 'Completed';
  filePath?: string;
}

export interface ProjectNoteRequest {
  projectID: number;
  title: string;
  content?: string;
}

export interface ProjectMetricRequest {
  projectID: number;
  name: string;
  description?: string;
  target?: number;
  currentValue?: number;
}