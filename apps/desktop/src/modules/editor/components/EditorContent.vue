<script setup lang="ts">
import {User, Home, Image, Upload, type LucideIcon} from 'lucide-vue-next';
import { Button } from '@/components/ui/button';

const props = defineProps<{
  file: {
    id: string;
    name: string;
    extension: string;
    path: string;
    icon: string;
  };
}>();

// Mock content based on file type
const getDummyContent = () => {
  if (props.file.id === 'protagonist') {
    return {
      title: 'Protagonist',
      content: `
# Título 1

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas vulputate sem erat, et suscipit metus convallis non. Duis bibendum gravida nunc id mattis. Suspendisse vestibulum mauris vitae aliquet tristique. Nulla a mi magna. Ut viverra tristique nulla, sit amet ultrices massa venenatis ac. Donec viverra leo eget eros interdum pulvinar. Vivamus faucibus euismod nibh, eu feugiat urna pharetra interdum. Etiam vitae lacus sed orci dapibus tincidunt sit amet nec risus. Fusce vitae hendrerit ligula.

Cras dignissim sem vitae ex suscipit varius. Aliquam fringilla metus nisi, eget hendrerit nisi consequat quis. Donec erat odio, congue ut elit eu, blandit suscipit nulla. Donec fringilla hendrerit porta. Praesent gravida vel ex sit amet tristique. Mauris malesuada magna dolor, ut feugiat neque posuere id. Phasellus auctor porttitor sem, vel accumsan dui efficitur eu. Sed et orci ut augue lobortis maximus. Sed vitae viverra diam. Ut vehicula auctor erat, vitae vehicula dolor malesuada vel.
Donec malesuada magna quis vestibulum gravida. Nam sodales massa vitae ultrices vestibulum.

## Título 2

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas vulputate sem erat, et suscipit metus convallis non. Duis bibendum gravida nunc id mattis. Suspendisse vestibulum mauris vitae aliquet tristique. Nulla a mi magna. Ut viverra tristique nulla, sit amet ultrices massa venenatis ac. Donec viverra leo eget eros interdum pulvinar. Vivamus faucibus euismod nibh, eu feugiat urna pharetra interdum. Etiam vitae lacus sed orci dapibus tincidunt sit amet nec risus. Fusce vitae hendrerit ligula.

Cras dignissim sem vitae ex suscipit varius. Aliquam fringilla metus nisi, eget hendrerit nisi consequat quis. Donec erat odio, congue ut elit eu, blandit suscipit nulla. Donec fringilla hendrerit porta. Praesent gravida vel ex sit amet tristique. Mauris malesuada magna dolor, ut feugiat neque posuere id. Phasellus auctor porttitor sem, vel accumsan dui efficitur eu. Sed et orci ut augue lobortis maximus. Sed vitae viverra diam. Ut vehicula auctor erat, vitae vehicula dolor malesuada vel.
Donec malesuada magna quis vestibulum gravida. Nam sodales massa vitae ultrices vestibulum.`
    };
  }

  if (props.file.id === 'kingdom') {
    return {
      title: 'Kingdom',
      content: `
Lorem ipsum dolor sit amet, consectetur adipiscing elit. In nec quam sit amet elit auctor auctor a non ante. Donec pellentesque pretium nisi porttitor ultrices. Pellentesque ac ipsum et dui ullamcorper accumsan sit amet tempus nunc. Nunc feugiat ligula vel risus viverra congue. Nullam vitae interdum justo. Maecenas pretium id ligula eu condimentum. Fusce ut pulvinar enim, sed porta nisi. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Etiam mollis justo a massa elementum sagittis. Sed auctor nisl vel ipsum sollicitudin imperdiet.

Phasellus vestibulum lectus vel varius porttitor. Vivamus nec metus in risus ullamcorper porta. Ut et neque eget lorem laoreet malesuada eu ac velit. Proin id sapien nisl, vel porta justo nisl vel lectus pulvinar, eget dapibus lectus lobortis. Fusce ac urna id nisl tempus elementum id eget arcu. Nulla vehicula urna ut est imperdiet, ac rhoncus velit lobortis. Proin auctor sodales turpis, vel pretium ligula interdum non. Quisque et libero nec nisi dignissim dictum. Aenean id dui viverra justo vehicula volutpat consectetur a sem. Curabitur tincidunt orci massa, id pulvinar tellus ultrices a. Morbi mollis vestibulum nisl, at pellentesque nunc. Curabitur et elit ante.

Curabitur efficitur consectetur porta. Nulla quis erat sed tortor efficitur vulputate a vitae urna. Praesent risus eu, molestie id dolor vitae, iaculis commodo dolor. Etiam maximus a felis eget fermentum. Integer molestie velit eu turpis sodales commodo. Mauris et mi magna. Proin et nisi non enim rhoncus consectetur sit amet sit amet neque. In mollis tincidunt ex ac malesuada. Duis porttitor nunc non risus consequat, sit amet commodo ante accumsan.

Nullam suscipit rutrum purus, eget pretium eros consectetur id. Aliquam nec leo ultrices, congue dolor vel, accumsan dui. Duis et nisi magna. Praesent hendrerit quam quis tortor rutrum,
a semper nibh viverra. Ut sollicitudin semper eros et mollis. Donec nec sollicitudin nunc, et tristique velit. Morbi eget felis faucibus, porta leo sit amet, accumsan elit. Maecenas vel leo ut eros
hendrerit elementum. Nunc porta risus vel congue laoreet. Ut auctor lectus vel metus finibus facilisis. Curabitur nec dui venenatis, mollis mauris eu, consectetur lectus. Class aptent taciti sociosqu ad
litora torquent per conubia nostra, per inceptos himenaeos.

Aenean fermentum metus eget tincidunt hendrerit. Nullam imperdiet est mi, non cursus elit convallis at. Etiam vestibulum, nibh eget varius vehicula, arcu sem mollis quam, sed
condimentum sem nunc ut sapien. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.
Vivamus ut pharetra magna. Pellentesque rhoncus urna sit
amet ex tincidunt, a suscipit est mollis. Phasellus eleifend mi in vestibulum pulvinar.`
    };
  }

  if (props.file.id === 'canvas') {
    return {
      title: 'Canvas',
      canvas: true
    };
  }

  return {
    title: props.file.name,
    content: `Content for ${props.file.name}`
  };
};

const content = getDummyContent();

// Icon mapping
const iconMap: Record<string, LucideIcon> = {
  'User': User,
  'Home': Home
};

// Get icon component based on the icon name
const getIconComponent = (iconName: string) => {
  return iconMap[iconName] || User;
};
</script>

<template>
  <div class="p-4">
    <!-- Cover Image Area -->
    <div class="w-full h-40 bg-muted/30 rounded-md flex flex-col items-center justify-center mb-6">
      <p class="font-medium text-xl text-center">PORTADA</p>
      <Button v-if="false" variant="outline" class="mt-3">
        <Upload class="h-4 w-4 mr-2" />
        Upload Cover Image
      </Button>
    </div>

    <!-- Main Content -->
    <div class="max-w-3xl mx-auto">
      <!-- File icon and title -->
      <div class="flex items-center mb-6">
        <div class="w-12 h-12 rounded-full bg-muted/50 flex items-center justify-center">
          <component :is="getIconComponent(file.icon)" class="h-6 w-6" />
        </div>
        <h1 class="text-2xl font-bold ml-3">{{ content.title }}</h1>
      </div>

      <!-- Canvas content -->
      <div v-if="content.canvas" class="border rounded-md p-4 h-96 flex items-center justify-center">
        <div class="flex items-center gap-8">
          <div class="w-32 h-32 bg-black rounded-full"></div>
          <div class="w-40 h-40 bg-black" style="clip-path: polygon(50% 0%, 0% 100%, 100% 100%);"></div>
        </div>
      </div>

      <!-- Text content -->
      <div v-else class="prose max-w-none">
        <p v-for="(paragraph, index) in content.content.split('\n\n')" :key="index" class="mb-4">
          {{ paragraph }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.prose h1 {
  font-size: 1.5rem;
  font-weight: bold;
  margin-top: 1.5rem;
  margin-bottom: 1rem;
}

.prose h2 {
  font-size: 1.25rem;
  font-weight: bold;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
}
</style>
