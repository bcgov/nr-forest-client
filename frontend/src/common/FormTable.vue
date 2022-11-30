<template>
  <b-table-simple responsive>
    <b-tbody>
      <!-- row.index must be unique, otherwise v-for can not detect the data change correctly -->
      <!-- VueJS updates the DOM based on the change in key -->
      <!-- https://stackoverflow.com/questions/69890148/element-deleted-from-the-array-but-v-for-doesnt-update-vuejs -->
      <b-tr v-for="(row, rowIndex) in data" :key="'row-' + row.index">
        <b-td
          v-for="(column, columnIndex) in columns"
          :key="'col-' + columnIndex"
        >
          <FormInput
            v-if="column.type == 'input'"
            :fieldProps="column.title"
            :value="row[column]"
            @updateValue="
              (id, newValue) => updateFormTable(id, newValue, rowIndex)
            "
          />
          <FormSelect
            v-if="column.type == 'select'"
            :fieldProps="column.title"
            :value="row[column]"
            :options="column.options"
            @updateValue="
              (id, newValue) => updateFormTable(id, newValue, rowIndex)
            "
          />
        </b-td>
        <b-td>
          <bi-x-circle
            style="font-size: 16px; color: red; margin-top: 36px"
            @click="deleteRow(rowIndex)"
          />
        </b-td>
      </b-tr>
      <PrimarySquareButton :text="addButtonText" @click="addRow()" />
    </b-tbody>
  </b-table-simple>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { PropType } from "vue";
import FormInput from "./FormInput.vue";
import FormSelect from "./FormSelect.vue";
import PrimarySquareButton from "./buttons/PrimarySquareButton.vue";
import BiXCircle from "~icons/bi/x-circle";
import type {
  FormComponentStructureType,
  CommonObjectType,
} from "../core/AppType";

const props = defineProps({
  columns: {
    type: Array as PropType<Array<FormComponentStructureType>>,
    required: true,
  },
  addButtonText: String,
  data: {
    type: Array as PropType<Array<CommonObjectType>>,
    required: true,
  },
});

const emit = defineEmits(["updateFormTable", "addRow", "deleteRow"]);

const updateFormTable = (id, newValue, row) => {
  emit("updateFormTable", id, newValue, row);
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
  name: "FormTable",
});
</script>

<style scoped></style>
