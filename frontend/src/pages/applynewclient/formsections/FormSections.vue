<template>
  <div>
    <!------ begin section ------->
    <FormSectionTemplate
      :data="formData.state.begin"
      :sectionProps="computedBeginSchema"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.mutations.updateFormValue('begin', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.mutations.updateFormArrayValue(
            'begin',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.mutations.addRow('begin', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) =>
          formData.mutations.deleteRow('begin', fieldId, rowIndex)
      "
    />

    <!------ company/individual information section ------->
    <FormSectionTemplate
      v-if="computedInformationSchemaType !== ''"
      :data="formData.state.information"
      :sectionProps="computedInformationSchema[computedInformationSchemaType]"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.mutations.updateFormValue('information', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.mutations.updateFormArrayValue(
            'information',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.mutations.addRow('information', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) =>
          formData.mutations.deleteRow('information', fieldId, rowIndex)
      "
    />

    <!------ contact information section ------->
    <FormSectionTemplate
      :data="formData.state.contact"
      :sectionProps="computedContactSectionSchema"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.mutations.updateFormValue('contact', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.mutations.updateFormArrayValue(
            'contact',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.mutations.addRow('contact', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) =>
          formData.mutations.deleteRow('contact', fieldId, rowIndex)
      "
    />

    <!------ add authorized individual section ------->
    <FormSectionTemplate
      :data="formData.state.authorized"
      :sectionProps="computedAuthorizedSectionSchema"
      @updateFormValue="
        (fieldId, newValue) =>
          formData.mutations.updateFormValue('authorized', fieldId, newValue)
      "
      @updateFormArrayValue="
        (fieldId, subFieldId, newValue, rowIndex) =>
          formData.mutations.updateFormArrayValue(
            'authorized',
            fieldId,
            subFieldId,
            newValue,
            rowIndex
          )
      "
      @addRow="(fieldId) => formData.mutations.addRow('authorized', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) =>
          formData.mutations.deleteRow('authorized', fieldId, rowIndex)
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
import { formData } from "../../../helpers/FormState";
import { addErrMsgToSchema } from "../../../helpers/formvalidation/AddErrorToSchema";

// create computed section schemas to determine when to display error messages as specified in the validation result
const computedBeginSchema = computed(() => {
  return addErrMsgToSchema(beginSectionSchema, "begin");
});
const computedInformationSchema = computed(() => {
  return addErrMsgToSchema(informationSectionSchema, "information");
});
const computedContactSectionSchema = computed(() => {
  return addErrMsgToSchema(contactSectionSchema, "contact");
});
const computedAuthorizedSectionSchema = computed(() => {
  return addErrMsgToSchema(authorizedSectionSchema, "authorized");
});

// based on client type, show different schema contenct for the information section
const computedInformationSchemaType = computed(() => {
  if (
    _.has(formData, ["state", "begin", "client_type"]) &&
    formData.state.begin.client_type !== ""
  ) {
    if (
      formData.state.begin.client_type != "individual" &&
      formData.state.begin.client_type != "soleProprietorship"
    ) {
      return "company";
    }
    // other types share the same schema as company
    return formData.state.begin.client_type;
  }
  return "";
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
