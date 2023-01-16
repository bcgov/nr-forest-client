<template>
  <div>
    <!------ begin section ------->
    <FormSectionTemplate
      :data="formData.data.begin"
      :sectionProps="computedBeginSchema"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.updateFormValue('begin', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.updateFormArrayValue(
            'begin',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.addRow('begin', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) => formData.deleteRow('begin', fieldId, rowIndex)
      "
    />

    <!------ company/individual information section ------->
    <FormSectionTemplate
      v-if="computedCompanyType !== ''"
      :data="formData.data.information"
      :sectionProps="computedInformationSchema[computedCompanyType]"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.updateFormValue('information', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.updateFormArrayValue(
            'information',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.addRow('information', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) =>
          formData.deleteRow('information', fieldId, rowIndex)
      "
    />

    <!------ contact information section ------->
    <FormSectionTemplate
      :data="formData.data.contact"
      :sectionProps="contactSectionSchema"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.updateFormValue('contact', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.updateFormArrayValue(
            'contact',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.addRow('contact', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) => formData.deleteRow('contact', fieldId, rowIndex)
      "
    />

    <!------ add authorized individual section ------->
    <FormSectionTemplate
      :data="formData.data.authorized"
      :sectionProps="authorizedSectionSchema"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.updateFormValue('authorized', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.updateFormArrayValue(
            'authorized',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.addRow('authorized', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) =>
          formData.deleteRow('authorized', fieldId, rowIndex)
      "
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import _ from "lodash";
import FormSectionTemplate from "./FormSectionTemplate.vue";
import { beginSectionSchema } from "../formsectionschemas/BeginSectionSchema";
import { informationSectionSchema } from "../formsectionschemas/InformationSectionSchema";
import { contactSectionSchema } from "../formsectionschemas/ContactSectionSchema";
import { authorizedSectionSchema } from "../formsectionschemas/AuthorizedSectionSchema";
import { formData, validationResult } from "../../../helpers/FormState";
import { addErrMsgToSchema } from "../../../helpers/formvalidation/AddErrorToSchema";

// create computed section schemas to determine when to display error messages as specified in the validation result
const computedBeginSchema = computed(() => {
  let beginSectionSchemaCopy = JSON.parse(JSON.stringify(beginSectionSchema));
  if (
    _.has(validationResult, ["value", "begin"]) &&
    validationResult.value.begin.length > 0
  ) {
    validationResult.value.begin.forEach((eachRow) => {
      beginSectionSchemaCopy = {
        ...beginSectionSchemaCopy,
        content: addErrMsgToSchema(beginSectionSchemaCopy.content, eachRow),
      };
    });
  }
  return beginSectionSchemaCopy;
});

const computedInformationSchema = computed(() => {
  let informationSectionSchemaCopy = JSON.parse(
    JSON.stringify(informationSectionSchema)
  );
  if (
    _.has(validationResult, ["value", "information"]) &&
    validationResult.value.information.length > 0 &&
    formData.data.begin.client_type !== ""
  ) {
    const type = getInformationSchemaType();
    validationResult.value.information.forEach((eachRow) => {
      informationSectionSchemaCopy[type] = {
        ...informationSectionSchemaCopy[type],
        content: addErrMsgToSchema(
          informationSectionSchemaCopy[type].content,
          eachRow
        ),
      };
    });
  }
  return informationSectionSchemaCopy;
});

// todo: do the same for other sections

// return different form content for information section based on the client type
const computedCompanyType = computed(() => {
  if (
    _.has(formData, ["dadta", "begin", "client_type"]) &&
    formData.data.begin.client_type !== ""
  ) {
    return getInformationSchemaType();
  }
  return "";
});

const getInformationSchemaType = () => {
  if (
    formData.data.begin.client_type != "individual" &&
    formData.data.begin.client_type != "soleProprietorship"
  ) {
    return "company";
  }
  return formData.data.begin.client_type;
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
