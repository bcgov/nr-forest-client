<template>
  <div>
    <!------ businessType section ------->
    <FormSectionTemplate
      :data="formData.state.businessType"
      :sectionProps="computedBusinessTypeSectionSchema"
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
import { computed, onMounted, ref } from "vue";
import _ from "lodash";
import FormSectionTemplate from "./FormSectionTemplate.vue";
import { businessTypeSectionSchema } from "../formsectionschemas/BusinessTypeSectionSchema";
import { informationSectionSchema } from "../formsectionschemas/InformationSectionSchema";
import { locationSectionSchema } from "../formsectionschemas/LocationSectionSchema";
import { formData } from "../../../store/newclientform/FormData";
import axios from "axios";

const backendUrl = import.meta.env.VITE_BACKEND_URL;
axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*';

const clientTypeCodeOptions = ref([]);

onMounted(async () => {
  const activeClientTypeCodes: FormSelectOptionType[] = [];
  let response = await axios.get(`${backendUrl}/api/clients/activeClientTypeCodes`, {});
  if (Object.keys(response.data).length) {
      response.data.forEach((code: any) => {
          let clientTypeCode = {
              value: code.code,
              text: code.description
          };
          activeClientTypeCodes.push(clientTypeCode);
      });
  }
  clientTypeCodeOptions.value = activeClientTypeCodes;
});
    
const computedBusinessTypeSectionSchema = computed(() => {
  const schemaCopy = businessTypeSectionSchema
                        .content
                        .map(p => p.fieldProps.modelName == "clientType" ? 
                              {
                                ...p,
                                options: clientTypeCodeOptions.value
                              } 
                            : p);

  return {
    ...businessTypeSectionSchema,
    content: schemaCopy
  };
}); 

const computedInformationSchemaType = computed(() => {
  if (
    _.has(formData, ["state", "businessType", "clientType"]) &&
    formData.state.businessType.clientType !== ""
  ) {
    if (
      formData.state.businessType.clientType != "I" &&
      formData.state.businessType.clientType != "Z"
    ) {
      return "C";
    }
    // other types share the same schema as company
    return formData.state.businessType.clientType;
  }
  return "";
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
import type { FormSelectOptionType } from "@/core/FormType";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
