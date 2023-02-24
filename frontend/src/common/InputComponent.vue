<template>
  <b-form-input v-model="value">
  </b-form-input>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps(['value']);
const emit = defineEmits(["updateValue"]);

const value = computed({
  get() {
        return props.value
  },
  set(value) {
    emit('updateValue', value)
  }
});
</script>