<script setup lang="ts">
import {ref, watch, computed} from 'vue';
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
    character?: string;
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

const selectedCharacter = computed(() => {
  return characters.value.find(char => char.name === localData.value.character);
});

watch(selectedCharacter, (newCharacter) => {
  if (newCharacter && newCharacter.imagePath) {
    localData.value.image = newCharacter.imagePath;
    updateData();
  }
});

const handleCharacterChange = (characterName: string) => {
  localData.value.character = characterName;
  const character = characters.value.find(char => char.name === characterName);
  if (character && character.imagePath) {
    localData.value.image = character.imagePath;
  }
  updateData();
};
</script>

<template>
  <div class="node-container">
    <NodeResizer :min-width="100" :min-height="30"/>
    <div class="node-content bg-gray-700 rounded-lg shadow-lg p-4">
      <Handle type="target" :position="Position.Left" :isConnectable="isConnectable"/>
      <h3 class="text-lg font-bold mb-2">{{ localData.label }}</h3>
      <template v-if="localData.type === 'showMessage'">
        <Select v-model="localData.character" @update:modelValue="handleCharacterChange">
          <SelectTrigger class="w-full">
            <SelectValue :placeholder="localData.character || 'Select a character'"/>
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
            :altText="localData.character"
            :characterId="selectedCharacter?.id"
            @update:image="(info) => { localData.image = info.path; updateData(); }"
        />
      </template>
      <Handle type="source" :position="Position.Right" :isConnectable="isConnectable"/>
    </div>
  </div>
</template>

<style scoped>
.node-container {
  width: 100%;
  height: 100%;
  min-width: 100px;
  min-height: 30px;
}

.node-content {
  width: 100%;
  height: 100%;
  overflow: auto;
}
</style>