<template>
  <CollapseCard
    :title="authorizedSectionSchema.container.title"
    defaultOpen
    :id="authorizedSectionSchema.container.id"
  >
    <div
      v-for="(row, rowIndex) in authorizedSectionSchema.content"
      :key="rowIndex"
    >
      <!-- <FormComponentOptions
        :data="data[row.fieldProps.id]"
        :schema="row"
        @updateFormValue="(id, newValue) => updateFormValue(id, newValue)"
      /> -->
      <FormTable
        v-if="row.type == 'table'"
        :data="data[row.fieldProps.id]"
        :addButtonText="row.addButtonText"
        :columns="row.columns"
        @updateFormArrayValue="updateFormArrayValue"
        @addRow="addRow"
        @deleteRow="deleteRow"
      />
    </div>
  </CollapseCard>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { PropType } from "vue";
import CollapseCard from "../../../common/CollapseCard.vue";
// import FormComponentOptions from "../../common/FormComponentOptions.vue";
import FormTable from "../../../common/FormTable.vue";
import { authorizedSectionSchema } from "../NewClient";
import type { FormSectionSchemaType } from "../../../core/AppType";

const props = defineProps({
  data: {
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
  name: "AddAuthorizedSection",
});
</script>

<style scoped></style>
