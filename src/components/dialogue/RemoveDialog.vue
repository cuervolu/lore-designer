<script setup lang="ts">
import { computed } from 'vue';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import type { NodeRemoveChange, EdgeRemoveChange } from '@vue-flow/core';

const { t } = useI18n();

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
        <DialogTitle>{{ t('branchDialogue.removeElementTitle') }}</DialogTitle>
        <DialogDescription>
          {{ t('branchDialogue.removeElementDescription', { elementType: elementType }) }}
        </DialogDescription>
      </DialogHeader>
      <DialogFooter>
        <Button @click="handleCancel">{{ t('branchDialogue.cancel') }}</Button>
        <Button @click="handleConfirm" variant="destructive">{{ t('branchDialogue.remove') }}</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>