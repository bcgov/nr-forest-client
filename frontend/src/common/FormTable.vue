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
              @updateFormValue="
                (id, newValue) => updateFormArrayValue(id, newValue, rowIndex)
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
import { ref } from "vue";
import type { PropType } from "vue";
import FormFieldTemplate from "./FormFieldTemplate.vue";
import FormComponentOptions from "./FormComponentOptions.vue";
import PrimarySquareButton from "./buttons/PrimarySquareButton.vue";
import BiXCircle from "~icons/bi/x-circle";
import type {
  FormFieldTemplateType,
  FormComponentSchemaType,
  CommonObjectType,
} from "../core/AppType";

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
  name: "FormTable",
});
</script>

<style scoped></style>
