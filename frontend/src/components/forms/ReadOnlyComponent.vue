<script setup lang="ts">
defineProps<{ label: string }>()
</script>
<template>
  <div class="grouping-11">
    <div class="title-group-01">
      <span class="label-01">{{ label }}</span>   
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