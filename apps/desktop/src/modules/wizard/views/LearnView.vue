<script setup lang="ts">
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
</script>

<template>
  <div class="container mx-auto p-8">
    <h1 class="text-2xl font-bold mb-6">Learn</h1>
    <p class="text-gray-600 mb-8">
      Learn how to use Lore Designer to streamline your game development or story creation process.
    </p>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <Card>
        <CardHeader>
          <CardTitle>Getting Started</CardTitle>
          <CardDescription>Learn the basics of Lore Designer</CardDescription>
        </CardHeader>
        <CardContent>
          <p>
            This guide will walk you through creating your first workspace, setting up characters,
            and managing your project.
          </p>
        </CardContent>
        <CardFooter>
          <Button variant="outline" class="w-full">View Guide</Button>
        </CardFooter>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Character Creation</CardTitle>
          <CardDescription>Create compelling characters</CardDescription>
        </CardHeader>
        <CardContent>
          <p>
            Learn how to build complex characters with backstories, traits, and relationships that
            bring your stories to life.
          </p>
        </CardContent>
        <CardFooter>
          <Button variant="outline" class="w-full">View Guide</Button>
        </CardFooter>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Dialogue Systems</CardTitle>
          <CardDescription>Craft engaging conversations</CardDescription>
        </CardHeader>
        <CardContent>
          <p>
            Discover how to create branching dialogues that respond to player choices and character
            relationships.
          </p>
        </CardContent>
        <CardFooter>
          <Button variant="outline" class="w-full">View Guide</Button>
        </CardFooter>
      </Card>
    </div>

    <div class="mt-8 text-center">
      <p class="mb-4 text-gray-600">
        Have more questions? Check out our documentation or join our community.
      </p>
      <div class="flex justify-center gap-4">
        <Button>Documentation</Button>
        <Button variant="outline">Community</Button>
      </div>
    </div>
  </div>
</template>
