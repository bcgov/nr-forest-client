<template>
  <div v-show="index == selectedContent">
    <slot :validateStep="validateStep" :active="index == selectedContent" />
  </div>
</template>
<script setup lang="ts">
import { inject } from "vue";

const props = defineProps<{
  title: string;
  index: number;
  valid: boolean;
  wizard: any;
}>();

const emit = defineEmits<{
  (e: "update:valid", value: { index: number; valid: boolean }): void;
}>();

const selectedContent = inject<number>("currentIndex");

const validateStep = (valid: boolean) => {
  emit("update:valid", { index: props.index, valid });
  props.wizard.processValidity({ index: props.index, valid });
};

emit("update:valid", { index: props.index, valid: false });
</script>
