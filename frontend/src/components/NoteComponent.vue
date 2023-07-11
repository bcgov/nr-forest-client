<template>
  <span class="note">{{ note }}</span>
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
