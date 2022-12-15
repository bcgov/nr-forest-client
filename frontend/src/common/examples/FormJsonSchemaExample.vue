<template>
  <div style="margin: 24px">
    <CollapseCard
      :title="containerProps.container.title"
      defaultOpen
      :id="containerProps.container.id"
    >
      <div v-for="(row, rowIndex) in containerProps.content" :key="rowIndex">
        <FormComponentOptions
          v-if="!row.depend || formData[row.depend.fieldId] == row.depend.value"
          :data="formData[row.fieldProps.id]"
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
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import FormComponentOptions from "../FormComponentOptions.vue";
import CollapseCard from "../CollapseCard.vue";
import type {
  FormFieldTemplateType,
  FromSelectOptionType,
  FormCheckBoxGroupOptionType,
  FormRadioGroupOptionType,
} from "../../core/AppType";

/* ------- container props ----------*/
const containerProps = {
  container: { title: "Sample Form section", id: "sample" }, // property for CollapseCard when use it
  content: [
    // form content for each CollapseCard
    {
      fieldProps: { label: "Name", id: "exampleInput" },
      type: "input",
    },
    {
      fieldProps: {
        label: "Which color you like",
        required: true,
        id: "exampleSelect",
        note: "Example of dropdown select",
        tooltip: "tooltip placeholder",
        errorMsg: "This field is required",
      },
      type: "select",
      options: [
        { value: "red", text: "Red" },
        { value: "green", text: "Green" },
      ],
    },
    {
      fieldProps: { label: "Which drink you like", id: "exampleCheckBoxGroup" },
      type: "checkboxgroup",
      options: [
        { code: "coffee", text: "Coffee" },
        { code: "tea", text: "Tea" },
        { code: "milk", text: "Milk" },
      ],
      depend: {
        fieldId: "exampleSelect",
        value: "red",
      },
    },
    {
      fieldProps: { label: "Which food you like", id: "exampleRadioGroup" },
      type: "radiogroup",
      options: [
        { code: "rice", text: "Rice" },
        { code: "noodle", text: "Noodle" },
      ],
    },
    {
      fieldProps: {
        label: "Example Table",
        id: "exampleTable",
      },
      type: "table",
      addButtonText: "+ Add another friend",
      columns: [
        {
          fieldProps: {
            label: "Name",
            id: "tableInput",
          },
          type: "input",
        },
        {
          fieldProps: { label: "Color", id: "tableSelect" },
          type: "select",
          options: [
            { value: "red", text: "Red" },
            { value: "green", text: "green" },
          ],
        },
      ],
    },
    {
      fieldProps: {
        id: "exampleGroup",
      },
      type: "group",
      addButtonText: "+ Add another like",
      deleteButtonText: "- Remove this like",
      columns: [
        {
          fieldProps: {
            label: "Link",
            id: "groupInput",
          },
          type: "input",
        },
        {
          fieldProps: { label: "Hobby", id: "groupCheckBoxGroup" },
          type: "checkboxgroup",
          options: [
            { code: "swim", text: "Swim" },
            { code: "dance", text: "Dance" },
            { code: "sing", text: "Sing" },
          ],
        },
      ],
    },
  ],
};

/* ------- form data ----------- */
const defaultFormData = {
  exampleInput: "",
  exampleSelect: "",
  exampleCheckBoxGroup: [],
  exampleRadioGroup: [],
  exampleTable: [
    {
      tableInput: "",
      tableSelect: "",
    },
  ],
  exampleGroup: [
    {
      groupInput: "",
      groupCheckBoxGroup: [],
    },
  ],
};

const formData = ref(JSON.parse(JSON.stringify(defaultFormData)));

/* --------------- update form data functions --------------------- */
const updateFormValue = (fieldId, value) => {
  formData.value[fieldId] = value;
};
const updateFormArrayValue = (fieldId, columnId, value, rowIndex) => {
  formData.value[fieldId][rowIndex][columnId] = value;
};
const addRow = (fieldId) => {
  const defaultNew = JSON.parse(JSON.stringify(defaultFormData[fieldId][0]));
  formData.value[fieldId].push({
    ...defaultNew,
    index: Math.floor(Math.random() * 10000000),
  });
};
const deleteRow = (fieldId, rowIndex) => {
  formData.value[fieldId].splice(rowIndex, 1);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "CompositionComponentTemplate",
});
</script>

<style scoped></style>
