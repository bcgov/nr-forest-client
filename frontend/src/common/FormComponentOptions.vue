<template>
  <div>
    <FormAutocomplete
      v-if="schema.type == 'autocomplete'"
      :fieldProps="computedFieldPropsForSingleComponent(schema.fieldProps)"
      :value="data"
      :disabled="computedDisableForSingleComponent"
      @updateValue="updateFormValue"
    />
    <FormInput
      v-if="schema.type == 'input'"
      :fieldProps="computedFieldPropsForSingleComponent(schema.fieldProps)"
      :value="data"
      :disabled="computedDisableForSingleComponent"
      @updateValue="updateFormValue"
    />
    <FormSelect
      v-if="schema.type == 'select'"
      :fieldProps="computedFieldPropsForSingleComponent(schema.fieldProps)"
      :value="data"
      :options="schema.options"
      :disabled="computedDisableForSingleComponent"
      @updateValue="updateFormValue"
    />
    <FormCheckbox
      v-if="schema.type == 'checkbox'"
      :fieldProps="computedFieldPropsForSingleComponent(schema.fieldProps)"
      :value="data"
      :disabled="computedDisableForSingleComponent"
      @updateValue="updateFormValue"
    />
    <FormCheckboxGroup
      v-if="schema.type == 'checkboxgroup'"
      :fieldProps="computedFieldPropsForSingleComponent(schema.fieldProps)"
      :value="data"
      :options="schema.options"
      :disabled="computedDisableForSingleComponent"
      @updateValue="updateFormValue"
    />
    <FormRadioGroup
      v-if="schema.type == 'radiogroup'"
      :fieldProps="computedFieldPropsForSingleComponent(schema.fieldProps)"
      :value="data"
      :options="schema.options"
      :disabled="computedDisableForSingleComponent"
      @updateValue="updateFormValue"
    />
    <FormGroup
      v-if="schema.type == 'group'"
      :data="data"
      :addButtonText="schema.addButtonText"
      :deleteButtonText="schema.deleteButtonText"
      :subfields="schema.subfields"
      :error="error"
      :disabledFields="disabledFields"
      :disableAll="disableAll"
      :fieldProps="schema.fieldProps"
      @updateFormArrayValue="updateFormArrayValue"
      @addRow="addRow"
      @deleteRow="deleteRow"
    />
    <FormTable
      v-if="schema.type == 'table'"
      :data="data"
      :addButtonText="schema.addButtonText"
      :subfields="schema.subfields"
      :error="error"
      :disabledFields="disabledFields"
      :disableAll="disableAll"
      :fieldProps="schema.fieldProps"
      @updateFormArrayValue="updateFormArrayValue"
      @addRow="addRow"
      @deleteRow="deleteRow"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import type { PropType } from "vue";
import FormInput from "./FormInput.vue";
import FormSelect from "./FormSelect.vue";
import FormCheckbox from "./FormCheckbox.vue";
import FormCheckboxGroup from "./FormCheckboxGroup.vue";
import FormRadioGroup from "./FormRadioGroup.vue";
import FormGroup from "./FormGroup.vue";
import FormTable from "./FormTable.vue";
import FormAutocomplete from "./FormAutocomplete.vue"
import type {
  FormComponentSchemaType,
  FormFieldValidationResultType,
  FormFieldTemplateType,
} from "../core/FormType";

const props = defineProps({
  schema: {
    type: Object as PropType<FormComponentSchemaType>,
    required: true,
  },
  data: {
    type: [String, Object, Number, Array, Boolean],
    required: true,
  },
  error: {
    type: Array<FormFieldValidationResultType>,
    default: [],
  },
  disabledFields: {
    type: Array<string>,
    default: [],
  },
  disableAll: {
    type: Boolean,
    default: false,
  },
});

const computedFieldPropsForSingleComponent = computed(() => {
  return (fieldProps: FormFieldTemplateType) => {
    if (props.error.length > 0) {
      return { ...fieldProps, errorMsg: props.error[0].errorMsg };
    }
    return fieldProps;
  };
});

const computedDisableForSingleComponent = computed(() => {
  if (props.disableAll || props.disabledFields.length > 0) return true;
  return false;
});

const emit = defineEmits([
  "updateFormValue",
  "updateFormArrayValue",
  "addRow",
  "deleteRow",
]);

const updateFormValue = (newValue: any, path: string) => {
  emit("updateFormValue", newValue, path);
};
const updateFormArrayValue = (newValue: any, path: string) => {
  emit("updateFormArrayValue", newValue, path);
};
const addRow = (path = "" as string) => {
  emit("addRow", path);
};
const deleteRow = (index: number, path = "" as string) => {
  emit("deleteRow", index, path);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormComponentOptions",
});
</script>

<style scoped></style>
