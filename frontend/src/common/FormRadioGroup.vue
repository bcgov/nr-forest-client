<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-form-radio
      v-for="(option, index) in options"
      :key="index"
      v-model="computedValue"
      :value="option.code"
      :disabled="disabled"
    >
      <!-- :state="state" -->
      <!-- https://github.com/cdmoro/bootstrap-vue-3/issues/819 -->
      {{ option.text }}
    </b-form-radio>
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import type {
  FormFieldTemplateType,
  FormRadioGroupOptionType,
} from "../core/FormType";

const props = defineProps({
  // form field template props (optional): label, required, tooltip, note, id, errorMsg
  fieldProps: {
    type: Object as PropType<FormFieldTemplateType>,
    default: { id: "form-radio-group" },
  },
  value: { type: Array as PropType<Array<string>>, required: true },
  options: {
    type: Array as PropType<Array<FormRadioGroupOptionType>> | undefined,
    required: true,
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
  name: "FormRadioGroup",
});
</script>

<style scoped></style>
