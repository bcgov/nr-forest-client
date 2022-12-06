<template>
  <div class="form-field-title">
    <div v-html="label" style="margin-right: 6px"></div>
    <bi-asterisk
      v-if="required"
      style="font-size: 8px; color: red; margin-top: 2px; margin-right: 6px"
    />
    <!-- <bi-question-circle-fill
      v-if="tooltip"
      v-b-popover.hover.top="tooltip"
      style="margin-left: 4px; font-size: 12px; margin-top: 4px; color: #6c757d"
    /> -->
    <bi-question-circle-fill
      :id="'form-field-title-' + id"
      v-if="tooltip"
      style="font-size: 12px; color: #6c757d"
    />
    <b-popover
      :target="'form-field-title-' + id"
      triggers="hover"
      placement="top"
      :content="tooltip"
    >
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
    default: "form-field-title",
  },
});
</script>
<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormFieldTitle",
});
</script>

<style scoped>
.form-field-title {
  margin-bottom: 8px;
  display: flex;
}
</style>
