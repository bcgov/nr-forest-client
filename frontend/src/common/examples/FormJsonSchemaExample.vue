<template>
  <div style="margin: 24px">
    <CollapseCard
      :title="containerProps.container.title"
      defaultOpen
      :id="containerProps.container.id"
    >
      <div v-for="(row, rowIndex) in containerProps.content" :key="rowIndex">
        <FormComponentOptions
          v-if="!row.depend || formData[row.depend.fieldModelName] == row.depend.value"
          :data="formData[row.fieldProps.modelName]"
          :schema="row"
          @updateFormValue="(id, newValue) => updateFormValue(id, newValue)"
          @updateFormArrayValue="
            (id, newValue, rowIndex) =>
              updateFormArrayValue(row.fieldProps.modelName, id, newValue, rowIndex)
          "
          @addRow="() => addRow(row.fieldProps.modelName)"
          @deleteRow="(rowIndex) => deleteRow(row.fieldProps.modelName, rowIndex)"
        />
      </div>
    </CollapseCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import FormComponentOptions from "../FormComponentOptions.vue";
import CollapseCard from "../CollapseCardComponent.vue";

/* ------- container props ----------*/
const containerProps = {
  container: { 
    title: "Sample Form section", 
    id: "sample" 
  }, // property for CollapseCard when use it
  content: [
    // form content for each CollapseCard
    {
      fieldProps: { 
        label: "Name", 
        id: "exampleInput",
        modelName: "exampleInput" 
      },
      type: "input",
    },
    {
      fieldProps: {
        label: "Which color you like",
        required: true,
        id: "exampleSelect",
        modelName: "exampleSelect",
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
      fieldProps: { 
        label: "Which drink you like", 
        id: "exampleCheckBoxGroup",
        modelName: "exampleCheckBoxGroup"
      },
      type: "checkboxgroup",
      options: [
        { code: "coffee", text: "Coffee" },
        { code: "tea", text: "Tea" },
        { code: "milk", text: "Milk" },
      ],
      depend: {
        fieldModelName: "exampleSelect",
        value: "red",
      },
    },
    {
      fieldProps: { 
        label: "Which food you like", 
        id: "exampleRadioGroup",
        modelName: "exampleRadioGroup"
      },
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
        modelName: "exampleTable"
      },
      type: "table",
      addButtonText: "+ Add another friend",
      subfields: [
        {
          fieldProps: {
            label: "Name",
            id: "tableInput",
            modelName: "tableInput"
          },
          type: "input",
        },
        {
          fieldProps: { 
            label: "Color", 
            id: "tableSelect",
            modelName: "tableSelect"
          },
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
        modelName: "exampleGroup"
      },
      type: "group",
      addButtonText: "+ Add another like",
      deleteButtonText: "- Remove this like",
      subfields: [
        {
          fieldProps: {
            label: "Link",
            id: "groupInput",
            modelName: "groupInput"
          },
          type: "input",
        },
        {
          fieldProps: { 
            label: "Hobby", 
            id: "groupCheckBoxGroup",
            modelName: "groupCheckBoxGroup"
          },
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
const updateFormValue = (fieldModelName: string, value: any) => {
  formData.value[fieldModelName] = value;
};
const updateFormArrayValue = (
  fieldModelName: string,
  subFieldId: string,
  value: any,
  rowIndex: number
) => {
  formData.value[fieldModelName][rowIndex][subFieldId] = value;
};
const addRow = (fieldModelName: string) => {
  const defaultNew = JSON.parse(
    JSON.stringify(defaultFormData[fieldModelName as keyof typeof defaultFormData][0])
  );
  formData.value[fieldModelName].push({
    ...defaultNew,
    index: Math.floor(Math.random() * 10000000),
  });
};
const deleteRow = (fieldModelName: string, rowIndex: number) => {
  formData.value[fieldModelName].splice(rowIndex, 1);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "FormJsonSchemaExample",
});
</script>

<style scoped></style>
