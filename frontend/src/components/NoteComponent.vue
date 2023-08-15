<template>
  <span class="note body-01">{{ note }}</span>
</template>

<script setup lang="ts">
const props = defineProps<{ note: string }>()
</script>

<style scoped>
.note {
  color: gray;
  font-size: 12px;
  margin-top: 2px;
}
</style>
