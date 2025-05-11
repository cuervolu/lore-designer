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
