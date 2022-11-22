<template>
  <div style="margin-top: 24px">
    <FormInput
      :fieldProps="inputFieldProps"
      :value="form.exampleInput"
      :disabled="inputDisabled"
      @updateValue="updateFormValue"
    />
    <FormSelect
      :fieldProps="selectFieldProps"
      :value="form.exampleSelect"
      :options="selectOptions"
      :disabled="selectDisabled"
      @updateValue="updateFormValue"
    />
    <FormCheckboxGroup
      :fieldProps="checkBoxGroupFieldProps"
      :value="form.exampleCheckBoxGroup"
      :options="checkBoxGroupOptions"
      :disabled="checkBoxGroupDisabled"
      @updateValue="updateFormValue"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import FormInput from "./FormInput.vue";
import FormSelect from "./FormSelect.vue";
import FormCheckboxGroup from "./FormCheckboxGroup.vue";
import type {
  FormFieldTemplateType,
  FromSelectOptionType,
  FormCheckBoxGroupOptionType,
} from "../core/AppType";

/* ------- form data ----------- */
const form = ref({
  exampleInput: "",
  exampleSelect: "1",
  exampleCheckBoxGroup: ["2"],
});
const updateFormValue = (id, value) => {
  form.value[id] = value;
  console.log("form data", form.value, value);
};

/* -------- input -------- */
const inputDisabled = ref(false);
const inputFieldProps: FormFieldTemplateType = {
  label: "Example Input",
  id: "exampleInput",
};

/* -------- select -------- */
const selectDisabled = ref(false);
const selectFieldProps: FormFieldTemplateType = {
  label: "Example Select",
  id: "exampleSelect",
  required: true,
  note: "example note text, or error message",
  tooltip: "tooltip placeholder",
};
const selectOptions: Array<FromSelectOptionType> = [
  { value: "1", text: "a" },
  { value: "2", text: "b" },
];

/* -------- check box group -------- */
const checkBoxGroupDisabled = ref(false);
const checkBoxGroupFieldProps: FormFieldTemplateType = {
  label: "Example Check Box Group",
  id: "exampleCheckBoxGroup",
};
const checkBoxGroupOptions: Array<FormCheckBoxGroupOptionType> = [
  { code: "1", text: "Option 1" },
  { code: "2", text: "Option 2" },
  { code: "3", text: "Option 3" },
];
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "CompositionComponentTemplate",
});
</script>

<style scoped></style>
