<script setup lang="ts">
import { Button } from '@/components/ui/button';
import { FileUp, Save, Download } from 'lucide-vue-next';
import NodeToolbar from './NodeToolbar.vue';

const { t } = useI18n();

const props = defineProps<{
  addNode: (type: string) => void
}>();

const emit = defineEmits(['open-variables']);

const loadFile = () => {
  // Logic to be implemented by the user
};

const saveFile = () => {
  // Logic to be implemented by the user
};

const exportFile = () => {
  // Logic to be implemented by the user
};
</script>

<template>
  <div class="bg-gray-800 p-2 flex items-center justify-between">
    <div class="flex items-center space-x-2">
      <Button @click="loadFile" variant="ghost" size="sm">
        <FileUp class="h-4 w-4 mr-2"/>
        {{ t('branchDialogue.load') }}
      </Button>
      <Button @click="saveFile" variant="ghost" size="sm">
        <Save class="h-4 w-4 mr-2"/>
        {{ t('branchDialogue.save') }}
      </Button>
      <Button @click="exportFile" variant="ghost" size="sm">
        <Download class="h-4 w-4 mr-2"/>
        {{ t('branchDialogue.export') }}
      </Button>
    </div>

    <div class="flex-grow flex justify-center space-x-2">
      <NodeToolbar @add-node="props.addNode"/>
    </div>

    <div class="flex-shrink-0">
      <Button @click="$emit('open-variables')" variant="ghost" size="sm">
        {{ t('branchDialogue.variables') }}
      </Button>
    </div>
  </div>
</template>