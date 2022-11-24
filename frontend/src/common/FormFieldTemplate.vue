<template>
  <div style="margin-bottom: 24px">
    <FormFieldTitle
      v-if="props.fieldProps.label"
      :label="props.fieldProps.label"
      :required="props.fieldProps.required"
      :tooltip="props.fieldProps.tooltip"
      :id="props.fieldProps.id"
    />
    <slot />
    <div v-if="props.fieldProps.note" class="form-field-note">
      <span v-html="fieldProps.note"></span>
    </div>
    <div v-if="props.fieldProps.errorMsg" class="form-field-err-msg">
      <span v-html="fieldProps.errorMsg"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import FormFieldTitle from "./FormFieldTitle.vue";
import type { FormFieldTemplateType } from "../core/AppType";

// composition api
const props = defineProps({
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: {
      label: null,
    },
  },
});
</script>
<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormFieldTemplate",
});
</script>

<style scoped>
.form-field-note {
  color: gray;
  font-size: 12px;
  margin-top: 2px;
}
.form-field-err-msg {
  color: #DE4B50;
  font-size: 12px;
  margin-top: 2px;
}
</style>
