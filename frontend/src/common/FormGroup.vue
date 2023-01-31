<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-card
      v-for="(row, rowIndex) in data"
      :key="row.index"
      style="margin-bottom: 12px"
    >
      <div
        v-for="(column, columnIndex) in subfields"
        :key="'col-' + columnIndex"
      >
        <FormComponentOptions
          :data="row[column.fieldProps.modelName]"
          :schema="column"
          :error="computedError(column.fieldProps.modelName, rowIndex)"
          :disabledFields="
            computedDisabledFields(column.fieldProps.modelName, rowIndex)
          "
          :disableAll="disableAll"
          @updateFormValue="
            (newValue, path) =>
              updateFormArrayValue(
                newValue,
                path != '' ? `${rowIndex}.${path}` : `${rowIndex}`
              )
          "
          @updateFormArrayValue="
            (newValue, path) =>
              updateFormArrayValue(
                newValue,
                path != ''
                  ? `${rowIndex}.${column.fieldProps.modelName}.${path}`
                  : `${rowIndex}.${column.fieldProps.modelName}`
              )
          "
          @addRow="
            (path = '') =>
              addRow(
                path != ''
                  ? `${rowIndex}.${column.fieldProps.modelName}.${path}`
                  : `${rowIndex}.${column.fieldProps.modelName}`
              )
          "
          @deleteRow="
            (subRowIndex, path = '') =>
              deleteRow(
                subRowIndex,
                path != ''
                  ? `${rowIndex}.${column.fieldProps.modelName}.${path}`
                  : `${rowIndex}.${column.fieldProps.modelName}`
              )
          "
        />
      </div>
      <PrimarySquareButton
        id="groupDeleteButton"
        v-if="data.length > 1"
        :text="deleteButtonText"
        @click="deleteRow(rowIndex)"
      />
    </b-card>
    <PrimarySquareButton
      id="groupAddButton"
      :text="addButtonText"
      @click="addRow()"
    />
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import _ from "lodash";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import FormComponentOptions from "./FormComponentOptions.vue";
import PrimarySquareButton from "./buttons/PrimarySquareButton.vue";
import type {
  FormFieldTemplateType,
  FormComponentSchemaType,
  CommonObjectType,
  FormFieldValidationResultType,
} from "../core/FormType";

const props = defineProps({
  subfields: {
    type: Array as PropType<Array<FormComponentSchemaType>>,
    required: true,
  },
  addButtonText: String,
  deleteButtonText: String,
  data: {
    type: Array as PropType<Array<CommonObjectType>>,
    required: true,
  },
  fieldProps: Object as PropType<FormFieldTemplateType>,
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

const computedError = computed(() => {
  return (subFieldModelName: string, rowIndex: number) => {
    const key = `${rowIndex}.${subFieldModelName}`;
    return props.error.filter((each) => _.includes(each.path, key));
  };
});

const computedDisabledFields = computed(() => {
  return (subFieldModelName: string, rowIndex: number) => {
    const key = `${rowIndex}.${subFieldModelName}`;
    return props.disabledFields.filter((each) => _.includes(each, key));
  };
});

const emit = defineEmits(["updateFormArrayValue", "addRow", "deleteRow"]);

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
  name: "FormGroup",
});
</script>

<style scoped></style>
