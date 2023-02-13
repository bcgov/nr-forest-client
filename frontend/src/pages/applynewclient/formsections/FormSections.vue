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
import { computed, onMounted, ref, watch } from "vue";
import _ from "lodash";
import FormSectionTemplate from "./FormSectionTemplate.vue";
import { businessTypeSectionSchema } from "../formsectionschemas/BusinessTypeSectionSchema";
import { informationSectionSchema } from "../formsectionschemas/InformationSectionSchema";
import { locationSectionSchema } from "../formsectionschemas/LocationSectionSchema";
import { formData } from "../../../store/newclientform/FormData";
import { useFetch } from "../../../services/forestClient.service";

const conversionFn = (code: any) => {return {value: code.code, text: code.description}};
const { data } = useFetch('/api/clients/activeClientTypeCodes',{method:'get',initialData:[]});

const computedBusinessTypeSectionSchema = computed(() => {
  const schemaCopy = businessTypeSectionSchema
                        .content
                        .map(p => p.fieldProps.modelName == "clientType" ? 
                              {
                                ...p,
                                options: data.value.map(conversionFn)
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
      formData.state.businessType.clientType != "individual" &&
      formData.state.businessType.clientType != "soleProprietorship"
    ) {
      return "company";
    }
    // other types share the same schema as company
    return formData.state.businessType.clientType;
  }
  return "";
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
import { FormSelectOptionType } from "../../../core/FormType";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
