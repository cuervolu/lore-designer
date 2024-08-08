<script setup lang="ts">
import { useRoute } from 'vue-router'
import ProjectDashboard from "~/components/projects/ProjectDashboard.vue";

const route = useRoute()
const projectId = parseInt(route.params.id as string)
</script>

<template>
  <ProjectDashboard :projectId="projectId" />
</template>

<style scoped>
</style>