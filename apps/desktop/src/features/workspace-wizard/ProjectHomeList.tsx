import { Plus } from 'lucide-react';
import type { ProjectSummary } from '@/types/editor';

interface ProjectHomeListProps {
  onCreate: () => void;
  onOpenProject: (project: ProjectSummary) => void;
  projects: ProjectSummary[];
}

export function ProjectHomeList({ onCreate, onOpenProject, projects }: ProjectHomeListProps) {
  return (
    <section
      className="mx-auto w-full max-w-[600px] px-6 pt-[18vh] pb-[100px]"
      aria-labelledby="home-wordmark"
    >
      <div
        className="mb-16 text-[11px] font-semibold tracking-[0.16em] text-faint uppercase"
        id="home-wordmark"
      >
        Lore Designer
      </div>
      {projects.map((project) => (
        <button
          className="group block w-full cursor-pointer border-b border-border px-1 py-6 text-left"
          key={project.id}
          onClick={() => onOpenProject(project)}
          type="button"
        >
          <span className="block text-[27px] leading-[1.25] font-semibold transition-colors group-hover:text-primary">
            {project.name}
          </span>
          <span className="mt-[7px] block text-[13px] leading-[1.5] text-muted-foreground">
            {project.lastEdited} · {project.entryCount} entries
            {project.status ? ` · ${project.status}` : ''}
          </span>
        </button>
      ))}
      <button
        className="flex w-full cursor-pointer items-center gap-1.5 px-1 py-6 text-left text-[16px] text-faint italic hover:text-primary"
        onClick={onCreate}
        type="button"
      >
        <Plus aria-hidden="true" size={16} strokeWidth={1.6} />
        New project…
      </button>
    </section>
  );
}
