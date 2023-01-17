<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-checkbox
      v-for="(option, index) in options"
      :key="index"
      v-model="computedValue"
      :value="option.code"
      :disabled="disabled"
    >
      <!-- :state="state" -->
      <!-- https://github.com/cdmoro/bootstrap-vue-3/issues/819 -->
      {{ option.text }}
    </b-form-checkbox>
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type {
  FormFieldTemplateType,
  FormCheckBoxGroupOptionType,
} from "../core/FormType";

const props = defineProps({
  // form field template props (optional): label, required, tooltip, note, id, errorMsg
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: { id: "form-checkboxgroup" },
  },
  value: { type: Array as PropType<Array<string>>, required: true },
  options: {
    type: Array as PropType<Array<FormCheckBoxGroupOptionType>> | undefined,
    required: true,
    default: [{ code: 1, text: "Option 1" }],
  },
  disabled: { type: Boolean, default: false },
  state: { type: Boolean, default: null },
});

const emit = defineEmits(["updateValue"]);

const computedValue = computed({
  get() {
    return props.value;
  },
  set(newValue: Array<string>) {
    emit("updateValue", props.fieldProps.id, newValue);
  },
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormCheckboxGroup",
});
</script>

<style scoped></style>
