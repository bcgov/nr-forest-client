<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
// Carbon
import '@carbon/web-components/es/components/loading/index.js';
// Composables
import { useEventBus } from '@vueuse/core'

defineProps<{
  isVisible: boolean;
  message: string;
  showLoading: boolean;
}>();

const overlayBus = useEventBus<boolean>('overlay-event');

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    overlayBus.emit({ isVisible: false, message: "", showLoading: false });
  }
};

//when ESC key is pressed, set the isVisible to false
onMounted(() => window.addEventListener('keydown', handleKeydown));

onUnmounted(() => window.removeEventListener('keydown', handleKeydown));
</script>

<template>
  <div v-if="isVisible" class="overlay">
    <div class="content">
      <cds-loading v-if="showLoading"></cds-loading>
      <div class="cds--label message">{{ message }}</div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;  
  padding: 20px;
}

.message {
  color: white;
  margin-top: 0.2rem;
  text-align: center;
}
</style>