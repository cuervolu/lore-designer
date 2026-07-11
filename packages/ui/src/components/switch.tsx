import { Switch as SwitchPrimitive } from "@base-ui/react/switch";

import { cn } from "@lore/ui/lib/utils";

function Switch({ className, ...props }: SwitchPrimitive.Root.Props) {
  return (
    <SwitchPrimitive.Root
      data-slot="switch"
      className={cn(
        "peer inline-flex h-[22px] w-10 shrink-0 items-center rounded-full border border-transparent bg-border transition-colors outline-none focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 data-checked:bg-accent",
        className,
      )}
      {...props}
    >
      <SwitchPrimitive.Thumb
        data-slot="switch-thumb"
        className="pointer-events-none block size-4 translate-x-0.5 rounded-full bg-muted-foreground transition-transform data-checked:translate-x-[18px] data-checked:bg-primary"
      />
    </SwitchPrimitive.Root>
  );
}

export { Switch };
