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
      :sectionProps="computedLocationSectionSchema"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import _ from "lodash";
import FormSectionTemplate from "@/pages/applynewclient/formsections/FormSectionTemplate.vue";
import { businessTypeSectionSchema } from "@/pages/applynewclient/formsectionschemas/BusinessTypeSectionSchema";
import { informationSectionSchema } from "@/pages/applynewclient/formsectionschemas/InformationSectionSchema";
import { locationSectionSchema } from "@/pages/applynewclient/formsectionschemas/LocationSectionSchema";
import { formData } from "@/store/newclientform/FormData";
import { useFetch } from "@/services/forestClient.service";
import { useBusinessNameIdAutoComplete, useCountryIdAutoComplete, conversionFn } from "@/services/FetchService.ts";


//We call it here to enable autocomplete
useBusinessNameIdAutoComplete();
//We will update the provinces based on the province update
const { data: provinceCodes } = useCountryIdAutoComplete();

const { data: activeClientTypeCodes } = useFetch('/api/clients/activeClientTypeCodes', { method:'get', initialData:[] });
const { data: countryCodes } = useFetch('/api/clients/activeCountryCodes?page=0&size=250', { method:'get', initialData:[] });
const { data: activeContactTypeCodes } = useFetch('/api/clients/activeContactTypeCodes', { method:'get', initialData:[] });

const computedBusinessTypeSectionSchema = computed(() => {
  const schemaCopy = businessTypeSectionSchema
                        .content
                        .map(p => p.fieldProps.modelName == "clientType" ? 
                              {
                                ...p,
                                options: activeClientTypeCodes.value.map(conversionFn)
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

const computedLocationSectionSchema = computed(() => {
  const deepSearch = (target) => {
    if (typeof target === 'object') {
      for (let key in target) {
            
        if (typeof target[key] === 'object') {
          if (key === 'subfields') {        
            const newsubfields = target[key]
                                  .map((p) =>
                                    p.fieldProps.modelName == "country"
                                      ? { ...p, options: countryCodes.value.map(conversionFn) }
                                      : p
                                  )
                                  .map((p) =>
                                    p.fieldProps.modelName == "province"
                                      ? { ...p, options: provinceCodes.value.map(conversionFn) }
                                      : p
                                  )
                                  .map((p) =>
                                    p.fieldProps.modelName == "contactType"
                                      ? { ...p, options: activeContactTypeCodes.value.map(conversionFn) }
                                      : p
                                  );

            target[key] = newsubfields;     
          }
          deepSearch(target[key]);
        }       
      }
    }
    return target;
  }

  const newSchema = deepSearch(locationSectionSchema);
  return { ...locationSectionSchema, newSchema};
});
</script>

<script lang="ts">
import { defineComponent } from "vue";
export default defineComponent({
  name: "ApplyNewClientPage",
});
</script>

<style scoped></style>
