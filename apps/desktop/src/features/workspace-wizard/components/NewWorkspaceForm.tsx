import { AlertTriangle, Check, ChevronLeft, Feather, Folder } from "lucide-react";
import { useState } from "react";

interface NewWorkspaceFormProps {
  onBack: () => void;
  onCreate: (data: { name: string; openAfter: boolean; path: string }) => void;
}

export function NewWorkspaceForm({ onBack, onCreate }: NewWorkspaceFormProps) {
  const [name, setName] = useState("");
  const [parent, setParent] = useState("~/Worlds");
  const [openAfter, setOpenAfter] = useState(true);
  const [errors, setErrors] = useState<Record<string, string | undefined>>({});
  const [submitting, setSubmitting] = useState(false);

  const slug = name
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
  const fullPath = slug ? `${parent.replace(/\/$/, "")}/${slug}` : `${parent.replace(/\/$/, "")}/…`;

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    const errs: Record<string, string> = {};
    if (!name.trim()) errs.name = "Give your world a name.";
    else if (name.trim().length < 2) errs.name = "Names must be at least 2 characters.";
    if (!parent.trim()) errs.parent = "Choose a folder.";
    setErrors(errs);
    if (Object.keys(errs).length) return;
    setSubmitting(true);
    setTimeout(() => {
      onCreate({ name: name.trim(), openAfter, path: fullPath });
    }, 600);
  };

  return (
    <div className="wiz-inner wiz-fade">
      <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
        <button
          aria-label="Back"
          className="btn ghost"
          onClick={onBack}
          style={{ width: "2rem", padding: 0, flexShrink: 0 }}
          type="button"
        >
          <ChevronLeft size={16} />
        </button>
        <div>
          <div
            style={{
              fontSize: "0.6875rem",
              fontWeight: 600,
              color: "var(--muted-foreground)",
              textTransform: "uppercase",
              letterSpacing: "0.08em",
            }}
          >
            Step 1 of 1
          </div>
          <div
            style={{
              fontSize: "1.125rem",
              fontWeight: 700,
              letterSpacing: "-0.01em",
              marginTop: 2,
            }}
          >
            Begin a new world
          </div>
        </div>
      </div>

      <form className="wiz-form" onSubmit={submit}>
        <h2>Workspace details</h2>

        <label className="wiz-field">
          Name
          <input
            autoFocus
            className="wiz-input"
            onChange={(e) => {
              setName(e.target.value);
              setErrors((es) => ({ ...es, name: undefined }));
            }}
            placeholder="The Ashen Coast"
            value={name}
          />
        </label>
        {errors.name && (
          <div className="wiz-form__err">
            <AlertTriangle size={12} /> {errors.name}
          </div>
        )}

        <label className="wiz-field">
          Parent folder
          <div className="wiz-form__row">
            <input
              className="wiz-input wiz-input--mono"
              onChange={(e) => {
                setParent(e.target.value);
                setErrors((es) => ({ ...es, parent: undefined }));
              }}
              placeholder="~/Worlds"
              value={parent}
            />
            <button className="btn" onClick={() => setParent("~/Documents/Worlds")} type="button">
              <Folder size={14} /> Browse…
            </button>
          </div>
        </label>
        {errors.parent && (
          <div className="wiz-form__err">
            <AlertTriangle size={12} /> {errors.parent}
          </div>
        )}

        <div className="wiz-form__preview">{fullPath}</div>

        <label className="wiz-check">
          <input
            checked={openAfter}
            className="sr-only"
            onChange={(e) => setOpenAfter(e.target.checked)}
            type="checkbox"
          />
          <span className={`wiz-cb${openAfter ? " wiz-cb--checked" : ""}`}>
            <Check size={10} strokeWidth={2.5} style={{ opacity: openAfter ? 1 : 0 }} />
          </span>
          Open after creation
        </label>

        <div className="wiz-form__divider" />

        <div className="wiz-form__actions">
          <button className="btn" disabled={submitting} onClick={onBack} type="button">
            Cancel
          </button>
          <button className="btn primary" disabled={submitting} type="submit">
            {submitting ? (
              <>
                <svg
                  className="wiz-spin"
                  fill="none"
                  height="14"
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeWidth="2.4"
                  viewBox="0 0 24 24"
                  width="14"
                >
                  <path d="M21 12a9 9 0 1 1-6.2-8.55" opacity="0.85" />
                </svg>
                Creating…
              </>
            ) : (
              <>
                <Feather size={14} /> Create Workspace
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
