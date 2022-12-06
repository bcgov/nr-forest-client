<template>
  <div>
    <b-card
      v-for="(row, rowIndex) in data"
      :key="row.index"
      style="margin-bottom: 12px"
    >
      <div v-for="(column, columnIndex) in columns" :key="'col-' + columnIndex">
        <FormComponentOptions
          :data="row[column.fieldProps.id]"
          :schema="column"
          @updateFormValue="
            (id, newValue) => updateFormArrayValue(id, newValue, rowIndex)
          "
        />
      </div>
      <PrimarySquareButton
        v-if="data.length > 1"
        :text="deleteButtonText"
        @click="deleteRow(rowIndex)"
      />
    </b-card>
    <PrimarySquareButton :text="addButtonText" @click="addRow()" />
  </div>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import FormComponentOptions from "./FormComponentOptions.vue";
import PrimarySquareButton from "./buttons/PrimarySquareButton.vue";
import type {
  FormComponentSchemaType,
  CommonObjectType,
} from "../core/AppType";

const props = defineProps({
  columns: {
    type: Array as PropType<Array<FormComponentSchemaType>>,
    required: true,
  },
  addButtonText: String,
  deleteButtonText: String,
  data: {
    type: Array as PropType<Array<CommonObjectType>>,
    required: true,
  },
});

const emit = defineEmits(["updateFormArrayValue", "addRow", "deleteRow"]);

const updateFormArrayValue = (id, newValue, row) => {
  emit("updateFormArrayValue", id, newValue, row);
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
  name: "FormGroup",
});
</script>

<style scoped></style>
