<template>
  <div>
    <!------ begin section ------->
    <FormSectionTemplate
      :data="data.begin"
      :sectionProps="computedBeginSchema"
      @updateFormValue="
        (id, newValue) => updateFormValue('begin', id, newValue)
      "
      @updateFormArrayValue="
        (fieldId, id, newValue, rowIndex) =>
          updateFormArrayValue('begin', fieldId, id, newValue, rowIndex)
      "
      @addRow="(fieldId) => addRow('begin', fieldId)"
      @deleteRow="(fieldId, rowIndex) => deleteRow('begin', fieldId, rowIndex)"
    />

    <!------ company/individual information section ------->
    <FormSectionTemplate
      v-if="computedCompanyType"
      :data="data.information"
      :sectionProps="informationSectionSchema[computedCompanyType]"
      @updateFormValue="
        (id, newValue) => updateFormValue('information', id, newValue)
      "
      @updateFormArrayValue="
        (fieldId, id, newValue, rowIndex) =>
          updateFormArrayValue('information', fieldId, id, newValue, rowIndex)
      "
      @addRow="(fieldId) => addRow('information', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) => deleteRow('information', fieldId, rowIndex)
      "
    />

    <!------ contact information section ------->
    <FormSectionTemplate
      :data="data.contact"
      :sectionProps="contactSectionSchema"
      @updateFormValue="
        (id, newValue) => updateFormValue('contact', id, newValue)
      "
      @updateFormArrayValue="
        (fieldId, id, newValue, rowIndex) =>
          updateFormArrayValue('contact', fieldId, id, newValue, rowIndex)
      "
      @addRow="(fieldId) => addRow('contact', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) => deleteRow('contact', fieldId, rowIndex)
      "
    />

    <!------ add authorized individual section ------->
    <FormSectionTemplate
      :data="data.authorized"
      :sectionProps="authorizedSectionSchema"
      @updateFormValue="
        (id, newValue) => updateFormValue('authorized', id, newValue)
      "
      @updateFormArrayValue="
        (fieldId, id, newValue, rowIndex) =>
          updateFormArrayValue('authorized', fieldId, id, newValue, rowIndex)
      "
      @addRow="(fieldId) => addRow('authorized', fieldId)"
      @deleteRow="
        (fieldId, rowIndex) => deleteRow('authorized', fieldId, rowIndex)
      "
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, reactive } from "vue";
import type { PropType } from "vue";
import FormSectionTemplate from "./FormSectionTemplate.vue";
import { validationResult } from "../../../helpers/AppState";
import {
  beginSectionSchema,
  informationSectionSchema,
  contactSectionSchema,
  authorizedSectionSchema,
} from "../NewClient";
import type {
  CommonObjectType,
  FormComponentSchemaType,
} from "../../../core/AppType";

const props = defineProps({
  data: {
    type: Object as PropType<CommonObjectType>,
    required: true,
    default: { begin: { client_type: "" } },
  },
});

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

const computedCompanyType = computed(() => {
  if (
    props.data.begin &&
    props.data.begin.client_type &&
    props.data.begin.client_type !== ""
  ) {
    if (
      props.data.begin.client_type != "individual" &&
      props.data.begin.client_type != "soleProprietorship"
    ) {
      return "company";
    }
    return props.data.begin.client_type;
  }
  return null;
});

const emit = defineEmits([
  "updateFormValue",
  "updateFormArrayValue",
  "addRow",
  "deleteRow",
]);

const updateFormValue = (
  containerId: string,
  fieldId: string,
  newValue: any
) => {
  emit("updateFormValue", containerId, fieldId, newValue);
};
const updateFormArrayValue = (
  containerId: string,
  fieldId: string,
  subFieldId: string,
  newValue: any,
  rowIndex: number
) => {
  emit(
    "updateFormArrayValue",
    containerId,
    fieldId,
    subFieldId,
    newValue,
    rowIndex
  );
};
const addRow = (containerId: string, fieldId: string) => {
  emit("addRow", containerId, fieldId);
};
const deleteRow = (containerId: string, fieldId: string, rowIndex: number) => {
  emit("deleteRow", containerId, fieldId, rowIndex);
};
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
