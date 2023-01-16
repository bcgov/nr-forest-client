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
      subfields: [
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
      subfields: [
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
const updateFormValue = (fieldId: string, value: any) => {
  formData.value[fieldId] = value;
};
const updateFormArrayValue = (
  fieldId: string,
  subFieldId: string,
  value: any,
  rowIndex: number
) => {
  formData.value[fieldId][rowIndex][subFieldId] = value;
};
const addRow = (fieldId: string) => {
  const defaultNew = JSON.parse(
    JSON.stringify(defaultFormData[fieldId as keyof typeof defaultFormData][0])
  );
  formData.value[fieldId].push({
    ...defaultNew,
    index: Math.floor(Math.random() * 10000000),
  });
};
const deleteRow = (fieldId: string, rowIndex: number) => {
  formData.value[fieldId].splice(rowIndex, 1);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormJsonSchemaExample",
});
</script>

<style scoped></style>
