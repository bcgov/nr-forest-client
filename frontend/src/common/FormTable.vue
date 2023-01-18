<template>
  <FormFieldTemplate :fieldProps="fieldProps">
    <b-table-simple responsive>
      <b-tbody>
        <!-- row.index must be unique, otherwise v-for can not detect the data change correctly -->
        <!-- VueJS updates the DOM based on the change in key -->
        <!-- https://stackoverflow.com/questions/69890148/element-deleted-from-the-array-but-v-for-doesnt-update-vuejs -->
        <b-tr v-for="(row, rowIndex) in data" :key="row.index">
          <b-td
            v-for="(column, columnIndex) in subfields"
            :key="'col-' + columnIndex"
          >
            <FormComponentOptions
              :data="row[column.fieldProps.id]"
              :schema="column"
              :error="computedError(column.fieldProps.id, rowIndex)"
              @updateFormValue="
                (newValue, path) =>
                  updateFormArrayValue(newValue, [rowIndex, ...path])
              "
              @updateFormArrayValue="
                (newValue, path) =>
                  updateFormArrayValue(newValue, [
                    rowIndex,
                    column.fieldProps.id,
                    ...path,
                  ])
              "
              @addRow="
                (path = []) => addRow([rowIndex, column.fieldProps.id, ...path])
              "
              @deleteRow="
                (subRowIndex, path = []) =>
                  deleteRow(subRowIndex, [
                    rowIndex,
                    column.fieldProps.id,
                    ...path,
                  ])
              "
            />
          </b-td>
          <b-td>
            <bi-x-circle
              id="tableDeleteButton"
              v-if="data.length > 1"
              style="font-size: 16px; color: red; margin-top: 36px"
              @click="deleteRow(rowIndex)"
            />
          </b-td>
        </b-tr>
        <PrimarySquareButton
          id="tableAddButton"
          :text="addButtonText"
          @click="addRow()"
        />
      </b-tbody>
    </b-table-simple>
  </FormFieldTemplate>
</template>

<script setup lang="ts">
import { computed } from "vue";
import _ from "lodash";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import FormComponentOptions from "./FormComponentOptions.vue";
import PrimarySquareButton from "./buttons/PrimarySquareButton.vue";
import BiXCircle from "~icons/bi/x-circle";
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
  return (subFieldId: string, rowIndex: number) => {
    const key = `${rowIndex}.${subFieldId}`;
    return props.error.filter((each) => _.includes(each.path, key));
  };
});
const emit = defineEmits(["updateFormArrayValue", "addRow", "deleteRow"]);

const updateFormArrayValue = (newValue: any, path: Array<string>) => {
  emit("updateFormArrayValue", newValue, path);
};
const addRow = (path = [] as Array<string>) => {
  emit("addRow", path);
};
const deleteRow = (index: number, path = [] as Array<string>) => {
  emit("deleteRow", index, path);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormTable",
});
</script>

<style scoped></style>
