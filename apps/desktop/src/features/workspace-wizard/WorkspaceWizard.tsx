import { useEffect, useMemo, useState } from 'react';
import { documentDir } from '@tauri-apps/api/path';
import { open } from '@tauri-apps/plugin-dialog';
import { Settings } from 'lucide-react';
import { createWorkspace, listWorkspaceTemplates } from '@api/workspace';
import { useAppVersion } from '@/api/app';
import { RECENT_PROJECTS } from '@/store/mock-data';
import type { ProjectSummary } from '@/types/editor';
import type { WorkspaceTemplateSummary } from './types';

interface WorkspaceWizardProps {
  onOpenExample: () => void;
  onOpenProject: (project: ProjectSummary) => void;
  onOpenSettings: () => void;
  onWorkspaceCreated: (name: string) => void;
  projects?: ProjectSummary[];
}

export function WorkspaceWizard({
  onOpenExample,
  onOpenProject,
  onOpenSettings,
  onWorkspaceCreated,
  projects = RECENT_PROJECTS,
}: WorkspaceWizardProps) {
  const [dialogOpen, setDialogOpen] = useState(false);

  return (
    <main className="home-screen">
      <button
        aria-label="Open settings"
        className="icon-button home-screen__settings"
        onClick={onOpenSettings}
        type="button"
      >
        <Settings aria-hidden="true" size={17} strokeWidth={1.6} />
      </button>

      {projects.length > 0 ? (
        <section className="home-list" aria-labelledby="home-wordmark">
          <Wordmark id="home-wordmark" />
          {projects.map((project) => (
            <button
              className="project-row"
              key={project.id}
              onClick={() => onOpenProject(project)}
              type="button"
            >
              <span className="project-row__name">{project.name}</span>
              <span className="project-row__meta">
                {project.lastEdited} · {project.entryCount} entries
                {project.status ? ` · ${project.status}` : ''}
              </span>
            </button>
          ))}
          <button
            className="text-action home-list__new"
            onClick={() => setDialogOpen(true)}
            type="button"
          >
            + New project…
          </button>
        </section>
      ) : (
        <EmptyProjects onCreate={() => setDialogOpen(true)} onOpenExample={onOpenExample} />
      )}

      <NewProjectDialog
        onClose={() => setDialogOpen(false)}
        onCreated={(name) => {
          setDialogOpen(false);
          onWorkspaceCreated(name);
        }}
        open={dialogOpen}
      />
    </main>
  );
}

function Wordmark({ id }: { id?: string }) {
  return (
    <div className="wordmark" id={id}>
      Lore Designer
    </div>
  );
}

function EmptyProjects({
  onCreate,
  onOpenExample,
}: {
  onCreate: () => void;
  onOpenExample: () => void;
}) {
  return (
    <section className="empty-projects" aria-labelledby="empty-projects-title">
      <Wordmark />
      <h1 id="empty-projects-title">Nothing here yet.</h1>
      <p>
        A project is a world of its own — a folder of entries that remember each other. Start one
        from scratch, or step into a small finished example first.
      </p>
      <button className="onboarding-option" onClick={onCreate} type="button">
        <span className="onboarding-option__title">Create a new project</span>
        <span className="onboarding-option__description">
          Start with an empty world and shape it your way.
        </span>
      </button>
      <button className="onboarding-option" onClick={onOpenExample} type="button">
        <span className="onboarding-option__title">Open an example project</span>
        <span className="onboarding-option__description">
          Explore “The Hollow,” a small pre-built world, to see how entries stay connected.
        </span>
      </button>
    </section>
  );
}

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

  if (!isOpen) return null;

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
    <div
      className="dialog-scrim"
      onMouseDown={(event) => event.target === event.currentTarget && onClose()}
    >
      <section
        aria-labelledby="new-project-title"
        aria-modal="true"
        className="new-project-dialog"
        role="dialog"
      >
        <div className="dialog-kicker" id="new-project-title">
          New Project
        </div>
        <label className="dialog-label" htmlFor="project-name">
          Project name
        </label>
        <input
          autoFocus
          className="dialog-input"
          id="project-name"
          onChange={(event) => setName(event.target.value)}
          placeholder="Name your world…"
          value={name}
        />
        <div className="dialog-label dialog-label--spaced">Workspace location</div>
        <div className="folder-row">
          <span className={parentPath ? 'folder-row__path' : 'folder-row__path is-empty'}>
            {parentPath || 'No folder chosen'}
          </span>
          <button className="folder-row__choose" onClick={() => void chooseFolder()} type="button">
            Choose folder…
          </button>
        </div>
        {error ? (
          <p className="dialog-error" role="alert">
            {error}
          </p>
        ) : null}
        <div className="dialog-actions">
          <button className="dialog-button dialog-button--cancel" onClick={onClose} type="button">
            Cancel
          </button>
          <button
            className="dialog-button dialog-button--create"
            disabled={submitting}
            onClick={() => void createProject()}
            type="button"
          >
            {submitting ? 'Creating…' : 'Create Project'}
          </button>
        </div>
      </section>
    </div>
  );
}
