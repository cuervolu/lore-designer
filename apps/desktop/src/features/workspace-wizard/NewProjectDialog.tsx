import { useEffect, useMemo, useState } from 'react';
import { documentDir } from '@tauri-apps/api/path';
import { open } from '@tauri-apps/plugin-dialog';
import { Button, Dialog, DialogContent, DialogTitle, Input } from '@lore/ui';
import { cn } from '@lore/ui/lib/utils';
import { createWorkspace, listWorkspaceTemplates } from '@api/workspace';
import { useAppVersion } from '@/api/app';
import type { WorkspaceTemplateSummary } from './types';

interface NewProjectDialogProps {
  onClose: () => void;
  onCreated: (name: string) => void;
  open: boolean;
}

export function NewProjectDialog({ onClose, onCreated, open: isOpen }: NewProjectDialogProps) {
  const [name, setName] = useState('');
  const [parentPath, setParentPath] = useState('');
  const [templates, setTemplates] = useState<WorkspaceTemplateSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const appVersion = useAppVersion();

  useEffect(() => {
    if (!isOpen) return;
    let cancelled = false;
    setName('');
    setError(null);
    void Promise.all([listWorkspaceTemplates(), documentDir()])
      .then(([items, directory]) => {
        if (cancelled) return;
        setTemplates(items);
        setParentPath(`${directory.replace(/[\\/]$/, '')}/Lore`);
      })
      .catch((nextError) => {
        if (cancelled) return;
        setTemplates([]);
        setParentPath('Lore');
        setError(nextError instanceof Error ? nextError.message : 'Failed to load templates.');
      });
    return () => {
      cancelled = true;
    };
  }, [isOpen]);

  const blankTemplate = useMemo(
    () => templates.find((template) => template.id === 'blank' && template.supportsCreation),
    [templates],
  );

  const chooseFolder = async () => {
    const selected = await open({
      defaultPath: parentPath,
      directory: true,
      multiple: false,
      title: 'Choose project folder',
    });
    if (typeof selected === 'string') {
      setParentPath(selected);
      setError(null);
    }
  };

  const createProject = async () => {
    const projectName = name.trim();
    if (!projectName) {
      setError('Give your project a name.');
      return;
    }
    if (!parentPath.trim()) {
      setError('Choose the parent folder for the project.');
      return;
    }
    if (!blankTemplate) {
      setError('No template is available yet.');
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const workspace = await createWorkspace({
        appVersion: appVersion ?? '0.1.0',
        name: projectName,
        parentPath: parentPath.trim(),
        templateId: blankTemplate.id,
      });
      onCreated(workspace.name);
    } catch (nextError) {
      setError(nextError instanceof Error ? nextError.message : 'Failed to create project.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog onOpenChange={(nextOpen) => !nextOpen && onClose()} open={isOpen}>
      <DialogContent className="max-w-[420px] p-8 pb-[26px]" showCloseButton={false}>
        <DialogTitle className="mb-[22px] text-[10px] font-semibold tracking-[0.14em] text-faint uppercase">
          New Project
        </DialogTitle>
        <label
          className="mb-2 block text-[11px] font-semibold tracking-[0.08em] text-faint uppercase"
          htmlFor="project-name"
        >
          Project name
        </label>
        <Input
          autoFocus
          className="h-auto rounded-none border-0 border-b border-border px-0 pt-1 pb-2.5 text-[19px] focus-visible:border-primary focus-visible:ring-0"
          id="project-name"
          onChange={(event) => setName(event.target.value)}
          placeholder="Name your world…"
          value={name}
        />
        <div className="mt-[26px] mb-2 block text-[11px] font-semibold tracking-[0.08em] text-faint uppercase">
          Workspace location
        </div>
        <div className="flex items-center justify-between gap-2.5 border-b border-border pt-1 pb-2.5">
          <span
            className={cn(
              'overflow-hidden text-[13.5px] text-ellipsis whitespace-nowrap',
              parentPath ? 'text-body' : 'text-faint italic',
            )}
          >
            {parentPath || 'No folder chosen'}
          </span>
          <button
            className="shrink-0 cursor-pointer text-[12.5px] font-semibold text-primary hover:underline"
            onClick={() => void chooseFolder()}
            type="button"
          >
            Choose folder…
          </button>
        </div>
        {error ? (
          <p className="mt-3.5 text-[12.5px] text-destructive" role="alert">
            {error}
          </p>
        ) : null}
        <div className="mt-[30px] flex justify-end gap-2.5">
          <Button onClick={onClose} type="button" variant="ghost">
            Cancel
          </Button>
          <Button disabled={submitting} onClick={() => void createProject()} type="button">
            {submitting ? 'Creating…' : 'Create Project'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
