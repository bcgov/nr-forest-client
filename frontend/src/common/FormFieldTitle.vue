<template>
  <div style="margin-bottom: 8px; display: flex">
    <div v-html="props.label"></div>
    <b-icon-asterisk
      v-if="props.required"
      style="
        font-size: 8px;
        color: red;
        margin-top: 4px;
        margin-right: 2px;
        margin-left: 4px;
      "
    />
    <b-icon-question-circle-fill
      v-if="props.tooltip"
      v-b-popover.hover.top="props.tooltip"
      style="margin-left: 4px; font-size: 12px; margin-top: 4px; color: #6c757d"
    />
  </div>
</template>

<script setup lang="ts">
// composition api
const props = defineProps({
  label: String,
  required: {
    type: Boolean,
    default: false,
  },
  tooltip: {
    type: String,
    default: null,
  },
});
</script>
<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormFieldTitle",
});
</script>

<style scoped></style>
