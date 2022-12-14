<template>
  <div>
    <FormInput
      v-if="schema.type == 'input'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormSelect
      v-if="schema.type == 'select'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :options="schema.options"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormCheckbox
      v-if="schema.type == 'checkbox'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormCheckboxGroup
      v-if="schema.type == 'checkboxgroup'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :options="schema.options"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormRadioGroup
      v-if="schema.type == 'radiogroup'"
      :fieldProps="schema.fieldProps"
      :value="data"
      :options="schema.options"
      :disabled="schema.disabled ? true : false"
      @updateValue="updateFormValue"
    />
    <FormGroup
      v-if="schema.type == 'group'"
      :data="data"
      :addButtonText="schema.addButtonText"
      :deleteButtonText="schema.deleteButtonText"
      :columns="schema.columns"
      :fieldProps="schema.fieldProps"
      @updateFormArrayValue="updateFormArrayValue"
      @addRow="addRow"
      @deleteRow="deleteRow"
    />
    <FormTable
      v-if="schema.type == 'table'"
      :data="data"
      :addButtonText="schema.addButtonText"
      :columns="schema.columns"
      :fieldProps="schema.fieldProps"
      @updateFormArrayValue="updateFormArrayValue"
      @addRow="addRow"
      @deleteRow="deleteRow"
    />
  </div>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import FormInput from "./FormInput.vue";
import FormSelect from "./FormSelect.vue";
import FormCheckbox from "./FormCheckbox.vue";
import FormCheckboxGroup from "./FormCheckboxGroup.vue";
import FormRadioGroup from "./FormRadioGroup.vue";
import FormGroup from "./FormGroup.vue";
import FormTable from "./FormTable.vue";
import type { FormComponentSchemaType } from "../core/AppType";

const props = defineProps({
  schema: {
    type: Object as PropType<FormComponentSchemaType>,
    required: true,
  },
  data: {
    type: [String, Object, Number, Array, Boolean],
    required: true,
  },
});

const emit = defineEmits([
  "updateFormValue",
  "updateFormArrayValue",
  "addRow",
  "deleteRow",
]);

const updateFormValue = (id, newValue) => {
  emit("updateFormValue", id, newValue);
};
const updateFormArrayValue = (id, value, row) => {
  emit("updateFormArrayValue", id, value, row);
};
const addRow = () => {
  emit("addRow");
};
const deleteRow = (row) => {
  emit("deleteRow", row);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormComponentOptions",
});
</script>

<style scoped></style>
