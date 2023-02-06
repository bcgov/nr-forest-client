<template>
  <div>
    <!------ begin section ------->
    <FormSectionTemplate
      :data="formData.state.begin"
      :sectionProps="beginSectionSchema"
    />

    <!------ company/individual information section ------->
    <FormSectionTemplate
      v-if="computedInformationSchemaType !== ''"
      :data="formData.state.information"
      :sectionProps="informationSectionSchema[computedInformationSchemaType]"
    />

    <!------ location information section ------->
    <FormSectionTemplate
      :data="formData.state.location"
      :sectionProps="locationSectionSchema"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import _ from "lodash";
import FormSectionTemplate from "./FormSectionTemplate.vue";
import { beginSectionSchema } from "../formsectionschemas/BeginSectionSchema";
import { informationSectionSchema } from "../formsectionschemas/InformationSectionSchema";
import { locationSectionSchema } from "../formsectionschemas/LocationSectionSchema";
import { formData } from "../../../store/newclientform/FormData";

// based on client type, show different schema contenct for the information section
const computedInformationSchemaType = computed(() => {
  if (
    _.has(formData, ["state", "begin", "clientType"]) &&
    formData.state.begin.clientType !== ""
  ) {
    if (
      formData.state.begin.clientType != "individual" &&
      formData.state.begin.clientType != "soleProprietorship"
    ) {
      return "company";
    }
    // other types share the same schema as company
    return formData.state.begin.clientType;
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
