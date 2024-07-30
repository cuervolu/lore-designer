<script setup lang="ts">
import { ref, markRaw } from 'vue';
import { VueFlow, useVueFlow, type NodeRemoveChange, type EdgeRemoveChange } from '@vue-flow/core';
import type { Node, Edge, NodeTypesObject, NodeChange, EdgeChange } from '@vue-flow/core';
import { Background } from "@vue-flow/background";
import { MiniMap } from "@vue-flow/minimap";
import { Controls } from "@vue-flow/controls";
import DialogueNode from "./DialogueNode.vue";
import TopToolbar from "./TopToolbar.vue";
import VariablesDialog from "./VariablesDialog.vue";
import KeyBindings from "./KeyBindings.vue";
import RemoveDialog from "~/components/dialogue/RemoveDialog.vue";

import '@vue-flow/core/dist/style.css';
import '@vue-flow/core/dist/theme-default.css';
import '@vue-flow/controls/dist/style.css';
import '@vue-flow/minimap/dist/style.css';
import '@vue-flow/node-resizer/dist/style.css';

const nodes = ref<Node[]>([]);
const edges = ref<Edge[]>([]);
const showVariablesDialog = ref(false);
const showRemoveDialog = ref(false);
const elementToRemove = ref<NodeRemoveChange | EdgeRemoveChange | null>(null);

const nodeTypes = markRaw({
  dialogueNode: DialogueNode,
}) as NodeTypesObject;

const { project, addNodes, addEdges, onNodesChange, onEdgesChange, applyNodeChanges, applyEdgeChanges } = useVueFlow();

const addNode = (type: string) => {
  const newNode: Node = {
    id: (nodes.value.length + 1).toString(),
    type: 'dialogueNode',
    position: project({ x: Math.random() * 500, y: Math.random() * 500 }),
    data: { type, label: type }
  };
  addNodes([newNode]);
};

const onConnect = (params: any) => {
  addEdges([params]);
};

const nodeColor = (node: Node) => {
  switch (node.type) {
    case 'dialogueNode':
      return '#6366f1';
    default:
      return '#ff0072';
  }
};

onNodesChange((changes: NodeChange[]) => {
  const removeChange = changes.find(change => change.type === 'remove') as NodeRemoveChange | undefined;
  if (removeChange) {
    elementToRemove.value = removeChange;
    showRemoveDialog.value = true;
  } else {
    applyNodeChanges(changes);
  }
});

onEdgesChange((changes: EdgeChange[]) => {
  const removeChange = changes.find(change => change.type === 'remove') as EdgeRemoveChange | undefined;
  if (removeChange) {
    elementToRemove.value = removeChange;
    showRemoveDialog.value = true;
  } else {
    applyEdgeChanges(changes);
  }
});

const handleConfirmRemove = () => {
  if (elementToRemove.value) {
    if ('id' in elementToRemove.value) {
      applyNodeChanges([elementToRemove.value as NodeRemoveChange]);
    } else if ('id' in elementToRemove.value) {
      applyEdgeChanges([elementToRemove.value as EdgeRemoveChange]);
    }
  }
  elementToRemove.value = null;
  showRemoveDialog.value = false;
};

const handleCancelRemove = () => {
  elementToRemove.value = null;
  showRemoveDialog.value = false;
};
</script>

<template>
  <div class="w-full h-full flex flex-col bg-gray-900 text-white">
    <TopToolbar @open-variables="showVariablesDialog = true" :add-node="addNode"/>

    <div class="flex-grow relative">
      <VueFlow
          v-model:nodes="nodes"
          v-model:edges="edges"
          @connect="onConnect"
          :nodeTypes="nodeTypes"
          :default-viewport="{ zoom: 1.5 }"
          :min-zoom="0.2"
          :max-zoom="4"
          :fitViewOnInit="true"
          :apply-default="false"
          class="w-full h-full"
      >
        <Background color="#aaa" :gap="16"/>
        <MiniMap :nodeColor="nodeColor" class="bg-gray-800 vue-flow__minimap" pannable zoomable/>
        <Controls position="top-left"/>
      </VueFlow>
    </div>

    <VariablesDialog v-model:open="showVariablesDialog"/>
    <KeyBindings/>
    <RemoveDialog
        v-model:open="showRemoveDialog"
        :element-to-remove="elementToRemove"
        @confirm="handleConfirmRemove"
        @cancel="handleCancelRemove"
    />
  </div>
</template>

<style scoped>
.vue-flow__minimap {
  background-color: #1f2937;
}
</style>