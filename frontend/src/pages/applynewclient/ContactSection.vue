<template>
  <CollapseCard
    :title="contactSectionSchema.container.title"
    defaultOpen
    :id="contactSectionSchema.container.id"
  >
    <div
      v-for="(row, rowIndex) in contactSectionSchema.content"
      :key="rowIndex"
    >
      <!-- <FormComponentOptions
        :data="data[row.fieldProps.id]"
        :schema="row"
        @updateFormValue="(id, newValue) => updateFormValue(id, newValue)"
      /> -->
      <FormGroup
        v-if="row.type == 'group'"
        :data="data[row.fieldProps.id]"
        :addButtonText="row.addButtonText"
        :deleteButtonText="row.deleteButtonText"
        :columns="row.columns"
        @updateFormArray="updateFormArray"
        @addRow="addRow"
        @deleteRow="deleteRow"
      />
    </div>
  </CollapseCard>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import CollapseCard from "../../common/CollapseCard.vue";
// import FormComponentOptions from "../../common/FormComponentOptions.vue";
import FormGroup from "../../common/FormGroup.vue";
import { contactSectionSchema } from "./NewClient";
import type { FormSectionSchemaType } from "../../core/AppType";

const props = defineProps({
  data: {
    type: Object as PropType<FormSectionSchemaType>,
    required: true,
  },
});

const emit = defineEmits([
  "updateFormValue",
  "updateFormArray",
  "addRow",
  "deleteRow",
]);

const updateFormValue = (id, newValue) => {
  emit("updateFormValue", id, newValue);
};
const updateFormArray = (id, value, row) => {
  emit("updateFormArray", id, value, row);
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
  name: "ContactSection",
});
</script>

<style scoped></style>
