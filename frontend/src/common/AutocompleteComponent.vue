<template>
  <b-form-input :id="id" 
                v-model="computedValue">
  </b-form-input>

  <!-- <ul id="example-1">
    <li v-for="item in searchData" :key="item.code">
      {{ item.code }}
    </li>
  </ul> -->

  {{ id }}
  <!-- {{ computedValue }} -->
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';


const props = defineProps({
  value: String,
  id: String,
  searchData: Object
});

const emit = defineEmits(["updateValue"]);
const computedValue = computed({
  get() {
        return props.value
  },
  set(newValue) {
    emit('updateValue', newValue)
  }
});

</script>