<script setup lang="ts">
const props = defineProps<{ label: string }>()
</script>
<template>
  <div class="grouping-11">
    <span class="label-01">{{ label }}</span>
    <slot></slot>
  </div>
</template>
