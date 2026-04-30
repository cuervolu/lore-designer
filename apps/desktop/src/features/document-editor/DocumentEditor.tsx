import { Button } from "@lore/ui";
import { BookMarked, Image as ImageIcon, PenSquare } from "lucide-react";
import { getTypeTone } from "@/store/editor-shell-helpers";
import { useEditorShellStore } from "@/store/editor-shell";
import type { InlineSegment } from "@/types/editor";
import { InlineEntityTag } from "@features/file-tree/LoreFileTree";

function RichSegments({ segments }: { segments: InlineSegment[] }) {
  return (
    <>
      {segments.map((segment, index) =>
        segment.tone == null ? (
          <span key={`${segment.text}-${index}`}>{segment.text}</span>
        ) : (
          <InlineEntityTag
            className="mx-1 align-middle"
            key={`${segment.text}-${index}`}
            label={segment.text}
            tone={segment.tone}
          />
        ),
      )}
    </>
  );
}

export function DocumentEditor() {
  const { activeDocument } = useEditorShellStore();

  return (
    <section className="document-editor">
      <div className="document-editor__scroll">
        <div className="document-editor__inner">
          <div
            className="document-cover"
            style={
              {
                "--cover-gradient": activeDocument.cover?.gradient ?? "none",
              } as React.CSSProperties
            }
          >
            <span
              aria-hidden="true"
              className="document-cover__glyph"
              style={{ color: activeDocument.cover?.accent ?? "#f2c4a8" }}
            >
              {activeDocument.icon ?? "📄"}
            </span>
          </div>

          <div className="document-toolbar">
            <Button size="sm" variant="ghost">
              <PenSquare />
              Add icon
            </Button>
            <Button size="sm" variant="ghost">
              <ImageIcon />
              Change cover
            </Button>
            <Button size="sm" variant="ghost">
              <BookMarked />
              Add to template
            </Button>
          </div>

          <div className="document-heading">
            <span className={`entity-link ${getTypeTone(activeDocument.kind)}`}>
              {activeDocument.kind}
            </span>
            <h1>{activeDocument.title}</h1>
          </div>

          <div className="document-body">
            {activeDocument.content.map((block) => {
              switch (block.type) {
                case "heading":
                  return (
                    <h2 className="document-body__heading" key={block.id}>
                      {block.text}
                    </h2>
                  );
                case "paragraph":
                  return (
                    <p className="document-body__paragraph" key={block.id}>
                      <RichSegments segments={block.segments} />
                    </p>
                  );
                case "callout":
                  return (
                    <div className="document-callout" key={block.id}>
                      <span className="document-callout__icon">{block.icon ?? "✦"}</span>
                      <p>
                        <RichSegments segments={block.segments} />
                      </p>
                    </div>
                  );
                case "bullets":
                  return (
                    <ul className="document-body__list" key={block.id}>
                      {block.items.map((item, index) => (
                        <li key={`${block.id}-${index}`}>
                          <RichSegments segments={item} />
                        </li>
                      ))}
                    </ul>
                  );
                default:
                  return null;
              }
            })}
          </div>
        </div>
      </div>
    </section>
  );
}
