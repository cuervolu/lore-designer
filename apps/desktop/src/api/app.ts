import { getVersion } from '@tauri-apps/api/app';
import { useEffect, useState } from 'react';

export function useAppVersion() {
  const [version, setVersion] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    void getVersion()
      .then((nextVersion) => {
        if (!cancelled) {
          setVersion(nextVersion);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setVersion(null);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  return version;
}
