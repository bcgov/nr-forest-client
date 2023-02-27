<template>
  <span class="note">{{ note }}</span>
</template>

<script setup lang="ts">
const props = defineProps({
  note: { 
    type: [String, Number], 
    required: true 
  },
});
</script>
<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "NoteComponent",
});
</script>

<style scoped>
.note {
  color: gray;
  font-size: 12px;
  margin-top: 2px;
}
</style>
