<script setup lang="ts">
import {
  Book,
  Bot,
  Code2,
  LifeBuoy,
  Settings2,
  SquareTerminal,
  SquareUser,
  PenTool
} from 'lucide-vue-next'

import {Button} from '@/components/ui/button'
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from '@/components/ui/tooltip'
</script>

<template>
  <aside class="flex h-full flex-col border-r">
    <div class="border-b p-2">
      <Button variant="outline" size="icon" aria-label="Home" as-child>
        <NuxtLink to="/">
          <PenTool class="size-5" />
        </NuxtLink>
      </Button>
    </div>
    <nav class="grid gap-1 p-2">
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Button
                variant="ghost"
                size="icon"
                class="rounded-lg bg-muted"
                aria-label="Playground"
            >
              <SquareTerminal class="size-5"/>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            Playground
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Button
                variant="ghost"
                size="icon"
                class="rounded-lg"
                aria-label="Models"
            >
              <Bot class="size-5"/>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            Models
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Button
                variant="ghost"
                size="icon"
                class="rounded-lg"
                aria-label="API"
            >
              <Code2 class="size-5"/>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            API
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Button
                variant="ghost"
                size="icon"
                class="rounded-lg"
                aria-label="Documentation"
            >
              <Book class="size-5"/>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            Documentation
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Button
                variant="ghost"
                size="icon"
                class="rounded-lg"
                aria-label="Settings"
            >
              <Settings2 class="size-5"/>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            Settings
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
    </nav>
    <nav class="mt-auto grid gap-1 p-2">
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Button
                variant="ghost"
                size="icon"
                class="mt-auto rounded-lg"
                aria-label="Help"
            >
              <LifeBuoy class="size-5"/>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            Help
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Button
                variant="ghost"
                size="icon"
                class="mt-auto rounded-lg"
                aria-label="Account"
            >
              <SquareUser class="size-5"/>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            Account
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
    </nav>
  </aside>
</template>

<style scoped>

</style>