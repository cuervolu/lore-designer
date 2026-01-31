export type OSPlatform = 'windows' | 'linux' | 'macos' | 'unknown';

export interface PlatformInfo {
  platform: OSPlatform;
  cmdKey: string;
  shiftKey: string;
  plusSeparator: string;
  symbols: {
    cmd: string;
    shift: string;
    alt: string;
    ctrl: string;
  };
}
