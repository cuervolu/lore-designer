import { createFileRoute } from '@tanstack/react-router';
import { useEffect } from 'react';
import { useEditorShellStore } from '@/store/editor-shell';

export const Route = createFileRoute('/workspace/$fileId')({
  component: FileRoute,
});

function FileRoute() {
  const { fileId } = Route.useParams();
  const openDocument = useEditorShellStore((s) => s.openDocument);

  useEffect(() => {
    openDocument(decodeURIComponent(fileId));
  }, [fileId, openDocument]);

  return null;
}
