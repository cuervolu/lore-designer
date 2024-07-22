<script setup lang="ts">
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList
} from "~/components/ui/breadcrumb";

const {t} = useI18n()
const localePath = useLocalePath()

interface Props {
  // Key to i18n translation
  from: string
  to: string
  oldRoute: string
  newRoute: string
}

const {to, from, oldRoute, newRoute} = defineProps<Props>();
</script>

<template>
  <Breadcrumb>
    <BreadcrumbList>
      <BreadcrumbItem>
        <BreadcrumbLink :href="localePath(oldRoute)">{{ t(from) }}</BreadcrumbLink>
      </BreadcrumbItem>
      <BreadcrumbSeparator />
      <BreadcrumbItem>
        <BreadcrumbLink :href="localePath(newRoute)">{{ t(to) }}</BreadcrumbLink>
      </BreadcrumbItem>
    </BreadcrumbList>
  </Breadcrumb>
</template>

<style scoped>

</style>