<template>

    <div style="margin: 24px">
        
        <CollapseCard title="Business Type"
                      id="businessTypeId"
                      defaultOpen>

            <Label label="What type of business are you?" 
                   :required="true" />
            <b-form-select id="businessTypeId"
                           v-model="formData.businessType.clientType" 
                           :options="clientTypeCodes" />        
            <ValidationMessages fieldId = 'businessTypeId'
                                :validationMessages="getValidationMessages"  />
        </CollapseCard>

        <CollapseCard title="Registered business" 
                      id="businessInformationId"
                      :display="displayBusinessInformation"
                      defaultOpen>
            <Label label="Start typing to search for your B.C. registered business" 
                   :required="true" />
            <Note note="The name must be the same as it is in BC Registries" />
        </CollapseCard>

    </div>
</template>

<script setup lang="ts">
import { formDataDto } from "../../dto/ApplyClientNumberDto";
import CollapseCard from "../../common/CollapseCardComponent.vue";
import Label from "../../common/LabelComponent.vue";
import Note from "../../common/NoteComponent.vue";
import ValidationMessages from "../../common/ValidationMessagesComponent.vue";

//---- Form Data ----//
let formData = ref(formDataDto);

const { data: activeClientTypeCodes } = useFetch('/api/clients/activeClientTypeCodes', { method:'get', initialData:[] });
const clientTypeCodes = computed(() => {
    return activeClientTypeCodes.value.map(conversionFn);
});

const displayBusinessInformation = computed(() => {
    return null != formData.value.businessType.clientType && 
            "I" != formData.value.businessType.clientType.value;
});

//---- Functions ----//
const submit = () => {
  console.log("formdata = ", JSON.stringify(formData));
};

const getValidationMessages = computed(() => {
    //TODO: Get this from BE. This is an example.
    /*const validationMessages = [{ 
        fieldId: "businessTypeId", 
        errorMsg: "What type of business are you? is required"}];*/
    
    const validationMessages = null;
    return validationMessages;
});
</script>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from "vue";
import { useFetch } from "@/services/forestClient.service";
import { conversionFn } from "@/services/FetchService";

export default defineComponent({
    name: "ApplyClientNumber"
});
</script>
  
<style scoped></style>