<script setup lang="ts">
import { ref, computed } from "vue";
import { ChevronLeft, ChevronRight, Sparkles, Folder, Check } from "lucide-vue-next";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";

const { t } = useI18n();

const props = defineProps<{
  workspaceName: string;
  workspacePath: string;
  openAfterCreation: boolean;
  isCreating: boolean;
  isBrowsing: boolean;
  errorMessage: string;
}>();

const emit = defineEmits<{
  "update:workspaceName": [value: string];
  "update:workspacePath": [value: string];
  "update:openAfterCreation": [value: boolean];
  browse: [];
  create: [];
  cancel: [];
}>();

const currentStep = ref(1);
const selectedTemplate = ref<"empty" | "fantasy" | "scifi">("empty");

const steps = computed(() => [
  {
    id: 1,
    title: t("wizard.new.steps.basicInfo.title"),
    description: t("wizard.new.steps.basicInfo.description"),
  },
  {
    id: 2,
    title: t("wizard.new.steps.template.title"),
    description: t("wizard.new.steps.template.description"),
  },
  {
    id: 3,
    title: t("wizard.new.steps.location.title"),
    description: t("wizard.new.steps.location.description"),
  },
]);

const canProceed = computed(() => {
  if (currentStep.value === 1) return props.workspaceName.length > 0;
  if (currentStep.value === 2) return true;
  if (currentStep.value === 3) return props.workspacePath.length > 0;
  return false;
});

const nextStep = () => {
  if (currentStep.value < 3 && canProceed.value) {
    currentStep.value++;
  }
};

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
};

const handleCreate = () => {
  emit("create");
};
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between mb-8">
      <div v-for="(step, index) in steps" :key="step.id" class="flex items-center">
        <div class="flex flex-col items-center">
          <div
            class="w-10 h-10 rounded-full flex items-center justify-center transition-all duration-300"
            :class="[
              currentStep >= step.id
                ? 'bg-primary text-primary-foreground'
                : 'bg-muted text-muted-foreground',
            ]"
          >
            <Check v-if="currentStep > step.id" class="w-5 h-5" />
            <span v-else>{{ step.id }}</span>
          </div>
          <div class="mt-2 text-center">
            <p class="text-sm font-medium">{{ step.title }}</p>
            <p class="text-xs text-muted-foreground">{{ step.description }}</p>
          </div>
        </div>
        <div
          v-if="index < steps.length - 1"
          class="w-24 h-0.5 mx-4 transition-colors duration-300"
          :class="currentStep > step.id ? 'bg-primary' : 'bg-muted'"
        />
      </div>
    </div>

    <div class="min-h-[300px]">
      <transition name="slide-fade" mode="out-in">
        <div v-if="currentStep === 1" key="step1" class="space-y-4">
          <div class="text-center mb-6">
            <Sparkles class="w-12 h-12 mx-auto mb-4 text-primary" />
            <h2 class="text-2xl font-bold mb-2">{{ $t("wizard.new.steps.basicInfo.heading") }}</h2>
            <p class="text-muted-foreground">{{ $t("wizard.new.steps.basicInfo.subheading") }}</p>
          </div>

          <div class="max-w-md mx-auto">
            <Label for="workspace-name" class="text-base">{{
              $t("wizard.new.steps.basicInfo.nameLabel")
            }}</Label>
            <Input
              id="workspace-name"
              :model-value="workspaceName"
              @update:model-value="emit('update:workspaceName', String($event))"
              :placeholder="$t('wizard.new.steps.basicInfo.namePlaceholder')"
              class="mt-2 text-lg"
              autofocus
            />
            <p class="text-xs text-muted-foreground mt-2">
              {{ $t("wizard.new.steps.basicInfo.nameHint") }}
            </p>
          </div>
        </div>

        <div v-else-if="currentStep === 2" key="step2" class="space-y-4">
          <div class="text-center mb-6">
            <h2 class="text-2xl font-bold mb-2">{{ $t("wizard.new.steps.template.heading") }}</h2>
            <p class="text-muted-foreground">
              {{ $t("wizard.new.steps.template.subheading") }}
            </p>
          </div>

          <div class="grid grid-cols-3 gap-4 max-w-3xl mx-auto">
            <Card
              class="cursor-pointer transition-all duration-300 hover:shadow-lg"
              :class="selectedTemplate === 'empty' ? 'ring-2 ring-primary' : ''"
              @click="selectedTemplate = 'empty'"
            >
              <CardContent class="p-6 text-center">
                <Folder class="w-12 h-12 mx-auto mb-3 text-muted-foreground" />
                <h3 class="font-semibold mb-1">{{ $t("wizard.new.steps.template.empty") }}</h3>
                <p class="text-xs text-muted-foreground">
                  {{ $t("wizard.new.steps.template.emptyDesc") }}
                </p>
              </CardContent>
            </Card>

            <Card
              class="cursor-pointer transition-all duration-300 hover:shadow-lg opacity-50"
              :class="selectedTemplate === 'fantasy' ? 'ring-2 ring-primary' : ''"
            >
              <CardContent class="p-6 text-center">
                <img
                  src="https://placehold.co/100x100/8b5cf6/ffffff/png?text=🐉"
                  class="w-12 h-12 mx-auto mb-3 rounded"
                  alt="Fantasy"
                />
                <h3 class="font-semibold mb-1">{{ $t("wizard.new.steps.template.fantasy") }}</h3>
                <p class="text-xs text-muted-foreground">
                  {{ $t("wizard.new.steps.template.comingSoon") }}
                </p>
              </CardContent>
            </Card>

            <Card
              class="cursor-pointer transition-all duration-300 hover:shadow-lg opacity-50"
              :class="selectedTemplate === 'scifi' ? 'ring-2 ring-primary' : ''"
            >
              <CardContent class="p-6 text-center">
                <img
                  src="https://placehold.co/100x100/3b82f6/ffffff/png?text=🚀"
                  class="w-12 h-12 mx-auto mb-3 rounded"
                  alt="Sci-Fi"
                />
                <h3 class="font-semibold mb-1">{{ $t("wizard.new.steps.template.scifi") }}</h3>
                <p class="text-xs text-muted-foreground">
                  {{ $t("wizard.new.steps.template.comingSoon") }}
                </p>
              </CardContent>
            </Card>
          </div>
        </div>

        <div v-else-if="currentStep === 3" key="step3" class="space-y-4">
          <div class="text-center mb-6">
            <Folder class="w-12 h-12 mx-auto mb-4 text-primary" />
            <h2 class="text-2xl font-bold mb-2">{{ $t("wizard.new.steps.location.heading") }}</h2>
            <p class="text-muted-foreground">{{ $t("wizard.new.steps.location.subheading") }}</p>
          </div>

          <div class="max-w-md mx-auto space-y-4">
            <div>
              <Label for="workspace-location" class="text-base">{{
                $t("wizard.new.steps.location.locationLabel")
              }}</Label>
              <div class="flex gap-2 mt-2">
                <Input
                  id="workspace-location"
                  :model-value="workspacePath"
                  @update:model-value="emit('update:workspacePath', String($event))"
                  readonly
                  :placeholder="$t('wizard.new.steps.location.locationPlaceholder')"
                />
                <Button
                  variant="outline"
                  type="button"
                  @click="emit('browse')"
                  :disabled="isBrowsing"
                >
                  {{
                    isBrowsing
                      ? $t("wizard.new.steps.location.browsing")
                      : $t("wizard.new.steps.location.browse")
                  }}
                </Button>
              </div>
            </div>

            <div class="flex items-center space-x-2">
              <input
                id="open-after-creation"
                type="checkbox"
                :checked="openAfterCreation"
                @change="
                  emit('update:openAfterCreation', ($event.target as HTMLInputElement).checked)
                "
                class="h-4 w-4 rounded border-gray-300"
              />
              <Label for="open-after-creation">{{
                $t("wizard.new.steps.location.openAfterCreation")
              }}</Label>
            </div>

            <div
              v-if="errorMessage"
              class="text-destructive text-sm p-3 bg-destructive/10 rounded-md"
            >
              {{ errorMessage }}
            </div>
          </div>
        </div>
      </transition>
    </div>

    <div class="flex justify-between pt-6 border-t">
      <Button
        variant="outline"
        @click="currentStep === 1 ? emit('cancel') : prevStep()"
        :disabled="isCreating"
      >
        <ChevronLeft class="w-4 h-4 mr-2" />
        {{ currentStep === 1 ? $t("wizard.new.actions.cancel") : $t("wizard.new.actions.back") }}
      </Button>

      <Button v-if="currentStep < 3" @click="nextStep" :disabled="!canProceed">
        {{ $t("wizard.new.actions.continue") }}
        <ChevronRight class="w-4 h-4 ml-2" />
      </Button>

      <Button v-else @click="handleCreate" :disabled="!canProceed || isCreating">
        {{ isCreating ? $t("wizard.new.actions.creating") : $t("wizard.new.actions.create") }}
        <Check class="w-4 h-4 ml-2" />
      </Button>
    </div>
  </div>
</template>

<style scoped>
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>
