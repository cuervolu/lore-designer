<script setup lang="ts">
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { useColorMode } from '@vueuse/core';
import { ref } from 'vue'

const colorMode = useColorMode({
  attribute: 'class',
  storageKey: 'lore-designer-theme',
  modes: {
    light: '',
    dark: 'dark',
  },
});

const isDarkMode = () => colorMode.value === 'dark';
const toggleDarkMode = () => {
  colorMode.value = isDarkMode() ? 'light' : 'dark';
};

const autoSave = ref(true);
</script>

<template>
  <div class="container mx-auto p-8">
    <h1 class="text-2xl font-bold mb-6">Settings</h1>

    <div class="space-y-6">
      <div class="bg-card text-card-foreground p-6 rounded-md shadow-sm border">
        <h2 class="text-xl font-medium mb-4">Appearance</h2>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-medium">Dark Mode</h3>
            <p class="text-sm text-muted-foreground">Toggle dark theme</p>
          </div>
          <Switch :model-value="isDarkMode()" @update:model-value="toggleDarkMode" />
        </div>
      </div>

      <div class="bg-card text-card-foreground p-6 rounded-md shadow-sm border">
        <h2 class="text-xl font-medium mb-4">General</h2>
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="font-medium">Auto Save</h3>
              <p class="text-sm text-muted-foreground">Automatically save changes</p>
            </div>
            <Switch v-model="autoSave" />
          </div>

          <div class="space-y-2">
            <Label for="default-path">Default Project Location</Label>
            <div class="flex gap-2">
              <Input id="default-path" placeholder="Select a default folder for new projects" />
              <Button variant="outline">Browse</Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
