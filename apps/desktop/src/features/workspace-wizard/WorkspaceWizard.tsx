import { useState } from 'react';
import { Settings } from 'lucide-react';
import { Button } from '@lore/ui';
import { RECENT_PROJECTS } from '@/store/mock-data';
import type { ProjectSummary } from '@/types/editor';
import { NewProjectDialog } from './NewProjectDialog';
import { OnboardingScreen } from './OnboardingScreen';
import { ProjectHomeList } from './ProjectHomeList';

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
    <main className="relative h-full w-full overflow-y-auto">
      <Button
        aria-label="Open settings"
        className="absolute top-7 right-8 text-faint hover:text-primary"
        onClick={onOpenSettings}
        size="icon"
        variant="ghost"
      >
        <Settings aria-hidden="true" size={17} strokeWidth={1.6} />
      </Button>

      {projects.length > 0 ? (
        <ProjectHomeList
          onCreate={() => setDialogOpen(true)}
          onOpenProject={onOpenProject}
          projects={projects}
        />
      ) : (
        <OnboardingScreen onCreate={() => setDialogOpen(true)} onOpenExample={onOpenExample} />
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
