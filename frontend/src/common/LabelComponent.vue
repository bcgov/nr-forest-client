<template>
  <div class="form-field-label">
    <label>{{ label }}</label>
    <bi-asterisk v-if="required" 
                 style="font-size: 8px; color: red; margin-top: 2px; margin-right: 6px" />
    <bi-question-circle-fill :id="'form-field-label-' + id" 
                              v-if="tooltip" 
                              style="font-size: 12px; color: #6c757d" />
    <b-popover :target="'form-field-label-' + id" 
                triggers="hover" 
                placement="top" 
               :content="tooltip">
    </b-popover>
  </div>
</template>

<script setup lang="ts">
import BiAsterisk from "~icons/bi/asterisk";
import BiQuestionCircleFill from "~icons/bi/question-circle-fill";

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
