<script setup lang="ts">
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
  SidebarTrigger,
} from '@/components/ui/sidebar';

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

// Import icons
import {
  BookOpen,
  Users,
  Map,
  MessageSquare,
  FolderOpen,
  FileText,
  Timer,
  Settings,
  Menu,
  ChevronDown,
  User,
} from 'lucide-vue-next';

// Menu items for the main navigation
const mainNavItems = [
  {
    title: 'Characters',
    icon: Users,
    url: '#',
  },
  {
    title: 'Locations',
    icon: Map,
    url: '#',
  },
  {
    title: 'Dialogues',
    icon: MessageSquare,
    url: '#',
  },
  {
    title: 'Assets',
    icon: FolderOpen,
    url: '#',
  },
  {
    title: 'GDD',
    icon: FileText,
    url: '#',
  },
  {
    title: 'Timeline',
    icon: Timer,
    url: '#',
  },
];

// Projects list (example data)
const projects = [
  {
    title: 'My Fantasy Novel',
    url: '#',
  },
  {
    title: 'RPG Adventure Game',
    url: '#',
  },
  {
    title: 'Sci-Fi Short Story',
    url: '#',
  },
];
</script>

<template>
  <Sidebar collapsible="icon">
    <!-- Sidebar Header with Workspace Selector -->
    <SidebarHeader>
      <SidebarTrigger class="lg:hidden mb-1">
        <Menu class="h-4 w-4" />
      </SidebarTrigger>
      <SidebarMenu>
        <SidebarMenuItem>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <SidebarMenuButton>
                <BookOpen class="w-5 h-5 mr-2" />
                <span>Current Workspace</span>
                <ChevronDown class="ml-auto h-4 w-4" />
              </SidebarMenuButton>
            </DropdownMenuTrigger>
            <DropdownMenuContent class="w-[--reka-popper-anchor-width]">
              <DropdownMenuItem>
                <span>New Workspace</span>
              </DropdownMenuItem>
              <DropdownMenuItem>
                <span>Open Workspace</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarHeader>

    <!-- Sidebar Content -->
    <SidebarContent>
      <!-- Navigation Group -->
      <SidebarGroup>
        <SidebarGroupLabel>Navigation</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem v-for="item in mainNavItems" :key="item.title">
              <SidebarMenuButton asChild>
                <a :href="item.url">
                  <component :is="item.icon" class="h-5 w-5" />
                  <span>{{ item.title }}</span>
                </a>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <!-- Projects Group -->
      <SidebarGroup>
        <SidebarGroupLabel>Projects</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem v-for="project in projects" :key="project.title">
              <SidebarMenuButton asChild>
                <a :href="project.url">
                  <FileText class="h-5 w-5" />
                  <span>{{ project.title }}</span>
                </a>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <!-- Sidebar Footer -->
    <SidebarFooter>
      <SidebarMenu>
        <SidebarMenuItem>
          <SidebarMenuButton asChild>
            <a href="#">
              <Settings class="h-5 w-5" />
              <span>Settings</span>
            </a>
          </SidebarMenuButton>
        </SidebarMenuItem>

        <SidebarMenuItem>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <SidebarMenuButton>
                <User class="h-5 w-5" />
                <span>Profile</span>
                <ChevronDown class="ml-auto h-4 w-4" />
              </SidebarMenuButton>
            </DropdownMenuTrigger>
            <DropdownMenuContent
              side="top"
              class="w-[--reka-popper-anchor-width]"
            >
              <DropdownMenuItem>
                <span>Preferences</span>
              </DropdownMenuItem>
              <DropdownMenuItem>
                <span>About</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      </SidebarMenu>
    </SidebarFooter>
  </Sidebar>
</template>
