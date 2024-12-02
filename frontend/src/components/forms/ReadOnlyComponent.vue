<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string;
    labelClass?: string;
  }>(),
  {
    labelClass: "label-01",
  },
);
</script>

<template>
  <div class="grouping-11">
    <div class="title-group-01">
      <span :class="labelClass">{{ label }}</span>
      <slot name="icon"></slot>
    </div>
    <slot></slot>
  </div>
</template>

<style scoped>
.title-group-01 {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
}
</style>
