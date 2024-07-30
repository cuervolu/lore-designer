<script setup lang="ts">
import {Button} from '@/components/ui/button';
import {FileUp, Save, Download} from 'lucide-vue-next';
import NodeToolbar from './NodeToolbar.vue';

const props = defineProps<{
  addNode: (type: string) => void
}>();

const emit = defineEmits(['open-variables']);

const loadFile = () => {
  // La lógica será implementada por el usuario
};

const saveFile = () => {
  // La lógica será implementada por el usuario
};

const exportFile = () => {
  // La lógica será implementada por el usuario
};
</script>

<template>
  <div class="bg-gray-800 p-2 flex items-center justify-between">
    <div class="flex items-center space-x-2">
      <Button @click="loadFile" variant="ghost" size="sm">
        <FileUp class="h-4 w-4 mr-2"/>
        Load
      </Button>
      <Button @click="saveFile" variant="ghost" size="sm">
        <Save class="h-4 w-4 mr-2"/>
        Save
      </Button>
      <Button @click="exportFile" variant="ghost" size="sm">
        <Download class="h-4 w-4 mr-2"/>
        Export
      </Button>
    </div>

    <div class="flex-grow flex justify-center space-x-2">
      <NodeToolbar @add-node="props.addNode"/>
    </div>

    <div class="flex-shrink-0">
      <Button @click="$emit('open-variables')" variant="ghost" size="sm">
        Variables
      </Button>
    </div>
  </div>
</template>

<style scoped>
</style>
