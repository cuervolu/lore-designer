import { computed } from "vue";
import { usePreferencesStore } from "@common/stores/preferences.store";

export function useFontSize() {
  const preferencesStore = usePreferencesStore();

  const fontSize = computed(() => `${preferencesStore.font_size}px`);

  const fontSizeClasses = computed(() => {
    const size = preferencesStore.font_size;

    if (size <= 12) return "text-xs";
    if (size <= 14) return "text-sm";
    if (size <= 16) return "text-base";
    if (size <= 18) return "text-lg";
    return "text-xl";
  });

  return {
    fontSize,
    fontSizeClasses,
  };
}
