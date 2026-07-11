interface OnboardingScreenProps {
  onCreate: () => void;
  onOpenExample: () => void;
}

export function OnboardingScreen({ onCreate, onOpenExample }: OnboardingScreenProps) {
  return (
    <section
      className="mx-auto w-full max-w-[560px] px-6 pt-[16vh] pb-[100px] text-center"
      aria-labelledby="empty-projects-title"
    >
      <div className="mb-16 text-[11px] font-semibold tracking-[0.16em] text-faint uppercase">
        Lore Designer
      </div>
      <h1 className="mb-[18px] text-[38px] leading-[1.2] font-semibold" id="empty-projects-title">
        Nothing here yet.
      </h1>
      <p className="mx-auto mb-14 max-w-[440px] text-[16px] leading-[1.6] text-muted-foreground">
        A project is a world of its own — a folder of entries that remember each other. Start one
        from scratch, or step into a small finished example first.
      </p>
      <button
        className="mb-3.5 block w-full cursor-pointer rounded-[10px] border border-border px-[26px] py-[22px] text-left transition-colors hover:bg-hover"
        onClick={onCreate}
        type="button"
      >
        <span className="mb-1.5 block text-[19px] font-semibold">Create a new project</span>
        <span className="block text-sm leading-[1.5] text-muted-foreground">
          Start with an empty world and shape it your way.
        </span>
      </button>
      <button
        className="block w-full cursor-pointer rounded-[10px] border border-border px-[26px] py-[22px] text-left transition-colors hover:bg-hover"
        onClick={onOpenExample}
        type="button"
      >
        <span className="mb-1.5 block text-[19px] font-semibold">Open an example project</span>
        <span className="block text-sm leading-[1.5] text-muted-foreground">
          Explore “The Hollow,” a small pre-built world, to see how entries stay connected.
        </span>
      </button>
    </section>
  );
}
