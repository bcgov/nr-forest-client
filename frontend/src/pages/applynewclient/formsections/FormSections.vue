<template>
  <div>
    <!------ begin section ------->
    <FormSectionTemplate
      :data="data.begin"
      :containerProps="beginSectionSchema"
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
      :containerProps="informationSectionSchema[computedCompanyType]"
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
      :containerProps="contactSectionSchema"
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
      :containerProps="authorizedSectionSchema"
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
import { computed, ref } from "vue";
import type { PropType } from "vue";
import FormSectionTemplate from "./FormSectionTemplate.vue";
import {
  beginSectionSchema,
  informationSectionSchema,
  contactSectionSchema,
  authorizedSectionSchema,
} from "../NewClient";
import type {
  CommonObjectType,
  FormValidationResultType,
} from "../../../core/AppType";

const props = defineProps({
  data: {
    type: Object as PropType<CommonObjectType>,
    required: true,
    default: { begin: { client_type: "" } },
  },
  validationResult: Object as PropType<FormValidationResultType>,
});

// const computedBeginSectionSchema = computed(() => {
//   if (props.validationResult && props.validationResult.begin) {
//     const beginSchemaCopy = JSON.parse(JSON.stringify(beginSectionSchema));
//     beginSchemaCopy.forEach((field) => {});
//     props.validationResult.begin.forEach((errorField) => {});
//   }
// });

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

const updateFormValue = (containerId, fieldId, newValue) => {
  emit("updateFormValue", containerId, fieldId, newValue);
};
const updateFormArrayValue = (
  containerId,
  fieldId,
  columnId,
  value,
  rowIndex
) => {
  emit("updateFormArrayValue", containerId, fieldId, columnId, value, rowIndex);
};
const addRow = (containerId, fieldId) => {
  emit("addRow", containerId, fieldId);
};
const deleteRow = (containerId, fieldId, rowIndex) => {
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
