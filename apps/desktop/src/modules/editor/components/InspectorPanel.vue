<script setup lang="ts">
import {computed, ref} from 'vue';
import { Search } from 'lucide-vue-next';
import { Input } from '@/components/ui/input';

const props = defineProps<{
  file?: {
    id: string;
    name: string;
    extension: string;
    path: string;
    icon: string;
  } | null;
}>();

const searchQuery = ref('');

// Mock properties based on file
const getFileProperties = () => {
  if (!props.file) return [];

  // Common properties
  const properties = [
    { name: 'Filename', value: `${props.file.name}.${props.file.extension}` },
    { name: 'Created At', value: '17/04/2025 10:22' },
    { name: 'Updated At', value: '18/04/2025 16:22' },
    { name: 'Word Count', value: '1,548' },
  ];

  // Add file-specific properties
  if (props.file.id === 'protagonist') {
    return [
      ...properties,
      { name: 'Character Type', value: 'Protagonist' },
      { name: 'Age', value: '28' },
      { name: 'Occupation', value: 'Warrior' },
      { name: 'Home', value: 'Kingdom Valley' },
      { name: 'Status', value: 'Alive' },
    ];
  }

  if (props.file.id === 'kingdom') {
    return [
      ...properties,
      { name: 'Type', value: 'Location' },
      { name: 'Region', value: 'Central Lands' },
      { name: 'Population', value: '25,000' },
      { name: 'Ruler', value: 'King Edmund' },
      { name: 'Climate', value: 'Temperate' },
    ];
  }

  if (props.file.id === 'canvas') {
    return [
      ...properties,
      { name: 'Type', value: 'Canvas' },
      { name: 'Elements', value: '2' },
      { name: 'Connections', value: '1' },
      { name: 'Version', value: '1.0' },
    ];
  }

  return properties;
};

const properties = getFileProperties();

// Filter properties based on search
const filteredProperties = computed(() => {
  if (!searchQuery.value) return properties;

  const query = searchQuery.value.toLowerCase();
  return properties.filter(prop =>
    prop.name.toLowerCase().includes(query) ||
    prop.value.toLowerCase().includes(query)
  );
});
</script>

<template>
  <div class="w-64 border-l h-full flex flex-col">
    <!-- Inspector Header -->
    <div class="p-2 text-lg font-semibold border-b">
      Inspector
    </div>

    <!-- Search -->
    <div class="p-2 relative">
      <Input
        v-model="searchQuery"
        placeholder="Filter properties..."
        class="pl-8"
      />
      <Search class="h-4 w-4 absolute left-4 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
    </div>

    <!-- Properties -->
    <div class="flex-1 overflow-auto p-2">
      <div v-if="file">
        <div v-for="(property, index) in filteredProperties" :key="index" class="mb-3">
          <div class="text-xs text-muted-foreground">{{ property.name }}</div>
          <div>{{ property.value }}</div>
        </div>
      </div>
      <div v-else class="flex items-center justify-center h-full text-muted-foreground">
        Select a file to view properties
      </div>
    </div>
  </div>
</template>
