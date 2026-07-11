import { ToggleGroup as ToggleGroupPrimitive } from "@base-ui/react/toggle-group";
import { Toggle as TogglePrimitive } from "@base-ui/react/toggle";

import { cn } from "@lore/ui/lib/utils";

interface SegmentedControlProps<Value extends string> extends Omit<
  ToggleGroupPrimitive.Props<Value>,
  "multiple"
> {
  size?: "default" | "compact";
}

function SegmentedControl<Value extends string>({
  className,
  size = "default",
  ...props
}: SegmentedControlProps<Value>) {
  return (
    <ToggleGroupPrimitive
      data-slot="segmented-control"
      data-size={size}
      className={cn(
        "inline-flex items-center gap-0.5 rounded-lg border border-border bg-sidebar p-0.75",
        className,
      )}
      {...props}
    />
  );
}

function SegmentedControlItem<Value extends string>({
  className,
  ...props
}: TogglePrimitive.Props<Value>) {
  return (
    <TogglePrimitive
      data-slot="segmented-control-item"
      className={cn(
        "cursor-pointer rounded-md px-4 py-1.5 text-[13px] font-semibold text-muted-foreground outline-none transition-colors select-none focus-visible:ring-2 focus-visible:ring-ring/50 data-pressed:bg-accent data-pressed:text-primary in-data-[size=compact]:px-3.5 in-data-[size=compact]:py-1 in-data-[size=compact]:text-xs",
        className,
      )}
      {...props}
    />
  );
}

export { SegmentedControl, SegmentedControlItem };
