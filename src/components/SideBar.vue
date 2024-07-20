<script setup lang="ts">
import {useI18n} from "vue-i18n";
import {
  Book,
  MessageCircle,
  Users,
  CircleHelp,
  Settings2,
  FolderKanban,
  PenTool
} from 'lucide-vue-next'
import {Button} from '@/components/ui/button'
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from '@/components/ui/tooltip'

const {t} = useI18n()

const sidebarItems = [
  {key: 'projects', icon: FolderKanban, route: '/projects'},
  {key: 'dialogue', icon: MessageCircle, route: '/dialogue'},
  {key: 'characters', icon: Users, route: '/characters'},
  {key: 'about', icon: Book, route: '/about'},
]

const bottomItems = [
  {key: 'help', icon: CircleHelp, route: '/help'},
  {key: 'settings', icon: Settings2, route: '/settings'},
]

const localePath = useLocalePath()
</script>

<template>
  <aside class="flex h-full flex-col border-r">
    <div class="border-b p-2">
      <Button variant="outline" size="icon" aria-label="Home" as-child>
        <NuxtLink :to="localePath('/')">
          <PenTool class="size-5"/>
        </NuxtLink>
      </Button>
    </div>
    <nav class="grid gap-1 p-2">
      <TooltipProvider v-for="item in sidebarItems" :key="item.key">
        <Tooltip>
          <TooltipTrigger as-child>
            <NuxtLink :to="localePath(item.route)">
              <Button
                  variant="ghost"
                  size="icon"
                  class="rounded-lg w-full"
                  :class="{ 'bg-muted': item.key === 'projects' }"
                  :aria-label="t(`sidebar.${item.key}`)"
              >
                <component :is="item.icon" class="size-5"/>
              </Button>
            </NuxtLink>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5" align="center">
            {{ t(`sidebar.${item.key}`) }}
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
    </nav>
    <nav class="mt-auto grid gap-1 p-2">
      <TooltipProvider v-for="item in bottomItems" :key="item.key">
        <Tooltip>
          <TooltipTrigger as-child>
            <NuxtLink :to="localePath(item.route)">
              <Button
                  variant="ghost"
                  size="icon"
                  class="mt-auto rounded-lg w-full"
                  :aria-label="t(`sidebar.${item.key}`)"
              >
                <component :is="item.icon" class="size-5"/>
              </Button>
            </NuxtLink>
          </TooltipTrigger>
          <TooltipContent side="right" :side-offset="5">
            {{ t(`sidebar.${item.key}`) }}
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>
    </nav>
  </aside>
</template>