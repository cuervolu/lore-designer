import { useState } from 'react';
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@lore/ui/components/breadcrumb';
import { Button } from '@lore/ui/components/button';
import { ButtonGroup } from '@lore/ui/components/button-group';
import { Columns2, Search } from 'lucide-react';
import { useEditorShellStore } from '@/store/editor-shell';
import { DocumentEditor } from '@features/document-editor/DocumentEditor';
import { EditorTabs } from '@features/editor-tabs/EditorTabs';

export function WorkspaceEditorPane() {
  const { activeDocument } = useEditorShellStore();

  return (
    <section className="workspace-editor">
      <EditorTabs />
      <EditorSubtoolbar title={activeDocument.title} kind={activeDocument.kind} />
      <DocumentEditor />
      <div className="editor-statusbar">
        <span>
          {activeDocument.kind} · {activeDocument.title}
        </span>
        <span>1,284 words · last edit 2 min ago</span>
      </div>
    </section>
  );
}

function EditorSubtoolbar({ title, kind }: { title: string; kind: string }) {
  const { workspaceName } = useEditorShellStore();
  const [view, setView] = useState<'read' | 'edit' | 'outline'>('read');

  return (
    <div className="editor-subtoolbar">
      <Breadcrumb className="editor-breadcrumbs">
        <BreadcrumbList>
          <BreadcrumbItem>{workspaceName}</BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>{kind.charAt(0).toUpperCase() + kind.slice(1)}s</BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage className="editor-breadcrumbs__current">{title}</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>

      <div className="editor-view-controls">
        <ButtonGroup className="editor-view-controls__group">
          {(['read', 'edit', 'outline'] as const).map((value) => (
            <Button
              className="editor-seg-btn"
              key={value}
              onClick={() => setView(value)}
              size="xs"
              type="button"
              variant={view === value ? 'outline' : 'ghost'}
            >
              {value.charAt(0).toUpperCase() + value.slice(1)}
            </Button>
          ))}
        </ButtonGroup>
        <span className="editor-view-sep" />
        <Button
          className="editor-view-controls__icon-button"
          size="icon-xs"
          title="Find"
          type="button"
          variant="ghost"
        >
          <Search data-icon="inline-start" strokeWidth={1.5} />
        </Button>
        <Button
          className="editor-view-controls__icon-button"
          size="icon-xs"
          title="Split view"
          type="button"
          variant="ghost"
        >
          <Columns2 data-icon="inline-start" strokeWidth={1.5} />
        </Button>
      </div>
    </div>
  );
}
