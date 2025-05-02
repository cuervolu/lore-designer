<script setup lang="ts">
import {lstat} from '@tauri-apps/plugin-fs';
import {error as logError} from '@tauri-apps/plugin-log';
import {computed, nextTick, ref, watch} from 'vue';
import {AlertTriangle, ChevronDown, ChevronRight, Info, Search} from 'lucide-vue-next';
import {parse as yamlParse, stringify as yamlStringify} from 'yaml';
import {Input} from '@/components/ui/input';
import {Textarea} from '@/components/ui/textarea';
import {ScrollArea} from '@/components/ui/scroll-area';
import {useEditorStore} from '@editor/stores/editor.store';
import type {EditorFile} from '@editor/types/editor.types.ts';
import {Button} from '@/components/ui/button';
import {Collapsible, CollapsibleContent, CollapsibleTrigger} from '@/components/ui/collapsible';
import {toast} from "vue-sonner";

const props = defineProps<{
  file?: EditorFile | null;
}>();

const editorStore = useEditorStore();

// --- State ---
const searchQuery = ref('');
const isLoading = ref(false);
const fileProperties = ref<Array<{ name: string; value: string }>>([]);
const frontmatterData = ref<Record<string, any> | null>(null);
const frontmatterParseError = ref<string | null>(null);
const isFrontmatterDirty = ref(false); // Local flag for frontmatter changes
const openSections = ref<Record<string, boolean>>({
  'File Info': true,
  'Frontmatter': true,
});

const hasFrontmatter = computed(() => {
  return frontmatterData.value !== null && Object.keys(frontmatterData.value).length > 0;
});

const supportsFrontmatter = computed(() => {
  if (!props.file) return false;
  // Determine if the current file type *should* have frontmatter based on convention/type
  return ['character', 'location', 'lore', 'gdd', 'markdown'].some(
    type => props.file?.extension?.toLowerCase().includes(type)
  );
});

const filteredFileProperties = computed(() => {
  if (!searchQuery.value) return fileProperties.value;
  const query = searchQuery.value.toLowerCase();
  return fileProperties.value.filter(prop =>
    prop.name.toLowerCase().includes(query) ||
    prop.value.toLowerCase().includes(query)
  );
});

// Filter frontmatter entries based on search (simple top-level key search)
const filteredFrontmatterEntries = computed(() => {
  if (!frontmatterData.value || !searchQuery.value) {
    return Object.entries(frontmatterData.value || {});
  }
  const query = searchQuery.value.toLowerCase();
  return Object.entries(frontmatterData.value).filter(([key]) =>
    key.toLowerCase().includes(query)
  );
});

watch(() => props.file, async (newFile) => {
    await loadInspectorData(newFile);
  },
  { immediate: true }
);

watch(frontmatterData, (newData, oldData) => {
  if (isLoading.value) return;

  // Need a reliable way to detect actual changes, deep watch can be tricky
  // For now, assume any change after load makes it dirty
  // A more robust way might involve comparing stringified versions, but can be slow
  if (newData && oldData && JSON.stringify(newData) !== JSON.stringify(oldData)) {
    isFrontmatterDirty.value = true;
    editorStore.markTabAsChanged();
    updateStoreFrontmatter();
  }
}, { deep: true });


async function loadInspectorData(file: EditorFile | null) {
  if (!file || !editorStore.currentWorkspace) {
    resetInspectorState();
    return;
  }

  isLoading.value = true;
  resetInspectorState();

  try {
    await loadBasicFileProperties(file);

    const fileContent = await editorStore.getFileContent(file.path);

    if (fileContent && supportsFrontmatter.value) {
      let rawFrontmatter: string | null = null;

      switch (fileContent.type) {
        case 'Character':
        case 'Location':
        case 'Lore':
          rawFrontmatter = fileContent.data.frontmatter;
          break;
        case 'Markdown':
          break;
      }

      // Update store's temporary state immediately (even if null)
      editorStore.activeFileFrontmatter = rawFrontmatter;

      if (rawFrontmatter) {
        parseFrontmatter(rawFrontmatter);
      } else {
        frontmatterData.value = {};
      }
    }
  } catch (err) {
    await logError(`Failed to load inspector data for ${file.path}: ${err}`);
    frontmatterParseError.value = `Failed to load data: ${err}`;
  } finally {
    isLoading.value = false;
    await nextTick(); // Ensure DOM is updated before resetting
    isFrontmatterDirty.value = false;
  }
}

function resetInspectorState() {
  fileProperties.value = [];
  frontmatterData.value = null;
  frontmatterParseError.value = null;
  isFrontmatterDirty.value = false;
  // Don't reset store's activeFileFrontmatter here, loadInspectorData handles it
}

async function loadBasicFileProperties(file: EditorFile) {
  if (!editorStore.currentWorkspace) return;
  const fullPath = `${editorStore.currentWorkspace.path}/${file.path}`;
  try {
    const metadata = await lstat(fullPath);
    fileProperties.value = [
      { name: 'Filename', value: file.name },
      { name: 'Path', value: file.path },
      { name: 'Extension', value: file.extension },
      { name: 'Size', value: formatFileSize(metadata.size) },
      { name: 'Modified', value: metadata.mtime?.toLocaleString() ?? 'N/A' },
      { name: 'Created', value: metadata.birthtime?.toLocaleString() ?? 'N/A' },
    ];
  } catch (err) {
    await logError(`Failed to get file metadata for ${fullPath}: ${err}`);
    fileProperties.value = [ // Show basic info even on error
      { name: 'Filename', value: file.name },
      { name: 'Path', value: file.path },
      { name: 'Error', value: 'Could not load file metadata' },
    ];
  }
}

async function parseFrontmatter(yamlString: string) {
  try {
    frontmatterData.value = yamlParse(yamlString);
    frontmatterParseError.value = null;
  } catch (e: any) {
    await logError(`YAML Parse Error: ${e}`);
    frontmatterParseError.value = `Invalid YAML: ${e.message || 'Syntax error'}`;
    frontmatterData.value = null; // Clear data on parse error
  }
}

async function updateStoreFrontmatter() {
  if (frontmatterData.value !== null) {
    try {
      editorStore.activeFileFrontmatter = yamlStringify(frontmatterData.value);
    } catch (e: any) {
      await logError(`YAML Stringify Error: ${e}`);
      toast.error("Error updating frontmatter", { description: "Could not serialize data to YAML."});
    }
  } else {
    editorStore.activeFileFrontmatter = null;
  }
}

function handleInputChange(key: string, event: Event) {
  if (!frontmatterData.value) return;
  const target = event.target as HTMLInputElement | HTMLTextAreaElement;
  const value = target.value;

  const currentValue = frontmatterData.value[key];
  if (typeof currentValue === 'number') {
    frontmatterData.value[key] = Number(value) || 0;
  } else if (typeof currentValue === 'boolean') {
    frontmatterData.value[key] = value.toLowerCase() === 'true';
  } else {
    frontmatterData.value[key] = value;
  }
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${Number.parseFloat((bytes / 1024 ** i).toFixed(1))} ${units[i]}`;
}

function isMultiline(value: any): boolean {
  return typeof value === 'string' && value.includes('\n');
}

function toggleSection(sectionName: string) {
  openSections.value[sectionName] = !openSections.value[sectionName];
}

</script>

<template>
  <div class="w-64 border-l h-full flex flex-col bg-background">
    <!-- Inspector Header -->
    <div class="p-2 text-sm font-semibold border-b flex items-center gap-2">
      <Info class="h-4 w-4" /> Inspector
    </div>

    <!-- Search -->
    <div class="p-2 relative border-b">
      <Input
        v-model="searchQuery"
        placeholder="Filter properties..."
        class="pl-8 h-8"
      />
      <Search class="h-4 w-4 absolute left-4 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
    </div>

    <!-- Properties Area -->
    <ScrollArea class="flex-1">
      <div class="p-2 text-xs">
        <div v-if="isLoading" class="flex items-center justify-center h-20">
          <span class="text-muted-foreground">Loading...</span>
        </div>

        <div v-else-if="!file" class="flex items-center justify-center h-full text-muted-foreground text-center px-4 py-10">
          Select a file to inspect its properties and frontmatter.
        </div>

        <!-- File Info Section -->
        <Collapsible v-else v-model:open="openSections['File Info']" class="mb-3">
          <CollapsibleTrigger class="flex items-center justify-between w-full text-left font-medium text-xs py-1 hover:bg-muted/50 rounded px-1">
            File Info
            <component :is="openSections['File Info'] ? ChevronDown : ChevronRight" class="h-4 w-4 text-muted-foreground" />
          </CollapsibleTrigger>
          <CollapsibleContent class="pt-1 pl-2 border-l ml-1">
            <div v-if="filteredFileProperties.length > 0" class="space-y-2 mt-1">
              <div v-for="(property, index) in filteredFileProperties" :key="`prop-${index}`">
                <div class="text-muted-foreground uppercase text-[0.65rem] font-semibold">{{ property.name }}</div>
                <div class="text-foreground break-words">{{ property.value }}</div>
              </div>
            </div>
            <div v-else-if="searchQuery" class="text-muted-foreground text-center py-2">
              No properties matching "{{ searchQuery }}"
            </div>
            <div v-else class="text-muted-foreground text-center py-2">
              No file properties loaded.
            </div>
          </CollapsibleContent>
        </Collapsible>

        <!-- Frontmatter Section -->
        <Collapsible v-if="file && supportsFrontmatter" v-model:open="openSections['Frontmatter']" class="mb-3">
          <CollapsibleTrigger class="flex items-center justify-between w-full text-left font-medium text-xs py-1 hover:bg-muted/50 rounded px-1">
            Frontmatter
            <component :is="openSections['Frontmatter'] ? ChevronDown : ChevronRight" class="h-4 w-4 text-muted-foreground" />
          </CollapsibleTrigger>
          <CollapsibleContent class="pt-1 pl-2 border-l ml-1">
            <div v-if="frontmatterParseError" class="mt-1 p-2 bg-destructive/10 text-destructive rounded border border-destructive/30 text-xs flex items-start gap-2">
              <AlertTriangle class="h-4 w-4 flex-shrink-0 mt-0.5" />
              <span>{{ frontmatterParseError }}</span>
            </div>

            <div v-else-if="hasFrontmatter && filteredFrontmatterEntries.length > 0" class="space-y-3 mt-1">
              <div v-for="([key, value]) in filteredFrontmatterEntries" :key="`fm-${key}`">
                <label :for="`fm-input-${key}`" class="text-muted-foreground uppercase text-[0.65rem] font-semibold block mb-0.5">{{ key }}</label>
                <!-- Basic Input/Textarea based on content -->
                <Textarea
                  v-if="isMultiline(value)"
                  :id="`fm-input-${key}`"
                  :value="value"
                  @input="handleInputChange(key, $event)"
                  class="w-full text-xs h-auto min-h-[4rem] resize-y"
                  rows="3"
                />
                <Input
                  v-else
                  :id="`fm-input-${key}`"
                  :value="value"
                  @input="handleInputChange(key, $event)"
                  class="w-full h-7 text-xs"
                  :type="typeof value === 'number' ? 'number' : 'text'"
                />
                <!-- TODO: Add support for boolean checkboxes, arrays, nested objects -->
              </div>
            </div>
            <div v-else-if="searchQuery" class="text-muted-foreground text-center py-2 mt-1">
              No frontmatter keys matching "{{ searchQuery }}"
            </div>
            <div v-else-if="!hasFrontmatter && frontmatterData" class="text-muted-foreground text-center py-2 mt-1">
              No frontmatter defined. Add fields below.
              <!-- TODO: Add "Add Field" button -->
              <Button variant="outline" size="sm" class="mt-2 w-full">Add Field</Button>
            </div>
            <div v-else class="text-muted-foreground text-center py-2 mt-1">
              No frontmatter data loaded.
            </div>
          </CollapsibleContent>
        </Collapsible>

        <div v-else-if="file && !supportsFrontmatter" class="text-muted-foreground text-center py-4 text-xs px-2">
          This file type does not typically use frontmatter.
        </div>

      </div>
    </ScrollArea>
  </div>
</template>
