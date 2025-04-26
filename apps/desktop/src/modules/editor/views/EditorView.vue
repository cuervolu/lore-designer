<script setup lang="ts">
import { ref } from 'vue';
import EditorLayout from '@editor/layouts/EditorLayout.vue';

const activeTab = ref('protagonist');
const workspaceName = ref('My Fantasy Novel');
const files = ref([
  { id: 'protagonist', name: 'Protagonist', extension: 'md', path: 'Characters/Protagonist.md', icon: 'User' },
  { id: 'kingdom', name: 'Kingdom', extension: 'md', path: 'Lore/Kingdom.md', icon: 'Home' },
  { id: 'canvas', name: 'Canvas', extension: 'canvas', path: 'Story/Canvas.canvas', icon: 'PenTool' }
]);


</script>

<template>
  <EditorLayout
    :active-tab="activeTab"
    :files="files"
    :workspace-name="workspaceName"
  />
</template>
