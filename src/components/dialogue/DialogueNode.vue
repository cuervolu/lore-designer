<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { Handle, Position } from '@vue-flow/core';
import { NodeResizer } from "@vue-flow/node-resizer";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { useCharacterStore } from '@/stores/character.store';
import type { CharacterForNode } from "~/interfaces";
import noPhoto from '~/assets/img/no_photo.webp';

const { t } = useI18n();

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
    <NodeResizer :min-width="300" :min-height="100" />
    <div class="node-content bg-gray-700 rounded-lg shadow-lg p-4">
      <Handle
          type="target"
          :position="Position.Left"
          :isConnectable="isConnectable"
          class="w-4 h-4 -left-2 bg-blue-500 border-2 border-white"
      />
      <div class="flex items-center mb-2">
        <div class="character-image mr-3">
          <img
              :src="localData.image || noPhoto"
              :alt="localData.character || t('branchDialogue.selectCharacter')"
              class="w-12 h-12 rounded-full object-cover border-2 border-primary"
          />
        </div>
        <div class="flex-grow">
          <h3 class="text-lg font-bold">{{ localData.label }}</h3>
          <Select v-model="localData.character" @update:modelValue="handleCharacterChange">
            <SelectTrigger class="w-full">
              <SelectValue :placeholder="localData.character || t('branchDialogue.selectCharacter')" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="character in characters" :key="character.id" :value="character.name">
                {{ character.name }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>
      <template v-if="localData.type === 'showMessage'">
        <Textarea
            v-model="localData.dialogue"
            @input="updateData"
            class="w-full mt-2 p-2 border rounded"
            :placeholder="t('branchDialogue.showMessage.placeholder')"
        />
      </template>
      <Handle
          type="source"
          :position="Position.Right"
          :isConnectable="isConnectable"
          class="w-4 h-4 -right-2 bg-green-500 border-2 border-white"
      />
    </div>
  </div>
</template>

<style scoped>
.node-container {
  width: 100%;
  height: 100%;
  min-width: 300px;
  min-height: 100px;
}
.node-content {
  width: 100%;
  height: 100%;
  overflow: auto;
}
.character-image img {
  width: 48px;
  height: 48px;
  object-fit: cover;
}
.vue-flow__handle {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #3b82f6;
  border: 2px solid #ffffff;
  transition: transform 0.3s ease;
}
.vue-flow__handle:hover {
  transform: scale(1.2);
}
.vue-flow__handle-left {
  left: -8px;
}
.vue-flow__handle-right {
  right: -8px;
}
</style>