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
      v-if="computedCompanyType"
      :data="formData.data.information"
      :sectionProps="informationSectionSchema[computedCompanyType]"
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
import FormSectionTemplate from "./FormSectionTemplate.vue";
import { beginSectionSchema } from "../formsectionschemas/BeginSectionSchema";
import { informationSectionSchema } from "../formsectionschemas/InformationSectionSchema";
import { contactSectionSchema } from "../formsectionschemas/ContactSectionSchema";
import { authorizedSectionSchema } from "../formsectionschemas/AuthorizedSectionSchema";
import { validationResult } from "../../../helpers/AppState";
import { formData } from "../../../helpers/FormState";
import type { FormComponentSchemaType } from "../../../core/AppType";

// todo: based on the validation result,
// create computed section schemas to determine when to display error messages as specified in the validation result

const computedBeginSchema = computed(() => {
  const beginSectionSchemaCopy = JSON.parse(JSON.stringify(beginSectionSchema));
  if (validationResult.value && validationResult.value.length > 0) {
    validationResult.value.forEach((each) => {
      if (each.containerId == "begin") {
        const targetField = each.fieldId;
        beginSectionSchemaCopy.content.forEach(
          (field: FormComponentSchemaType) => {
            if (field.fieldProps.id == targetField) {
              field.fieldProps.errorMsg = each.errorMsg;
            }
          }
        );
      }
    });
  }
  return beginSectionSchemaCopy;
});

// todo: do the same for other sections

// return different form content for information section based on the client type
const computedCompanyType = computed(() => {
  if (
    formData.data.begin &&
    formData.data.begin.client_type &&
    formData.data.begin.client_type !== ""
  ) {
    if (
      formData.data.begin.client_type != "individual" &&
      formData.data.begin.client_type != "soleProprietorship"
    ) {
      return "company";
    }
    return formData.data.begin.client_type;
  }
  return null;
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
