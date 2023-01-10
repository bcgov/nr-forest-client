<template>
  <CollapseCard
    :title="sectionProps.container.title"
    defaultOpen
    :id="sectionProps.container.id"
  >
    <div v-for="(row, rowIndex) in sectionProps.content" :key="rowIndex">
      <FormComponentOptions
        v-if="!row.depend || data[row.depend.fieldId] == row.depend.value"
        :data="data[row.fieldProps.id]"
        :schema="row"
        @updateFormValue="(id, newValue) => updateFormValue(id, newValue)"
        @updateFormArrayValue="
          (id, newValue, rowIndex) =>
            updateFormArrayValue(row.fieldProps.id, id, newValue, rowIndex)
        "
        @addRow="() => addRow(row.fieldProps.id)"
        @deleteRow="(rowIndex) => deleteRow(row.fieldProps.id, rowIndex)"
      />
    </div>
  </CollapseCard>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import CollapseCard from "../../../common/CollapseCard.vue";
import FormComponentOptions from "../../../common/FormComponentOptions.vue";
import type {
  CommonObjectType,
  FormSectionSchemaType,
} from "../../../core/AppType";

const props = defineProps({
  data: {
    type: Object as PropType<CommonObjectType>,
    required: true,
  },
  sectionProps: {
    type: Object as PropType<FormSectionSchemaType>,
    required: true,
  },
});

const emit = defineEmits([
  "updateFormValue",
  "updateFormArrayValue",
  "addRow",
  "deleteRow",
]);

const updateFormValue = (fieldId: string, newValue: any) => {
  emit("updateFormValue", fieldId, newValue);
};
const updateFormArrayValue = (
  fieldId: string,
  columnId: string,
  newValue: any,
  rowIndex: number
) => {
  emit("updateFormArrayValue", fieldId, columnId, newValue, rowIndex);
};
const addRow = (fieldId: string) => {
  emit("addRow", fieldId);
};
const deleteRow = (fieldId: string, rowIndex: number) => {
  emit("deleteRow", fieldId, rowIndex);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormSectionTemplate",
});
</script>

<style scoped></style>
