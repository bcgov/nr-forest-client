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

    <!------ contact information section ------->
    <FormSectionTemplate
      :data="formData.state.contact"
      :sectionProps="contactSectionSchema"
    />

    <!------ add authorized individual section ------->
    <FormSectionTemplate
      :data="formData.state.authorized"
      :sectionProps="authorizedSectionSchema"
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
import { formData } from "../../../store/newclientform/FormData";

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
