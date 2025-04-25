<script setup lang="ts">
interface Props {
  customClass?: string;
}

defineProps<Props>();
</script>

<template>
  <svg
    :class="customClass"
    viewBox="0 0 36 36"
    xmlns="http://www.w3.org/2000/svg"
    fill="currentColor"
    aria-hidden="true"
    role="img"
  >
    <path
      d="M28 8H14a2 2 0 0 0-2 2v2h2v-2h14v10h-2v2h2a2 2 0 0 0 2-2V10a2 2 0 0 0-2-2"
    />
    <path
      d="M22 14H8a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V16a2 2 0 0 0-2-2M8 26V16h14v10Z"
    />
  </svg>
</template>
