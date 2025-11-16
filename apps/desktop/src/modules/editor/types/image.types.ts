export interface ManagedImage {
  displaySrc: string;
  relativeStoragePath: string;
  absoluteDiskPath: string;
  altText: string;
  originalFileName: string;
}

export interface ImageManagerError {
  type: "NoWorkspace" | "DialogCancelled" | "CopyFailed" | "PathError" | "Unknown";
  message: string;
  details?: unknown;
}

export interface ValidationReport {
  total_images: number;
  missing_images: MissingImageInfo[];
  orphaned_images: string[];
}

export interface MissingImageInfo {
  path: string;
  original_name: string;
  used_by: string[];
}