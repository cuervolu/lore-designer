import { open } from '@tauri-apps/plugin-dialog';
import { useEffect, useMemo, useState } from 'react';
import { documentDir } from '@tauri-apps/api/path';
import { createWorkspace } from '@api/workspace';
import type {
  CreateWorkspaceResult,
  WorkspaceTemplateSummary,
} from '@features/workspace-wizard/types';

interface NewProjectPaneProps {
  appVersion: string | null;
  onOpen: (workspace: CreateWorkspaceResult) => void;
  templates: WorkspaceTemplateSummary[];
}

export function NewProjectPane({ appVersion, onOpen, templates }: NewProjectPaneProps) {
  const [name, setName] = useState('');
  const [path, setPath] = useState('');
  const [templateId, setTemplateId] = useState('blank');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (templates.length === 0) {
      return;
    }

    const nextSelected = templates.find((template) => template.id === templateId)
      ? templateId
      : templates[0].id;
    if (nextSelected !== templateId) {
      setTemplateId(nextSelected);
    }
  }, [templateId, templates]);

  useEffect(() => {
    let cancelled = false;

    void documentDir()
      .then((directory) => {
        if (!cancelled) {
          setPath((current) => current || `${directory.replace(/\/$/, '')}/Lore`);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setPath((current) => current || 'Lore');
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const selectedTemplate = useMemo(
    () => templates.find((template) => template.id === templateId) ?? null,
    [templateId, templates],
  );

  const canSubmit =
    !submitting &&
    name.trim().length >= 2 &&
    path.trim().length > 0 &&
    selectedTemplate?.supportsCreation === true;

  const handleChooseLocation = async () => {
    const selected = await open({
      defaultPath: path,
      directory: true,
      multiple: false,
      title: 'Choose project folder',
    });

    if (typeof selected === 'string') {
      setPath(selected);
      setError(null);
    }
  };

  const handleCreateProject = async () => {
    if (!selectedTemplate) {
      setError('No template is available yet.');
      return;
    }

    if (!name.trim()) {
      setError('Give your project a name.');
      return;
    }

    if (!path.trim()) {
      setError('Choose where the project should be created.');
      return;
    }

    if (!selectedTemplate.supportsCreation) {
      setError(`${selectedTemplate.displayName} is not available yet.`);
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      const workspace = await createWorkspace({
        appVersion: appVersion ?? '0.1.0',
        name: name.trim(),
        path: path.trim(),
        templateId: selectedTemplate.id,
      });
      onOpen(workspace);
    } catch (nextError) {
      const message = nextError instanceof Error ? nextError.message : 'Failed to create project.';
      setError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="wiz-form-wrap">
      <h1 className="wiz-form-title">New project</h1>

      <div className="wiz-form-row">
        <label className="wiz-form-label">Name</label>
        <input
          autoFocus
          className="wiz-input"
          onChange={(e) => setName(e.target.value)}
          placeholder="The Quiet Atlas"
          value={name}
        />
      </div>

      <div className="wiz-form-row">
        <label className="wiz-form-label">Location</label>
        <div>
          <div style={{ display: 'flex', gap: 6 }}>
            <input
              className="wiz-input wiz-input--mono"
              onChange={(e) => setPath(e.target.value)}
              style={{ flex: 1 }}
              value={path}
            />
            <button className="ld-btn" onClick={() => void handleChooseLocation()} type="button">
              Choose…
            </button>
          </div>
          <div className="wiz-input-hint">
            Enter the target workspace folder. It may already exist if it is empty.
          </div>
        </div>
      </div>

      <div className="wiz-form-row">
        <label className="wiz-form-label">Start from</label>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          {templates.map((template) => (
            <button
              aria-disabled={!template.supportsCreation}
              className={`wiz-template-option${templateId === template.id ? ' selected' : ''}`}
              key={template.id}
              onClick={() => setTemplateId(template.id)}
              style={!template.supportsCreation ? { opacity: 0.62 } : undefined}
              type="button"
            >
              <div
                className={`wiz-template-option__radio${templateId === template.id ? ' selected' : ''}`}
              >
                {templateId === template.id && <div className="wiz-template-option__radio-dot" />}
              </div>
              <div>
                <div className="wiz-template-option__title">
                  {template.displayName}
                  {!template.supportsCreation && (
                    <span style={{ marginLeft: 8, color: 'var(--ink-4)', fontSize: 11 }}>
                      Coming soon
                    </span>
                  )}
                </div>
                <div className="wiz-template-option__desc">{template.description}</div>
                {template.id === 'sample' && templateId === 'sample' && <SampleSummary />}
              </div>
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="wiz-input-hint" style={{ color: '#c6654f' }}>
          {error}
        </div>
      )}

      <div className="wiz-form-actions">
        <button className="ld-btn" type="button">
          Cancel
        </button>
        <button
          className="ld-btn primary"
          disabled={!canSubmit}
          onClick={() => void handleCreateProject()}
          type="button"
        >
          {submitting ? 'Creating…' : 'Create project'}
        </button>
      </div>
    </div>
  );
}

function SampleSummary() {
  return (
    <div
      style={{
        marginTop: 10,
        padding: '8px 10px',
        borderRadius: 5,
        background: 'rgba(125,118,102,0.08)',
        border: '1px solid var(--divider)',
        fontSize: 11.5,
        color: 'var(--ink-2)',
        lineHeight: 1.55,
        fontFamily: 'var(--font-sans)',
      }}
    >
      <div style={{ color: 'var(--ink-3)', marginBottom: 4 }}>Includes</div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2px 16px' }}>
        <span>5 characters · Wren, Corvin, Mira…</span>
        <span>5 locations · Saltreach, Lowmarsh…</span>
        <span>3 factions · 4 lore entries</span>
        <span>4 drafts incl. Chapter 01</span>
        <span>2 open threads</span>
        <span>Cross-references between all of the above</span>
      </div>
    </div>
  );
}
