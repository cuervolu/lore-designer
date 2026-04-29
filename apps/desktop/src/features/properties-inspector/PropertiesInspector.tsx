import { cn } from "@lore/ui";
import { ChevronRight } from "lucide-react";
import { getTypeTone } from "@core/editor-shell/helpers";
import { useEditorShellStore } from "@core/editor-shell/store";
import type { FrontmatterField } from "@core/editor-shell/types";

function InspectorField({
  field,
  path,
  updateFrontmatterField,
}: {
  field: FrontmatterField;
  path: string;
  updateFrontmatterField: (
    path: string,
    fieldKey: string,
    value: FrontmatterField["value"],
  ) => void;
}) {
  if (field.type === "select" && field.options != null) {
    return (
      <select
        className="inspector-select"
        onChange={(event) => updateFrontmatterField(path, field.key, event.target.value)}
        value={String(field.value)}
      >
        {field.options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    );
  }

  if (field.type === "tags" || field.type === "relations") {
    return (
      <div className="inspector-chip-list">
        {(field.value as string[]).map((item) => (
          <span
            className={cn(
              "entity-link",
              field.type === "tags" ? "entity-link--muted" : "entity-type--character",
            )}
            key={item}
          >
            {item}
          </span>
        ))}
        <button className="inspector-chip-add" type="button">
          + Add
        </button>
      </div>
    );
  }

  return (
    <input
      className="inspector-input"
      onChange={(event) =>
        updateFrontmatterField(
          path,
          field.key,
          field.type === "number" ? Number(event.target.value) : event.target.value,
        )
      }
      type={field.type === "number" ? "number" : "text"}
      value={String(field.value)}
    />
  );
}

export function PropertiesInspector() {
  const { activeDocument, updateFrontmatterField } = useEditorShellStore();

  return (
    <aside className="properties-panel">
      <div className="properties-panel__header">
        <span>Properties</span>
        <ChevronRight className="size-4 opacity-60" />
      </div>

      <div className="properties-panel__body">
        <section className="properties-section">
          <p className="properties-section__label">Type</p>
          <span className={cn("entity-link entity-link--strong", getTypeTone(activeDocument.kind))}>
            {activeDocument.kind}
          </span>
        </section>

        <section className="properties-section">
          <p className="properties-section__label">Frontmatter</p>
          <div className="properties-grid">
            {activeDocument.frontmatter.map((field) => (
              <div className="properties-field" key={field.key}>
                <label className="properties-field__label" htmlFor={`field-${field.key}`}>
                  {field.label}
                </label>
                <InspectorField
                  field={field}
                  path={activeDocument.path}
                  updateFrontmatterField={updateFrontmatterField}
                />
              </div>
            ))}
          </div>
        </section>

        <section className="properties-section">
          <div className="properties-field">
            <p className="properties-field__label">Backlinks · {activeDocument.backlinks.length}</p>
            <div className="inspector-chip-list">
              {activeDocument.backlinks.map((item) => (
                <span className="entity-link entity-type--lore" key={item}>
                  {item}
                </span>
              ))}
            </div>
          </div>
        </section>
      </div>
    </aside>
  );
}
