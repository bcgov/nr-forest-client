<template>
  <b-form-input :id="id" 
                v-model="computedValue"
                :list="datalistId"
                autocomplete="off">
  </b-form-input>

  <!-- TODO: Value should be an object. It displays the name, but on the back, this should contain:
  the code, the name, the id, and the address (for BC registry) -->
  <datalist :id="datalistId">
    <option v-for="(item, index) in searchData" 
            :key="index"       
            :value="item.name">
      {{item.name}}
    </option>      
  </datalist>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps({
  value: { 
    type: String, 
    required: true
  },
  id: { 
    type: String, 
    required: true 
  },
  datalistId: { 
    type: String, 
    required: true 
  },
  searchData: {
    type: Object,
    required: true
  }
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

<script lang="ts">
  import { defineComponent } from "vue";
  export default defineComponent({
    name: "AutocompleteComponent",
  });
</script>

<style scoped>
datalist {
  position: absolute;
  max-height: 20em;
  border: 0 none;
  overflow-x: hidden;
  overflow-y: auto;
}

datalist option {
  font-size: 0.8em;
  padding: 0.3em 1em;
  background-color: #ccc;
  cursor: pointer;
}

datalist option:hover, datalist option:focus {
  color: #fff;
  background-color: #036;
  outline: 0 none;
}
</style>