<template>
  <div style="margin-bottom: 24px">
    <FormFieldTitle
      v-if="props.fieldProps.label"
      :label="props.fieldProps.label"
      :required="props.fieldProps.required"
      :tooltip="props.fieldProps.tooltip"
    />
    <slot />
    <p v-if="props.fieldProps.note" class="form-field-note">
      <span v-html="fieldProps.note"></span>
    </p>
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
      label: "Hello",
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
</style>
