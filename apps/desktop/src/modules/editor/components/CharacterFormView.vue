<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { toast } from "vue-sonner";
import TipTapEditor from "@editor/components/TipTapEditor.vue";
import { useEditorStore } from "@editor/stores/editor.store";
import type { EditorFile } from "@editor/types/editor.types";
import { parse, stringify } from "yaml";
import { error as logError } from "tauri-plugin-tracing";
import { useDebounceFn } from "@vueuse/core";
import { useFormConfig } from "@editor/composables/useFormConfig";
import DynamicField from "./dynamic-form/DynamicField.vue";
import { Skeleton } from "@/components/ui/skeleton";
import { invoke } from "@tauri-apps/api/core";

const props = defineProps<{
  file: EditorFile;
}>();
const editorStore = useEditorStore();

const { config, isLoading: isConfigLoading, loadConfig } = useFormConfig();

const formData = ref<Record<string, any>>({});
const isUpdatingFromStore = ref(false);

const heroFields = computed(
  () => config.value?.sections.find((s) => s.key === "hero")?.fields || [],
);
const sidebarFields = computed(
  () => config.value?.sections.find((s) => s.key === "sidebar")?.fields || [],
);
const gridFields = computed(
  () => config.value?.sections.find((s) => s.key === "grid")?.fields || [],
);
const customFields = computed(
  () => config.value?.sections.find((s) => s.key === "custom")?.fields || [],
);
const lastParsedFrontmatter = ref<string | null>(null);

function parseFrontmatter() {
  const frontmatter = editorStore.activeFileFrontmatter;

  if (frontmatter === lastParsedFrontmatter.value) {
    return;
  }

  lastParsedFrontmatter.value = frontmatter;

  if (!frontmatter) {
    resetForm();
    return;
  }

  try {
    isUpdatingFromStore.value = true;
    const parsed = (parse(frontmatter) as any) || {};

    formData.value = parsed;

    if (!formData.value.aliases) formData.value.aliases = [];
    if (parsed.custom_properties && !formData.value.customProperties) {
      formData.value.customProperties = Array.isArray(parsed.custom_properties)
        ? parsed.custom_properties
        : [];
    } else if (!formData.value.customProperties) {
      formData.value.customProperties = [];
    }
  } catch (error) {
    logError(`Failed to parse character frontmatter: ${error}`);
    toast.error("Failed to parse character frontmatter");
  } finally {
    setTimeout(() => {
      isUpdatingFromStore.value = false;
    }, 100);
  }
}

function resetForm() {
  formData.value = {};
}

function serializeToYaml(): string {
  const data: any = { ...formData.value };

  if (data.aliases && Array.isArray(data.aliases)) {
    data.aliases = data.aliases.filter((a: string) => a && a.trim());
    if (data.aliases.length === 0) delete data.aliases;
  }

  if (data.customProperties) {
    const propsArray = Array.isArray(data.customProperties)
      ? data.customProperties
      : [];

    data.custom_properties = propsArray.filter(
      (prop: any) => prop.key && prop.key.trim(),
    );

    if (data.custom_properties.length === 0) delete data.custom_properties;
    delete data.customProperties;
  }

  Object.keys(data).forEach((key) => {
    if (data[key] === null) delete data[key];
  });

  return stringify(data, { indent: 2 });
}

function updateStoreState() {
  if (isUpdatingFromStore.value) return;

  try {
    const serialized = serializeToYaml();
    lastParsedFrontmatter.value = serialized;
    editorStore.activeFileFrontmatter = serialized;
    editorStore.markTabAsChanged();
  } catch (e) {
    logError(`Failed to stringify/update store state: ${e}`);
  }
}

const debouncedUpdateImageReferences = useDebounceFn(async () => {
  const currentFilePath = editorStore.activeTab?.path;
  if (!currentFilePath || !editorStore.currentWorkspace?.path) {
    return;
  }

  const imagePaths: string[] = [];

  const allFields = [
    ...(heroFields.value || []),
    ...(sidebarFields.value || []),
    ...(gridFields.value || []),
    ...(customFields.value || []),
  ];

  for (const field of allFields) {
    if (field.type === "image" && formData.value[field.key]) {
      imagePaths.push(formData.value[field.key]);
    }
  }

  if (imagePaths.length > 0 || editorStore.activeTab?.path) {
    try {
      await invoke("update_image_references", {
        workspacePath: editorStore.currentWorkspace.path,
        filePath: currentFilePath,
        imagePaths,
      });
    } catch (err) {
      await logError(`Failed to update image references: ${err}`);
    }
  }
}, 500);

const handleBodyUpdate = (newContent: string) => {
  editorStore.updateActiveFileContent(newContent);
  editorStore.markTabAsChanged();
};

function handleBodyTextUpdate(_plainText: string) { }

watch(
  () => props.file,
  () => {
    parseFrontmatter();
  },
  { immediate: true },
);

watch(
  formData,
  () => {
    updateStoreState();
    const hasImageFields = [
      ...(heroFields.value || []),
      ...(sidebarFields.value || []),
      ...(gridFields.value || []),
      ...(customFields.value || []),
    ].some((field) => field.type === "image");

    if (hasImageFields) {
      debouncedUpdateImageReferences();
    }
  },
  { deep: true },
);

onMounted(() => {
  parseFrontmatter();
  loadConfig();
});
</script>

<template>
  <div class="h-full w-full overflow-y-auto bg-background">
    <div v-if="isConfigLoading" class="max-w-5xl mx-auto px-6 py-8 space-y-6"></div>

    <div v-else-if="config" class="max-w-5xl mx-auto px-6 py-8">
      <div class="flex gap-8 items-start mb-8">
        <div class="flex-1 space-y-6">
          <div v-if="heroFields.length > 0">
            <DynamicField v-for="field in heroFields" :key="field.key" :field-definition="field"
              v-model="formData[field.key]" />
          </div>

          <div v-if="gridFields.length > 0" class="grid grid-cols-2 gap-4 pt-4">
            <DynamicField v-for="field in gridFields" :key="field.key" :field-definition="field"
              v-model="formData[field.key]" />
          </div>
        </div>

        <div class="shrink-0">
          <div v-if="sidebarFields.length > 0">
            <DynamicField v-for="field in sidebarFields" :key="field.key" :field-definition="field"
              v-model="formData[field.key]" />
          </div>
        </div>
      </div>

      <div class="space-y-6 pt-6 border-t border-2 border-border rounded-lg overflow-hidden bg-muted/5">
        <TipTapEditor :modelValue="editorStore.activeFileContent" @update:modelValue="handleBodyUpdate"
          @textUpdate="handleBodyTextUpdate" placeholder="Write your character's description, backstory, personality..."
          class="p-4" />
      </div>

      <div class="pt-6 border-t">
        <div v-if="customFields.length > 0">
          <DynamicField v-for="field in customFields" :key="field.key" :field-definition="field"
            v-model="formData[field.key]" />
        </div>
      </div>
    </div>
  </div>
</template>
