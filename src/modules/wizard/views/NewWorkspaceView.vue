<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';

const router = useRouter();
const workspaceName = ref('');
const workspacePath = ref('');

const goBack = () => {
  router.push({ name: 'workspaces' });
};

const createWorkspace = () => {
  // Here would be the logic to create the workspace
  console.log('Creating workspace:', { name: workspaceName.value, path: workspacePath.value });
  // For now, just go back to workspaces
  goBack();
};
</script>

<template>
  <div class="container mx-auto p-8 max-w-2xl">
    <header class="mb-6">
      <Button variant="ghost" @click="goBack" class="mb-2">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="w-4 h-4 mr-2"><path d="m15 18-6-6 6-6"/></svg>
        Back
      </Button>
      <h1 class="text-2xl font-bold">Create a New Workspace</h1>
      <p class="text-gray-600">Set up a new workspace for your project</p>
    </header>

    <Card>
      <CardHeader>
        <CardTitle>Workspace Details</CardTitle>
      </CardHeader>
      <CardContent>
        <form class="space-y-4">
          <div class="space-y-2">
            <Label for="workspace-name">Workspace Name</Label>
            <Input
              id="workspace-name"
              v-model="workspaceName"
              placeholder="My Awesome Project"
              required
            />
          </div>

          <div class="space-y-2">
            <Label for="workspace-location">Location</Label>
            <div class="flex gap-2">
              <Input
                id="workspace-location"
                v-model="workspacePath"
                placeholder="Select a folder"
                required
              />
              <Button variant="outline" type="button">Browse</Button>
            </div>
          </div>
        </form>
      </CardContent>
      <CardFooter class="flex justify-end gap-2">
        <Button variant="outline" @click="goBack">Cancel</Button>
        <Button @click="createWorkspace" :disabled="!workspaceName || !workspacePath">
          Create Workspace
        </Button>
      </CardFooter>
    </Card>
  </div>
</template>
