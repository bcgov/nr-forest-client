<template>
  <div style="margin-bottom: 24px">
    <FormFieldTitle
      v-if="fieldProps.label"
      :label="fieldProps.label"
      :required="fieldProps.required"
      :tooltip="fieldProps.tooltip"
      :id="fieldProps.id"
    />
    <slot />
    <div v-if="fieldProps.note" class="form-field-note">
      <span v-html="fieldProps.note"></span>
    </div>
    <div v-if="errorType" class="form-field-err-msg">
      <span v-html="computedErrorMsg[errorType]"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
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
    required: true,
  },
  errorType: String,
});

const computedErrorMsg = computed(() => {
  let errorMsgs = {};
  if (props.fieldProps.errorMsg) errorMsgs = props.fieldProps.errorMsg;
  if (props.fieldProps.required) {
    errorMsgs = {
      missingRequired: "This field is required.",
      ...errorMsgs,
    };
  }
  return errorMsgs;
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
  color: #de4b50;
  font-size: 12px;
  margin-top: 2px;
}
</style>
