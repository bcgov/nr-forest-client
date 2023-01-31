<template>
  <FormFieldTemplate :fieldProps="{ ...fieldProps, label: '' }">
    <b-form-checkbox v-model="computedValue" :disabled="disabled">
      {{ fieldProps.label }}
    </b-form-checkbox>
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type { FormFieldTemplateType } from "../core/FormType";

const props = defineProps({
  // form field template props (optional): label, required, tooltip, note, id, errorMsg
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: { id: "form-checkbox" },
  },
  value: { type: Boolean, required: true },
  disabled: { type: Boolean, default: false },
  state: { type: Boolean, default: null },
});

const emit = defineEmits(["updateValue"]);

const computedValue = computed({
  get() {
    return props.value;
  },
  set(newValue: Boolean) {
    emit("updateValue", newValue, props.fieldProps.modelName);
  },
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormCheckbox",
});
</script>

<style scoped></style>
