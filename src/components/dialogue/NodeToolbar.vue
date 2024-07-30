<script setup lang="ts">
import {
  MessageSquare,
  Play,
  GitBranch,
  Percent,
  RotateCcw,
  Variable,
  MessageCircle,
  Pause,
  Dices
} from 'lucide-vue-next';
import { Button } from '@/components/ui/button';

const toolbarOptions = [
  { icon: MessageSquare, label: 'Show Message', type: 'showMessage', color: 'bg-red-500', hoverColor: 'hover:bg-red-600' },
  { icon: Play, label: 'Execute', type: 'execute', color: 'bg-red-500', hoverColor: 'hover:bg-red-600' },
  { icon: Pause, label: 'Wait', type: 'wait', color: 'bg-red-500', hoverColor: 'hover:bg-red-600' },
  { icon: GitBranch, label: 'Condition Branch', type: 'conditionBranch', color: 'bg-green-500', hoverColor: 'hover:bg-green-600' },
  { icon: Dices, label: 'Random Branch', type: 'randomBranch', color: 'bg-green-500', hoverColor: 'hover:bg-green-600' },
  { icon: Percent, label: 'Chance Branch', type: 'chanceBranch', color: 'bg-green-500', hoverColor: 'hover:bg-green-600' },
  { icon: RotateCcw, label: 'Repeat', type: 'repeat', color: 'bg-green-500', hoverColor: 'hover:bg-green-600' },
  { icon: Variable, label: 'Set Local Variable', type: 'setLocalVariable', color: 'bg-blue-400', hoverColor: 'hover:bg-blue-500' },
  { icon: MessageCircle, label: 'Comment', type: 'comment', color: 'bg-purple-500', hoverColor: 'hover:bg-purple-600' },
];

const emit = defineEmits(['add-node']);
</script>

<template>
  <div class="flex space-x-1 bg-gray-700 p-1 rounded-md">
    <TooltipProvider>
      <Tooltip v-for="option in toolbarOptions"
               :key="option.label">
        <TooltipTrigger as-child>
          <Button
              @click="emit('add-node', option.type)"
              variant="ghost"
              size="icon"
              class="h-8 w-8 p-1.5"
              :class="[option.color, option.hoverColor]"
          >
            <component :is="option.icon" class="h-full"/>
          </Button>
        </TooltipTrigger>
        <TooltipContent>
          <p>
            {{ option.label }}
          </p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  </div>
</template>
