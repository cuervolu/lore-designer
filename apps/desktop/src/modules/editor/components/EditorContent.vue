<script setup lang="ts">
import {User, Home, Upload, type LucideIcon} from 'lucide-vue-next';
import {Button} from '@/components/ui/button';

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
const renderMarkdown = (markdown: string | null) => {
  if (!markdown) return "";
  return markdown
  .split("\n\n")
  .map((p) => {
    if (p.startsWith("## ")) {
      return `<h2 class="text-xl font-semibold mt-6 mb-2">${p.substring(
        3
      )}</h2>`;
    }
    if (p.startsWith("#")) {
      return `<h1 class="text-2xl font-bold mt-8 mb-4">${p.substring(
        2
      )}</h1>`;
    }
    return `<p class="mb-4">${p}</p>`;
  })
  .join("");
};
</script>

<template>
  <div class="h-full overflow-y-auto">
    <div class="min-h-full">
      <!-- Sección de portada -->
      <div class="w-full h-48 bg-muted/30 relative group">
        <img
          :src="'https://placehold.co/1200x300/222/555?text=+'"
          alt="Cover"
          class="w-full h-full object-cover"
        />
        <div class="absolute bottom-0 left-10 transform translate-y-1/2 flex flex-col items-start">
          <div
            class="w-16 h-16 rounded-lg bg-background/95 backdrop-blur-sm border shadow-md flex items-center justify-center p-2"
          >
            <component
              :is="getIconComponent(file.icon)"
              class="h-full w-full text-foreground"
            />
          </div>
          <h1
            class="mt-2 text-xl font-bold text-foreground"
          >
            {{ content.title }}
          </h1>
        </div>

        <Button
          v-if="false"
          variant="outline"
          class="absolute bottom-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <Upload class="h-4 w-4 mr-2"/>
          Upload Cover
        </Button>
      </div>

      <div class="pt-16 px-6 pb-20">
        <div class="max-w-5xl ml-10">
          <div
            v-if="content.canvas"
            class="border rounded-md p-4 min-h-[400px] flex items-center justify-center bg-muted/10"
          >
            <div class="flex items-center gap-8">
              <div class="w-32 h-32 bg-foreground rounded-full"></div>
              <div
                class="w-40 h-40 bg-foreground"
                style="clip-path: polygon(50% 0%, 0% 100%, 100% 100%)"
              ></div>
            </div>
          </div>

          <div
            v-else
            class="prose dark:prose-invert lg:prose-xl max-w-none"
            v-html="renderMarkdown(content.content)"
          />
        </div>
      </div>
    </div>
  </div>
</template>
