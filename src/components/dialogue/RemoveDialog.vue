<script setup lang="ts">
import { computed } from 'vue';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import type { NodeRemoveChange, EdgeRemoveChange } from '@vue-flow/core';

const props = defineProps<{
  open: boolean;
  elementToRemove: NodeRemoveChange | EdgeRemoveChange | null;
}>();

const emit = defineEmits(['update:open', 'confirm', 'cancel']);

const elementType = computed(() => {
  if (!props.elementToRemove) return '';
  return 'nodeId' in props.elementToRemove ? 'node' : 'edge';
});

const handleConfirm = () => {
  emit('confirm');
};

const handleCancel = () => {
  emit('cancel');
};
</script>

<template>
  <Dialog :open="open" @update:open="(value: boolean) => emit('update:open', value)">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Remove Element</DialogTitle>
        <DialogDescription>
          Are you sure you want to remove this {{ elementType }}?
        </DialogDescription>
      </DialogHeader>
      <DialogFooter>
        <Button @click="handleCancel">Cancel</Button>
        <Button @click="handleConfirm" variant="destructive">Remove</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>