<template>
  <CollapseCard
    :title="containerProps.container.title"
    defaultOpen
    :id="containerProps.container.id"
  >
    <div v-for="(row, rowIndex) in containerProps.content" :key="rowIndex">
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
import { computed } from "vue";
import type { PropType } from "vue";
import CollapseCard from "../../../common/CollapseCard.vue";
import FormComponentOptions from "../../../common/FormComponentOptions.vue";
import type {
  CommonObjectType,
  FormSectionContainerType,
} from "../../../core/AppType";

const props = defineProps({
  data: {
    type: Object as PropType<CommonObjectType>,
    required: true,
  },
  containerProps: {
    type: Object as PropType<FormSectionContainerType>,
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
const updateFormArrayValue = (fieldId, id, value, rowIndex) => {
  emit("updateFormArrayValue", fieldId, id, value, rowIndex);
};
const addRow = (fieldId) => {
  emit("addRow", fieldId);
};
const deleteRow = (fieldId, rowIndex) => {
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
