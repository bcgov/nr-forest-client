<template>
  <div class="form-field-label">
    <label>{{ label }}</label>
    <div v-if="tooltip">
      &nbsp;
      <bi-question-circle-fill
        :id="'popover' + id"
        style="font-size: 12px; color: #6c757d; vertical-align: top"
      />
      <b-popover :target="'popover' + id" placement="right">
        <span v-html="tooltip" />
      </b-popover>
    </div>
  </div>
</template>

<script setup lang="ts">
import BiQuestionCircleFill from "~icons/carbon/help-filled";

const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  required: {
    type: Boolean,
    default: false,
  },
  tooltip: {
    type: String,
    default: null,
  },
  id: {
    type: String,
    default: "form-field-label",
  },
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "LabelComponent",
});
</script>

<style scoped>
.form-field-label {
  margin-bottom: 8px;
  display: flex;
}
</style>
