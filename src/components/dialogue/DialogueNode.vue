<script setup lang="ts">
import {ref, watch} from 'vue';
import {Handle, Position} from '@vue-flow/core';
import {NodeResizer} from "@vue-flow/node-resizer";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui/select';
import {Textarea} from '@/components/ui/textarea';
import ImageUploader from '@/components/ImageUploader.vue';
import {useCharacterStore} from '@/stores/character.store';
import type {CharacterForNode} from "~/interfaces";

const props = defineProps<{
  data: {
    type: string;
    label: string;
    dialogue?: string;
    character?: any;
    image?: string;
  };
  isConnectable?: boolean;
}>();

const emit = defineEmits(['update:data']);

const characterStore = useCharacterStore();
const characters = ref<CharacterForNode[]>([]);
const localData = ref({...props.data});

onMounted(async () => {
  characters.value = await characterStore.getCharactersName();
});

watch(() => props.data, (newData) => {
  localData.value = {...newData};
}, {deep: true});

const updateData = () => {
  emit('update:data', localData.value);
};
</script>

<template>
  <div class="bg-gray-700 rounded-lg shadow-lg p-4 w-64">
    <NodeResizer :min-width="100" :min-height="30"/>
    <Handle type="target" :position="Position.Left" :isConnectable="isConnectable"/>
    <h3 class="text-lg font-bold mb-2">{{ localData.label }}</h3>

    <template v-if="localData.type === 'showMessage'">
      <Select v-model="localData.character" @update:modelValue="updateData">
        <SelectTrigger class="w-full">
          <SelectValue :placeholder="localData.character?.name || 'Select a character'"/>
        </SelectTrigger>
        <SelectContent>
          <SelectItem v-for="character in characters" :key="character.id" :value="character.name">
            {{ character.name }}
          </SelectItem>
        </SelectContent>
      </Select>
      <Textarea
          v-model="localData.dialogue"
          @input="updateData"
          class="w-full mt-2 p-2 border rounded"
          placeholder="Enter dialogue..."
      />
      <ImageUploader
          :initialImage="localData.image"
          :altText="localData.character?.name"
          @update:image="(info) => { localData.image = info.path; updateData(); }"
      />
    </template>

    <Handle type="source" :position="Position.Right" :isConnectable="isConnectable"/>
  </div>
</template>