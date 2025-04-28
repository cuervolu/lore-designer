<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getVersion } from '@tauri-apps/api/app';
import { error as logError } from '@tauri-apps/plugin-log';
import { Github } from 'lucide-vue-next';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  'update:isOpen': [value: boolean];
}>();

const appVersion = ref('0.1.0');
const appLogo = new URL('@/assets/logo.webp', import.meta.url).href;
const openSource = ref(false);

// Get app version on component mount
onMounted(async () => {
  try {
    appVersion.value = await getVersion();
  } catch (err) {
    await logError(`Failed to get app version: ${err}`);
  }
});

// Close the modal
const closeModal = () => {
  emit('update:isOpen', false);
  // Reset the license toggle state when closing
  openSource.value = false;
};

// Toggle license view
const toggleLicenseView = () => {
  openSource.value = !openSource.value;
};
</script>

<template>
  <Dialog :open="isOpen" @update:open="emit('update:isOpen', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle class="text-xl">About Lore Designer</DialogTitle>
        <DialogDescription>
          A streamlined game narrative and worldbuilding tool for indie developers
        </DialogDescription>
      </DialogHeader>

      <div class="py-4">
        <!-- App Logo and Version -->
        <div class="flex flex-col items-center mb-6">
          <img :src="appLogo" alt="Lore Designer Logo" class="w-24 h-24 mb-2" />
          <h2 class="text-lg font-semibold">Lore Designer</h2>
          <p class="text-sm text-muted-foreground">Version {{ appVersion }}</p>
        </div>

        <!-- Toggle between About and License -->
        <div v-if="!openSource" class="space-y-4">
          <!-- Credits -->
          <div>
            <h3 class="font-medium mb-2">Credits</h3>
            <ul class="text-sm space-y-1">
              <li><span class="text-muted-foreground">Design & Development:</span> Ángel Cuervo</li>
              <li><span class="text-muted-foreground">Application Icon:</span> iriata18</li>
            </ul>
          </div>

          <!-- GitHub -->
          <div>
            <h3 class="font-medium mb-2">Open Source</h3>
            <p class="text-sm">
              Lore Designer is open source software licensed under AGPL-3.0.
              <Button
                variant="link"
                class="px-0 h-auto"
                @click="toggleLicenseView"
              >
                View License
              </Button>
            </p>

            <div class="mt-2">
              <a
                href="https://github.com/cuervolu/lore-designer"
                target="_blank"
                rel="noopener noreferrer"
                class="inline-flex items-center gap-2 text-sm hover:text-primary"
              >
                <Github class="w-4 h-4" />
                <span>Visit GitHub Repository</span>
              </a>
            </div>
          </div>
        </div>

        <!-- License Text -->
        <div v-else>
          <div class="flex items-center justify-between mb-2">
            <h3 class="font-medium">License (AGPL-3.0)</h3>
            <Button
              variant="ghost"
              size="sm"
              @click="toggleLicenseView"
            >
              Back to About
            </Button>
          </div>

          <ScrollArea class="h-40 w-full rounded-md border p-4">
            <div class="text-xs font-mono">
              <p>GNU AFFERO GENERAL PUBLIC LICENSE</p>
              <p>Version 3, 19 November 2007</p>
              <br />
              <p>Copyright (C) 2007 Free Software Foundation, Inc. </p>
              <p>Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.</p>
              <br />
              <p>Preamble</p>
              <br />
              <p>The GNU Affero General Public License is a free, copyleft license for software and other kinds of works, specifically designed to ensure cooperation with the community in the case of network server software.</p>
              <br />
              <p>The licenses for most software and other practical works are designed to take away your freedom to share and change the works. By contrast, our General Public Licenses are intended to guarantee your freedom to share and change all versions of a program--to make sure it remains free software for all its users.</p>
              <br />
              <p>When we speak of free software, we are referring to freedom, not price. Our General Public Licenses are designed to make sure that you have the freedom to distribute copies of free software (and charge for them if you wish), that you receive source code or can get it if you want it, that you can change the software or use pieces of it in new free programs, and that you know you can do these things.</p>
              <br />
              <p>For the complete license text, please visit: https://www.gnu.org/licenses/agpl-3.0.html</p>
            </div>
          </ScrollArea>
        </div>
      </div>

      <DialogFooter>
        <Button @click="closeModal">Close</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
