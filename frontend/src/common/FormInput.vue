<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-input v-model="computedValue" :disabled="disabled" :state="state">
    </b-form-input>
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type { FormFieldTemplateType } from "../core/AppType";

const props = defineProps({
  // form field template props (optional): label, required, tooltip, note, id, errorMsg
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: { id: "form-input" },
  },
  value: { type: [String, Number], required: true },
  disabled: { type: Boolean, default: false },
  state: { type: Boolean, default: null },
});

const emit = defineEmits(["updateValue"]);

const computedValue = computed({
  get() {
    return props.value;
  },
  set(newValue: string | number) {
    emit("updateValue", props.fieldProps.id, newValue);
  },
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormInput",
});
</script>

<style scoped></style>
