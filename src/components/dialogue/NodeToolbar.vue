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
import {Button} from '@/components/ui/button';

const {t} = useI18n();

const toolbarOptions = [
  {
    icon: MessageSquare,
    label: 'branchDialogue.showMessage.label',
    type: 'showMessage',
    color: 'bg-red-500',
    hoverColor: 'hover:bg-red-600'
  },
  {
    icon: Play,
    label: 'branchDialogue.execute',
    type: 'execute',
    color: 'bg-red-500',
    hoverColor: 'hover:bg-red-600'
  },
  {
    icon: Pause,
    label: 'branchDialogue.wait',
    type: 'wait',
    color: 'bg-red-500',
    hoverColor: 'hover:bg-red-600'
  },
  {
    icon: GitBranch,
    label: 'branchDialogue.conditionBranch',
    type: 'conditionBranch',
    color: 'bg-green-500',
    hoverColor: 'hover:bg-green-600'
  },
  {
    icon: Dices,
    label: 'branchDialogue.randomBranch',
    type: 'randomBranch',
    color: 'bg-green-500',
    hoverColor: 'hover:bg-green-600'
  },
  {
    icon: Percent,
    label: 'branchDialogue.chanceBranch',
    type: 'chanceBranch',
    color: 'bg-green-500',
    hoverColor: 'hover:bg-green-600'
  },
  {
    icon: RotateCcw,
    label: 'branchDialogue.repeat',
    type: 'repeat',
    color: 'bg-green-500',
    hoverColor: 'hover:bg-green-600'
  },
  {
    icon: Variable,
    label: 'branchDialogue.setLocalVariable',
    type: 'setLocalVariable',
    color: 'bg-blue-400',
    hoverColor: 'hover:bg-blue-500'
  },
  {
    icon: MessageCircle,
    label: 'branchDialogue.comment',
    type: 'comment',
    color: 'bg-purple-500',
    hoverColor: 'hover:bg-purple-600'
  },
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
            {{ t(option.label) }}
          </p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  </div>
</template>
