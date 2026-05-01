import { useEditorShellStore } from "@/store/editor-shell";
import type { InlineSegment } from "@/types/editor";

function RichText({ segments }: { segments: InlineSegment[] }) {
  return (
    <>
      {segments.map((seg, i) =>
        seg.tone == null ? (
          <span key={i}>{seg.text}</span>
        ) : (
          <a
            key={i}
            style={{
              color: "var(--link)",
              textDecoration: "none",
              borderBottom: "1px solid rgba(61,86,124,0.32)",
              cursor: "pointer",
            }}
          >
            {seg.text}
          </a>
        ),
      )}
    </>
  );
}

export function DocumentEditor() {
  const { activeDocument } = useEditorShellStore();

  return (
    <section
      className="editor-document"
      style={{ flex: 1, overflow: "auto", minHeight: 0, padding: "40px 0" }}
    >
      <div className="editor-document__inner">
        <div className="editor-document__eyebrow">{activeDocument.kind}</div>

        <h1>{activeDocument.title}</h1>

        <div className="editor-document__aliases" style={{ marginBottom: 22 }}>
          {activeDocument.path}
        </div>

        {activeDocument.content.map((block) => {
          switch (block.type) {
            case "heading":
              return <h2 key={block.id}>{block.text}</h2>;
            case "paragraph":
              return (
                <p key={block.id}>
                  <RichText segments={block.segments} />
                </p>
              );
            case "callout":
              return (
                <div className="callout" key={block.id}>
                  <RichText segments={block.segments} />
                </div>
              );
            case "bullets":
              return (
                <ul
                  key={block.id}
                  style={{ paddingLeft: "1.3rem", lineHeight: 1.85, margin: "0.3rem 0 1rem" }}
                >
                  {block.items.map((item, idx) => (
                    <li key={idx}>
                      <RichText segments={item} />
                    </li>
                  ))}
                </ul>
              );
            default:
              return null;
          }
        })}
      </div>
    </section>
  );
}
