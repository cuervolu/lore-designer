<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import WorkspaceItem from '@wizard/components/WorkspaceItem.vue';

const router = useRouter();

// Mock data for workspaces
const workspaces = ref([
  { id: 1, name: 'workspace', path: 'D:\\projects\\workspace' },
  { id: 2, name: 'workspace', path: 'D:\\projects\\workspace' },
  { id: 3, name: 'workspace', path: 'D:\\projects\\workspace' },
  { id: 4, name: 'workspace', path: 'D:\\projects\\workspace' },
  { id: 5, name: 'workspace', path: 'D:\\projects\\workspace' },
]);

const searchQuery = ref('');
</script>

<template>
  <div class="container mx-auto p-8">
    <header class="flex justify-between items-center mb-8">
      <h1 class="text-2xl font-bold">Welcome to Lore Designer</h1>
      <div class="flex gap-2">
        <Button variant="outline">
          Import
        </Button>
        <Button variant="outline">
          Open
        </Button>
        <Button @click="router.push({ name: 'new-workspace' })">
          New Workspace
        </Button>
      </div>
    </header>

    <div class="mb-6">
      <Input
        v-model="searchQuery"
        placeholder="Search workspaces"
        type="search"
        class="max-w-md"
      />
    </div>

    <div class="grid gap-4 mt-4">
      <WorkspaceItem
        v-for="workspace in workspaces"
        :key="workspace.id"
        :workspace="workspace"
      />
    </div>
  </div>
</template>
