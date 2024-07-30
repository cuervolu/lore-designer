<script setup lang="ts">
import { ref, watchEffect } from 'vue';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

interface Variable {
  type: string;
  name: string;
}

const props = defineProps<{
  open: boolean
}>();

const emit = defineEmits(['update:open']);

const variables = ref<Variable[]>([]);
const newVariableType = ref<string>('STRING');
const newVariableName = ref<string>('');

const addVariable = () => {
  if (newVariableName.value.trim() !== '') {
    variables.value.push({
      type: newVariableType.value,
      name: newVariableName.value.trim()
    });
    newVariableName.value = '';
  }
};

// Use watchEffect to react to changes in props.open
watchEffect(() => {
  if (props.open) {
    // You could potentially load variables here if needed
    // For now, we'll just ensure the dialog opens
  }
});
</script>

<template>
  <Dialog :open="props.open" @update:open="(value: boolean) => emit('update:open', value)">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Local Variables</DialogTitle>
      </DialogHeader>
      <div class="flex space-x-2">
        <Select v-model="newVariableType">
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="STRING">STRING</SelectItem>
            <SelectItem value="NUMBER">NUMBER</SelectItem>
            <SelectItem value="BOOLEAN">BOOLEAN</SelectItem>
          </SelectContent>
        </Select>
        <Input v-model="newVariableName" placeholder="Variable name" />
        <Button @click="addVariable">+</Button>
      </div>
      <ul>
        <li v-for="variable in variables" :key="variable.name">
          {{ variable.type }} - {{ variable.name }}
        </li>
      </ul>
    </DialogContent>
  </Dialog>
</template>