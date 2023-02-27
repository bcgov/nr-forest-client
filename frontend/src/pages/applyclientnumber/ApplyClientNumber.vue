<template>

    <div style="margin: 24px">
        
        <CollapseCard title="Business Type"
                      id="businessTypeId"
                      defaultOpen>

            <Label label="What type of business are you?" 
                   :required="true" />
            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4">
                <b-form-select id="businessTypeId"
                               v-model="formData.businessType.clientType" 
                               :options="clientTypeCodes" />        
            </div>
            <ValidationMessages fieldId = 'businessTypeId'
                                :validationMessages="validationMessages"  />
        </CollapseCard>

        <CollapseCard title="Registered business" 
                      id="businessInformationId"
                      :display="displayBusinessInformation"
                      defaultOpen>
            <Label label="Start typing to search for your B.C. registered business" 
                   :required="true" />
       
            <!-- TODO: Value should be an object. It displays the name, but on the back, this should contain:
            the code, the name, the id, and the address (for BC registry) -->
            <Autocomplete id="businessNameId"
                          :value="formData.businessInformation.businessName"
                          :searchData="businessNames"
                          datalistId="businessNameListId"
                          @updateValue="formData.businessInformation.businessName = $event" />
            <Note note="The name must be the same as it is in BC Registries" />
        </CollapseCard>

        <CollapseCard title="Mailing address" 
                      id="mailingAddressId"
                      :display="displayCommonSections"
                      defaultOpen>

            <label>
                <strong>This information is from BC Registries. If it's incorrect, go to BC Registries to update it before continuing.</strong>
            </label>
        </CollapseCard>

        <CollapseCard title="Form submitter information" 
                      id="submitterInformationId"
                      :display="displayCommonSections"
                      defaultOpen>

            <label>
                <strong>This information is from your BCeID. If it's incorrect, go to BCeID login page to update it before submitting your form.</strong>
            </label>

            <br /><br />
            <b-row>
                <b-col cols="3">
                    <Label label="First name" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.firstName" />
                    <ValidationMessages fieldId = 'submitterFirstNameId'
                                        :validationMessages="validationMessages"  />
                </b-col>
                <b-col cols="3">
                    <Label label="Last name" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.lastName" />
                    <ValidationMessages fieldId = 'submitterLastNameId'
                                        :validationMessages="validationMessages"  />
                </b-col>
                <b-col cols="3">
                    <Label label="Phone number" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.phoneNumber" />
                    <ValidationMessages fieldId = 'submitterPhoneNumberId'
                                        :validationMessages="validationMessages"  />
                </b-col>
                <b-col cols="3">
                    <Label label="Email address" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.email" />
                </b-col>
            </b-row>

        </CollapseCard>
    </div>
</template>

<script setup lang="ts">
import { formDataDto } from "../../dto/ApplyClientNumberDto";
import CollapseCard from "../../common/CollapseCardComponent.vue";
import Label from "../../common/LabelComponent.vue";
import Note from "../../common/NoteComponent.vue";
import Autocomplete from "../../common/AutocompleteComponent.vue";
import ValidationMessages from "../../common/ValidationMessagesComponent.vue";

//---- Form Data ----//
let formData = ref(formDataDto);

const { data: activeClientTypeCodes } = useFetch('/api/clients/activeClientTypeCodes', { method:'get', initialData:[] });
const clientTypeCodes = computed(() => {
    return activeClientTypeCodes.value.map(conversionFn);
});

//TODO: Hardcoded for now. Use API.
const businessNames = [
  {
    "code": "FM0234310",
    "name": "PACIFIC GEOTECH SYSTEMS"
  },
  {
    "code": "BC0605515",
    "name": "PACIFIC GEOTECH SYSTEMS LTD."
  }
];

const displayBusinessInformation = computed(() => {
    return null != formData.value.businessType.clientType && 
            "I" != formData.value.businessType.clientType.value &&
            "Z" != formData.value.businessType.clientType.value;
});

const displayCommonSections = computed(() => {
    if (null != formData.value.businessType.clientType && 
            "I" == formData.value.businessType.clientType.value) {
        //TODO
        return true;
    }
    else { 
        //TODO: Change businessName as Object
        if (null != formData.value.businessType.clientType && 
            null != formData.value.businessInformation && 
            null != formData.value.businessInformation.businessName) {
            return true;
        }
        else {
            return false;
        }
    }
});

//---- Functions ----//
const submit = () => {
  console.log("formdata = ", JSON.stringify(formData));
};

const validationMessages = computed(() => {
    //TODO: Get this from BE. This is an example.
    /*const validationMessages = [
        { 
            fieldId: "businessTypeId", 
            errorMsg: "What type of business are you? is required"
        },
        { 
            fieldId: "submitterFirstNameId", 
            errorMsg: "First Name is required"
        }
    ];*/
    
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