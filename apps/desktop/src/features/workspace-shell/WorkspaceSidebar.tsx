import { Sidebar } from '@lore/ui/components/sidebar';
import { LoreFileTree } from '@features/file-tree/LoreFileTree';

export function WorkspaceSidebar() {
  return (
    <Sidebar className="workspace-sidebar" collapsible="none" side="left">
      <LoreFileTree />
    </Sidebar>
  );
}
