<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{  
  name: string;
  identifier: string;
  email: string;
  provider: string;
}>();

const initials = computed(() =>
  props
  .name
  .split(' ')
  .map((word) => word.trim()[0].toUpperCase())
  .reduce((acc, value, index, array) => {
    switch (index) {
      case 0:
        return value;
      case array.length - 1:
        return acc + value;
      default:
        return acc;
    }
  })
);


</script>

<template>
  <div class="avatar">
    <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64">
      <ellipse cx="32" cy="32" rx="30" ry="30" class="color-blue" />
    </svg>
    <span class="avatar-text">{{ initials }}</span>
  </div>
  <div class="grouping-20">
    <p class="heading-compact-02 light-theme-text-text-primary">{{name}}</p>
    <p class="helper-text-01">{{provider.toUpperCase()}}: {{identifier}}</p>
    <p class="helper-text-01">{{email}}</p>
  </div>
</template>



<style scoped>
.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  width: 64px;
  height: 64px;
}

svg {
  width: 100%;
  height: 100%;
}

.color-blue {
  fill: #0073E6;
}

.avatar-text {
  position: absolute;
  color: white;
  font-size: 20px;
  font-weight: bold;
  text-align: center;
  text-transform: uppercase;
}
</style>
