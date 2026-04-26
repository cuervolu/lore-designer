import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from "@lore/ui";
import { BookOpen, FolderTree, Search, Sparkles } from "lucide-react";

const workspaceSections = [
  {
    title: "Workspace-first",
    description:
      "Markdown files stay on disk. Lore Designer gives them structure without taking ownership away from the author.",
    icon: FolderTree,
  },
  {
    title: "Inspectable lore",
    description:
      "Properties, metadata, and future templates are designed to feel readable instead of hidden behind the app.",
    icon: Search,
  },
  {
    title: "Built for story systems",
    description:
      "This shell is the starting point for characters, locations, lore documents, and dynamic query views.",
    icon: BookOpen,
  },
];

function App() {
  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="app-shell">
        <aside className="sidebar-surface flex w-full max-w-[18rem] flex-col gap-6 p-6">
          <div className="flex flex-col gap-2">
            <p className="text-xs font-medium uppercase tracking-[0.24em] text-muted-foreground">
              Lore Designer
            </p>
            <h1 className="text-2xl font-semibold tracking-tight text-balance">
              A quiet workspace for worldbuilders.
            </h1>
          </div>

          <div className="flex flex-col gap-3">
            <div className="panel-surface flex items-start gap-3 rounded-xl p-4">
              <Sparkles className="mt-0.5 text-primary" />
              <div className="flex flex-col gap-1">
                <p className="text-sm font-medium">Shared visual foundation</p>
                <p className="text-sm text-muted-foreground">
                  Tokens, shadcn primitives, and global styles now live in
                  <code className="ml-1 rounded bg-muted px-1.5 py-0.5 text-[0.8rem] text-foreground">
                    @lore/ui
                  </code>
                  .
                </p>
              </div>
            </div>

            <div className="panel-surface rounded-xl p-4">
              <p className="text-sm font-medium">Current targets</p>
              <div className="mt-3 flex flex-wrap gap-2">
                <span className="entity-link entity-type--character">Characters</span>
                <span className="entity-link entity-type--location">Locations</span>
                <span className="entity-link entity-type--lore">Lore</span>
              </div>
            </div>
          </div>
        </aside>

        <main className="flex min-w-0 flex-1 flex-col gap-6 p-6 md:p-8">
          <section className="hero-surface overflow-hidden rounded-[1.75rem] p-8 md:p-10">
            <div className="relative z-10 flex max-w-3xl flex-col gap-5">
              <p className="text-sm font-medium uppercase tracking-[0.22em] text-primary">
                Monorepo foundation
              </p>
              <div className="flex flex-col gap-3">
                <h2 className="max-w-2xl text-4xl font-semibold tracking-tight text-balance">
                  Desktop app and reusable UI now share the same design system.
                </h2>
                <p className="max-w-2xl text-base leading-7 text-muted-foreground md:text-lg">
                  This first shell keeps the old visual direction: soft surfaces, muted neutrals,
                  editorial spacing, and a calm panel hierarchy ready for the real product modules.
                </p>
              </div>

              <div className="flex flex-wrap gap-3">
                <Button size="lg">Open Workspace</Button>
                <Button variant="outline" size="lg">
                  Explore Templates
                </Button>
              </div>
            </div>
          </section>

          <section className="grid gap-4 xl:grid-cols-3">
            {workspaceSections.map(({ title, description, icon: Icon }) => (
              <Card key={title} className="h-full">
                <CardHeader className="flex flex-row items-start gap-4">
                  <div className="panel-icon">
                    <Icon />
                  </div>
                  <div className="flex flex-col gap-1.5">
                    <CardTitle>{title}</CardTitle>
                    <CardDescription>{description}</CardDescription>
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-sm leading-6 text-muted-foreground">
                    The app surface is now decoupled from Tauri-specific styling, so the same
                    language can later power docs and marketing web surfaces without duplication.
                  </p>
                </CardContent>
              </Card>
            ))}
          </section>
        </main>
      </div>
    </div>
  );
}

export default App;
