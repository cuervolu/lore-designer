<script setup lang="ts">
import {toRef} from 'vue';
import {
  AlertTriangle,
  ChevronDown,
  ChevronRight,
  Info,
  Search,
  Save,
  Undo2,
} from 'lucide-vue-next';
import {Input} from '@/components/ui/input';
import {Textarea} from '@/components/ui/textarea';
import {Checkbox} from '@/components/ui/checkbox';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {ScrollArea} from '@/components/ui/scroll-area';
import {useEditorStore} from '@editor/stores/editor.store';
import type {EditorFile} from '@editor/types/editor.types.ts';
import {Button} from '@/components/ui/button';
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible';
import {useInspector} from '@editor/composables/useInspector';


const props = defineProps<{
  file?: EditorFile | null;
}>();


const editorStore = useEditorStore();


const {
  searchQuery,
  isLoading,
  frontmatterData,
  frontmatterParseError,
  isFrontmatterDirty,
  isFrontmatterSupportedType,
  openSections,
  filteredFileProperties,
  filteredFrontmatterEntries,
  handleInputChange,
  applyFrontmatterChanges,
  discardFrontmatterChanges,
  toggleSection
} = useInspector(toRef(props, 'file'), editorStore);

const frontmatterSchema: Record<
  string,
  { type: 'text' | 'textarea' | 'select' | 'checkbox' | 'number'; options?: string[] }
> = {
  status: {type: 'select', options: ['active', 'inactive', 'deceased', 'unknown']},
};

function getFieldType(key: string, value: unknown): string {
  if (frontmatterSchema[key]) {
    return frontmatterSchema[key].type;
  }
  if (typeof value === 'boolean') return 'checkbox';
  if (typeof value === 'number') return 'number';
  if (typeof value === 'string' && value.includes('\n')) return 'textarea';
  return 'text';
}

function getSelectOptions(key: string): string[] {
  return frontmatterSchema[key]?.options ?? [];
}

function onInputValueChange(key: string, event: Event) {
  const target = event.target as HTMLInputElement | HTMLTextAreaElement;
  handleInputChange(key, target.value);
}

function onNumberValueChange(key: string, event: Event) {
  const target = event.target as HTMLInputElement;
  handleInputChange(key, Number(target.value) || 0);
}

function onCheckboxChange(key: string, checked: boolean | 'indeterminate') {
  const booleanValue = checked === true;
  handleInputChange(key, booleanValue);
}

function onSelectChange(key: string, value: string | null | undefined) {
  const valueToSave = value ?? '';
  handleInputChange(key, valueToSave);
}

</script>

<template>
  <div class="w-64 border-l h-full flex flex-col bg-background">
    <div class="p-2 text-sm font-semibold border-b flex items-center justify-between gap-2">
      <div class="flex items-center gap-2">
        <Info class="h-4 w-4"/>
        Inspector
      </div>
      <!-- Dirty State Indicator & Actions -->
      <div v-if="isFrontmatterDirty" class="flex items-center gap-1">
        <Button
          variant="ghost"
          @click="applyFrontmatterChanges"
        >
          <Save class="h-4 w-4 text-blue-500"/>
        </Button>
        <Button
          variant="ghost"
          @click="discardFrontmatterChanges"
        >
          <Undo2 class="h-4 w-4 text-destructive"/>
        </Button>
      </div>
    </div>


    <!-- Search -->
    <div class="p-2 relative border-b">
      <Input
        v-model="searchQuery"
        placeholder="Filter properties..."
        class="pl-8 h-8 text-xs"
      />
      <Search
        class="h-4 w-4 absolute left-4 top-1/2 transform -translate-y-1/2 text-muted-foreground"
      />
    </div>

    <!-- Properties Area -->
    <ScrollArea class="flex-1">
      <div class="p-2 text-xs">
        <div v-if="isLoading" class="flex items-center justify-center h-20">
          <span class="text-muted-foreground">Loading...</span>
        </div>

        <div
          v-else-if="!file"
          class="flex items-center justify-center h-full text-muted-foreground text-center px-4 py-10"
        >
          Select a file to inspect its properties and frontmatter.
        </div>

        <!-- File Info Section -->
        <Collapsible
          v-else
          v-model:open="openSections['File Info']"
          class="mb-3"
          :default-open="true"
        >
          <CollapsibleTrigger
            class="flex items-center justify-between w-full text-left font-medium text-xs py-1 hover:bg-muted/50 rounded px-1"
            @click="toggleSection('File Info')"
          >
            File Info
            <component
              :is="openSections['File Info'] ? ChevronDown : ChevronRight"
              class="h-4 w-4 text-muted-foreground"
            />
          </CollapsibleTrigger>
          <CollapsibleContent class="pt-1 pl-2 border-l ml-1">
            <div
              v-if="filteredFileProperties.length > 0"
              class="space-y-2 mt-1"
            >
              <div
                v-for="(property, index) in filteredFileProperties"
                :key="`prop-${index}`"
              >
                <div
                  class="text-muted-foreground uppercase text-[0.65rem] font-semibold"
                >
                  {{ property.name }}
                </div>
                <div class="text-foreground break-words">
                  {{ property.value }}
                </div>
              </div>
            </div>
            <div
              v-else-if="searchQuery"
              class="text-muted-foreground text-center py-2"
            >
              No properties matching "{{ searchQuery }}"
            </div>
            <div v-else class="text-muted-foreground text-center py-2">
              No file properties loaded.
            </div>
          </CollapsibleContent>
        </Collapsible>

        <!-- Frontmatter Section -->
        <Collapsible
          v-if="file && isFrontmatterSupportedType"
          v-model:open="openSections['Frontmatter']"
          class="mb-3"
          :default-open="true"
        >
          <CollapsibleTrigger
            class="flex items-center justify-between w-full text-left font-medium text-xs py-1 hover:bg-muted/50 rounded px-1"
            @click="toggleSection('Frontmatter')"
          >
            Frontmatter
            <component
              :is="openSections['Frontmatter'] ? ChevronDown : ChevronRight"
              class="h-4 w-4 text-muted-foreground"
            />
          </CollapsibleTrigger>
          <CollapsibleContent class="pt-1 pl-2 border-l ml-1">
            <!-- Error Display -->
            <div
              v-if="frontmatterParseError"
              class="mt-1 p-2 bg-destructive/10 text-destructive rounded border border-destructive/30 text-xs flex items-start gap-2"
            >
              <AlertTriangle class="h-4 w-4 flex-shrink-0 mt-0.5"/>
              <span>{{ frontmatterParseError }}</span>
            </div>

            <!-- Data Display -->
            <div
              v-else-if="
                frontmatterData &&
                typeof frontmatterData === 'object' &&
                filteredFrontmatterEntries.length > 0
              "
              class="space-y-3 mt-1"
            >
              <!-- Iterate over entries -->
              <div
                v-for="([key, value]) in filteredFrontmatterEntries"
                :key="`fm-${key}`"
              >
                <label
                  :for="`fm-input-${key}`"
                  class="text-muted-foreground uppercase text-[0.65rem] font-semibold block mb-0.5"
                >{{ key }}</label
                >

                <!-- Determine Input Type -->
                <template v-if="getFieldType(key, value) === 'select'">
                  <Select
                    :value="String(value ?? '')"
                  @update:model-value="(val) => onSelectChange(key, val)"
                  >
                  <SelectTrigger class="h-7 text-xs">
                    <SelectValue placeholder="Select status..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem
                      v-for="opt in getSelectOptions(key)"
                      :key="opt"
                      :value="opt"
                    >
                      {{ opt }}
                    </SelectItem>
                  </SelectContent>
                  </Select>
                </template>


                <template v-else-if="getFieldType(key, value) === 'checkbox'">
                  <div class="flex items-center h-7">
                    <Checkbox
                      :id="`fm-input-${key}`"
                      :checked="!!value"
                    @update:checked="(checked: boolean | 'indeterminate') => onCheckboxChange(key, checked)"
                    class="mr-2"
                    />
                    <label
                      :for="`fm-input-${key}`"
                      class="text-xs text-muted-foreground"
                    >(Boolean)</label>
                  </div>
                </template>


                <template v-else-if="getFieldType(key, value) === 'textarea'">
                  <Textarea
                    :id="`fm-input-${key}`"
                    :value="value ?? ''"
                    @input="onInputValueChange(key, $event)"
                    class="w-full text-xs h-auto min-h-[4rem] resize-y font-mono"
                    rows="3"
                  />
                </template>

                <template v-else-if="getFieldType(key, value) === 'number'">
                  <Input
                    :id="`fm-input-${key}`"
                    :value="value ?? 0"
                    @input="onNumberValueChange(key, $event)"
                    class="w-full h-7 text-xs"
                    type="number"
                  />
                </template>

                <template v-else> <!-- Default: text input -->
                  <Input
                    :id="`fm-input-${key}`"
                    :value="value ?? ''"
                    @input="onInputValueChange(key, $event)"
                    class="w-full h-7 text-xs"
                    type="text"
                  />
                </template>
              </div>
            </div>

            <!-- No Matching Search Results -->
            <div
              v-else-if="
                searchQuery && frontmatterData && typeof frontmatterData === 'object'
              "
              class="text-muted-foreground text-center py-2 mt-1"
            >
              No frontmatter keys matching "{{ searchQuery }}"
            </div>

            <!-- No Frontmatter Defined -->
            <div
              v-else-if="
                frontmatterData &&
                typeof frontmatterData === 'object' &&
                Object.keys(frontmatterData).length === 0
              "
              class="text-muted-foreground text-center py-2 mt-1"
            >
              No frontmatter defined.
              <!-- TODO: Add "Add Field" button -->
              <Button variant="outline" size="sm" class="mt-2 w-full" disabled>Add Field</Button>
            </div>

            <!-- Catch-all -->
            <div
              v-else-if="!frontmatterParseError"
              class="text-muted-foreground text-center py-2 mt-1"
            >
              {{ isLoading ? 'Loading...' : 'No frontmatter data.' }}
            </div>
          </CollapsibleContent>
        </Collapsible>

        <!-- Message for unsupported types -->
        <div
          v-else-if="file && !isFrontmatterSupportedType"
          class="text-muted-foreground text-center py-4 text-xs px-2"
        >
          This file type does not use frontmatter.
        </div>
      </div>
    </ScrollArea>
  </div>
</template>
