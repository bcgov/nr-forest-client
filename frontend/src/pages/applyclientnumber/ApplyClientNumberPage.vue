<template>

    <div style="margin: 24px">
        
        <CollapseCard title="Business Type"
                      id="clientTypeId"
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
                          @updateValue="formData.businessInformation.businessName = $event;
                                        populateBusinessList($event);
                                        filterSearchData($event)" />
            <Note note="The name must be the same as it is in BC Registries" />
            <ValidationMessages fieldId = 'businessNameId'
                                :validationMessages="validationMessages" />
        </CollapseCard>

        <CollapseCard title="Mailing address" 
                      id="mailingAddressId"
                      :display="displayCommonSections"
                      defaultOpen>

            <span>
                <strong>This information is from 
                        <a href="https://www.bcregistry.ca/business/auth/home/decide-business">BC Registries</a>. 
                        If it's incorrect, go to BC Registries to update it before continuing.</strong>
            </span>

            <br /><br />
            <AddressSection id="addressListId" 
                            :addresses="formData.location.addresses"
                            :validationMessages="validationMessages" />
        </CollapseCard>

        <CollapseCard title="Form submitter information" 
                      id="submitterInformationId"
                      :display="displayCommonSections"
                      defaultOpen>

            <span>
                <strong>This information is from your BCeID. If it's incorrect, go to BCeID login page to update it before submitting your form.</strong>
            </span>>

            <br /><br />
            <b-row>
                <b-col cols="3">
                    <Label label="First name" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.submitterFirstName" />
                    <ValidationMessages fieldId = 'submitterFirstNameId'
                                        :validationMessages="validationMessages"  />
                </b-col>
                <b-col cols="3">
                    <Label label="Last name" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.submitterLastName" />
                    <ValidationMessages fieldId = 'submitterLastNameId'
                                        :validationMessages="validationMessages"  />
                </b-col>
                <b-col cols="3">
                    <Label label="Phone number" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.submitterPhoneNumber" />
                    <ValidationMessages fieldId = 'submitterPhoneNumberId'
                                        :validationMessages="validationMessages"  />
                </b-col>
                <b-col cols="3">
                    <Label label="Email address" 
                           :required="true" />
                    <b-form-input v-model="formData.submitterInformation.submitterEmail" />
                    <ValidationMessages fieldId = 'submitterEmailId'
                                        :validationMessages="validationMessages"  />
                </b-col>
            </b-row>

        </CollapseCard>

        <b-button class="chefsBlue"
                  @click="submit()">
            Submit
        </b-button>

    </div>
</template>

<script setup lang="ts">
import { formDataDto } from "../../dto/ApplyClientNumberDto";
import CollapseCard from "../../common/CollapseCardComponent.vue";
import Label from "../../common/LabelComponent.vue";
import Note from "../../common/NoteComponent.vue";
import Autocomplete from "../../common/AutocompleteComponent.vue";
import ValidationMessages from "../../common/ValidationMessagesComponent.vue";
import AddressSection from "./AddressSectionComponent.vue";

axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*';
const forestClientBase = import.meta.env.VITE_BACKEND_URL;

//---- Form Data ----//
let formData = ref(formDataDto);

//--- Initializing the Addresses array ---//
addNewAddress(formDataDto.location.addresses);

const { data: activeClientTypeCodes } = useFetch('/api/clients/activeClientTypeCodes', { method:'get', initialData:[] });
const clientTypeCodes = computed(() => {
    return activeClientTypeCodes.value.map(conversionFn);
});

let businessNames = Array<CodeDescrType>();
async function populateBusinessList(event: any) {
    if (event.length >= 3) {
        const encodedBusinessName = encodeURIComponent(event);
        const response = await axios.get(forestClientBase + '/api/orgbook/name/' + encodedBusinessName);
        businessNames = response.data.map(conversionFn);
    }
};

const displayBusinessInformation = computed(() => {
    return null !== formData.value.businessType.clientType && 
            formData.value.businessType.clientType.value.length > 0 &&
            "I" !== formData.value.businessType.clientType.value &&
            "Z" !== formData.value.businessType.clientType.value;
});

const displayCommonSections = computed(() => {
    if (null !== formData.value.businessType.clientType && 
            "I" === formData.value.businessType.clientType.value) {
        //TODO
        return true;
    }
    else { 
        if (null !== formData.value.businessType.clientType && 
            null !== formData.value.businessInformation && 
            null !== formData.value.businessInformation.businessName &&
            "" !== formData.value.businessInformation.businessName) {
            return true;
        }
        else {
            return false;
        }
    }
});

function filterSearchData(event: any) {
    const filterValue = event.toLowerCase();
    const filteredSearchData = businessNames.filter(p => p.text.toLowerCase().includes(filterValue));
    if (filteredSearchData.length === 1) {
        formData.value.businessInformation.incorporationNumber = filteredSearchData[0].value.value;
    }
    return filteredSearchData;
}

//---- Functions ----//
let validationMessages = ref([] as ValidationMessageType[]);

//TODO: Get this from BE. This is an example.
const validationMessages2 = [
    { 
        fieldId: "clientTypeId", 
        errorMsg: "What type of business are you? is required"
    },
    { 
        fieldId: "businessNameId", 
        errorMsg: "Business name is required"
    },
    { 
        fieldId: "countryId0", 
        errorMsg: "Country is required"
    },
    {
        fieldId: "contactTypeId0",
        errorMsg: "Role is required"
    }
] as ValidationMessageType[];

function submit(): void {
  //TODO: Call API to persist data and get validation messages
  validationMessages.value = validationMessages2;
  console.log("validationMessages = ", JSON.stringify(validationMessages));
  console.log("formData = ", JSON.stringify(formData.value));
}

</script>

<script lang="ts">
import { computed, defineComponent, ref } from "vue";
import { addNewAddress, useFetch } from "../../services/ForestClientService";
import { conversionFn } from "../../services/FetchService";
import axios from "axios";
import type { CodeDescrType, ValidationMessageType } from "../../core/CommonTypes";

export default defineComponent({
    name: "ApplyClientNumber"
});
</script>
  
<style scoped>
  .chefsBlue {
    background: #003366;
  }
</style>