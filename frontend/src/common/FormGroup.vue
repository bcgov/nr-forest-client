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
          :data="row[column.fieldProps.id]"
          :schema="column"
          :error="computedError(column.fieldProps.id, rowIndex)"
          @updateFormValue="
            (id, newValue) => updateFormArrayValue(id, newValue, rowIndex)
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
});

const computedError = computed(() => {
  return (subFieldId: string, rowIndex: number) =>
    props.error.filter(
      (each) => each.subFieldId == subFieldId && each.rowIndex == rowIndex
    );
});

const emit = defineEmits(["updateFormArrayValue", "addRow", "deleteRow"]);

const updateFormArrayValue = (id: string, newValue: any, row: number) => {
  emit("updateFormArrayValue", id, newValue, row);
};
const addRow = () => {
  emit("addRow");
};
const deleteRow = (row: number) => {
  emit("deleteRow", row);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormGroup",
});
</script>

<style scoped></style>
