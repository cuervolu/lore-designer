<script setup lang="ts">
import {ref, watch, onMounted} from 'vue';
import {Handle, Position} from '@vue-flow/core';
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
import type {Character} from '~/interfaces';

const props = defineProps<{
  data: {
    dialogue: string;
    character: Character | null;
    image: string | null;
  };
  isConnectable: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:dialogue', value: string): void;
  (e: 'update:character', value: Character | null): void;
  (e: 'update:image', value: string): void;
}>();

const characterStore = useCharacterStore();
const characters = ref<Character[]>([]);
const localDialogue = ref(props.data.dialogue);
const selectedCharacterName = ref<string>(props.data.character?.name || '');

onMounted(async () => {
  const result = await characterStore.getCharacters();
  characters.value = result.characters;
});

watch(() => props.data, (newData) => {
  localDialogue.value = newData.dialogue;
  selectedCharacterName.value = newData.character?.name || '';
}, {deep: true});

const updateDialogue = () => {
  emit('update:dialogue', localDialogue.value);
};

const updateCharacter = (characterName: string) => {
  const character = characters.value.find(c => c.name === characterName) || null;
  emit('update:character', character);
};

const updateImage = (imageInfo: { path: string }) => {
  emit('update:image', imageInfo.path);
};
</script>

<template>
  <div class="bg-muted/60 rounded-lg shadow-lg p-4 w-96">
    <Handle
        type="target"
        :position="Position.Top"
        :isConnectable="isConnectable"
        class="w-3 h-3 bg-primary"
    />
    <Select v-model="selectedCharacterName" @update:modelValue="updateCharacter">
      <SelectTrigger class="w-full">
        <SelectValue :placeholder="selectedCharacterName || 'Select a character'"/>
      </SelectTrigger>
      <SelectContent>
        <SelectItem v-for="character in characters" :key="character.id" :value="character.name">
          {{ character.name }}
        </SelectItem>
      </SelectContent>
    </Select>
    <Textarea
        v-model="localDialogue"
        @input="updateDialogue"
        class="w-full mt-2 p-2 border rounded"
        placeholder="Enter dialogue..."
    />
    <div class="mt-2">
      <ImageUploader
          :initialImage="data.image"
          :altText="selectedCharacterName"
          @update:image="updateImage"
      />
    </div>
    <Handle
        type="source"
        :position="Position.Bottom"
        :isConnectable="isConnectable"
        class="w-3 h-3 bg-primary"
    />
  </div>
</template>