<template>
  <div style="margin: 24px">
    <CollapseCard
      title="Section One"
      defaultOpen
      nextId="sectwo"
      nextText="Open the next section"
    >
      <FormInput
        :fieldProps="inputFieldProps"
        :value="form.exampleInput"
        :disabled="inputDisabled"
        :state="inputState"
        @updateValue="updateFormValue"
      />
      <FormSelect
        :fieldProps="selectFieldProps"
        :value="form.exampleSelect"
        :options="selectOptions"
        :disabled="selectDisabled"
        @updateValue="updateFormValue"
      />
      <!-- :state="selectState" -->
      <!-- https://github.com/cdmoro/bootstrap-vue-3/issues/819 -->
    </CollapseCard>
    <CollapseCard title="Section Two" id="sectwo">
      <FormCheckboxGroup
        :fieldProps="checkBoxGroupFieldProps"
        :value="form.exampleCheckBoxGroup"
        :options="checkBoxGroupOptions"
        :disabled="checkBoxGroupDisabled"
        @updateValue="updateFormValue"
      />
      <!-- :state="checkBoxGroupState" -->
      <!-- https://github.com/cdmoro/bootstrap-vue-3/issues/819 -->
      <FormRadioGroup
        :fieldProps="radioGroupFieldProps"
        :value="form.exampleRadioGroup"
        :options="radioGroupOptions"
        :disabled="radioGroupDisabled"
        @updateValue="updateFormValue"
      />
      <!-- :state="radioGroupState" -->
      <!-- https://github.com/cdmoro/bootstrap-vue-3/issues/819 -->
    </CollapseCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import FormInput from "../FormInput.vue";
import FormSelect from "../FormSelect.vue";
import FormCheckboxGroup from "../FormCheckboxGroup.vue";
import FormRadioGroup from "../FormRadioGroup.vue";
import CollapseCard from "../CollapseCard.vue";
import type {
  FormFieldTemplateType,
  FromSelectOptionType,
  FormCheckBoxGroupOptionType,
  FormRadioGroupOptionType,
} from "../../core/FormType";

/* ------- form data ----------- */
const form = ref({
  exampleInput: "",
  exampleSelect: "1",
  exampleCheckBoxGroup: ["2"],
  exampleRadioGroup: ["3"],
});
const updateFormValue = (value: any, id: string) => {
  form.value[id as keyof typeof form.value] = value;
  console.log("form data", form.value, value);
};

/* -------- input -------- */
const inputDisabled = ref(false);
const inputState = ref(false);
const inputFieldProps: FormFieldTemplateType = {
  label: "Example Input",
  id: "exampleInput",
  note: "Example of note text",
  errorMsg: "Example of error message",
};

/* -------- select -------- */
const selectDisabled = ref(false);
const selectState = ref(null);
const selectFieldProps: FormFieldTemplateType = {
  label: "Example Select",
  id: "exampleSelect",
  required: true,
  note: "Example of note text",
  tooltip: "tooltip placeholder",
};
const selectOptions: Array<FromSelectOptionType> = [
  { value: "1", text: "a" },
  { value: "2", text: "b" },
];

/* -------- check box group -------- */
const checkBoxGroupDisabled = ref(false);
const checkBoxGroupState = ref(null);
const checkBoxGroupFieldProps: FormFieldTemplateType = {
  label: "Example Check Box Group",
  id: "exampleCheckBoxGroup",
};
const checkBoxGroupOptions: Array<FormCheckBoxGroupOptionType> = [
  { code: "1", text: "Option 1" },
  { code: "2", text: "Option 2" },
  { code: "3", text: "Option 3" },
];

/* -------- radio group -------- */
const radioGroupDisabled = ref(false);
const radioGroupState = ref(null);
const radioGroupFieldProps: FormFieldTemplateType = {
  label: "Example Radio Group",
  id: "exampleRadioGroup",
};
const radioGroupOptions: Array<FormRadioGroupOptionType> = [
  { code: "1", text: "Option 1" },
  { code: "2", text: "Option 2" },
  { code: "3", text: "Option 3" },
];
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "SingleFormComponentExample",
});
</script>

<style scoped></style>
