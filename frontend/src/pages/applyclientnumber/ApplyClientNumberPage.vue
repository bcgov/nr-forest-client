<template>
    <div style="margin: 24px">
        <!-- Global error messages for hidden fields -->
        <ValidationMessages fieldId = 'businessInformation.incorporationNumber'
                            :validationMessages="validationMessages"
                            :modelValue="formData.businessInformation.incorporationNumber" />
        <ValidationMessages fieldId = 'businessInformation.clientType'
                            :validationMessages="validationMessages"
                            :modelValue="formData.businessInformation.clientType" />
        <ValidationMessages fieldId = 'apiErrorLblId'
                            :validationMessages="validationMessages"
                            :modelValue="formData.businessInformation.clientType" />
        <!--  -->

        <label>
            To use this form:<br />
            &nbsp;&nbsp;&nbsp;1. A registered business must be in good standing with BC Registries.<br />
            &nbsp;&nbsp;&nbsp;2. You must be able to receive email.<br /><br />
            All fields are mandatory unless noted.
        </label>
        <br /><br />

        <CollapseCard title="Registered business" 
                      id="businessInformationId"
                      defaultOpen>
            <Label label="Choose one these options:" 
                   id="clientTypeLabelId" />

            <b-form-group @change="getBusinessName">
                <b-form-radio v-model="formData.businessInformation.businessType" 
                              value="R">
                    I have a BC registered business (corporation, sole proprietorship, society, etc.)
                </b-form-radio>
                <b-form-radio v-model="formData.businessInformation.businessType" 
                              value="U">I have an unregistered sole proprietorship
                </b-form-radio>
            </b-form-group>
            <ValidationMessages fieldId = 'businessInformation.businessType'
                                :validationMessages="validationMessages"
                                :modelValue="formData.businessInformation.businessType" />

            <div v-if="formData.businessInformation.businessType == 'R'">
                <Label label="Start typing to search for your B.C. registered business"
                       id="businessNameLabelId" />
        
                <Autocomplete id="businessNameId"
                              :value="formData.businessInformation.businessName"
                              :validationMessages="validationMessages"
                              fieldId = 'businessInformation.businessName'
                              :searchData="businessNames"
                              datalistId="businessNameListId"
                              @updateValue="formData.businessInformation.businessName = $event;                                        
                                            populateBusinessList($event);
                                            filterSearchData($event)" />
                <Note note="The name must be the same as in BC Registries" />
            </div>

            <div v-if="formData.businessInformation.businessType == 'U'">
                <Label label="Unregistered sole proprietorship" />
                {{ formData.businessInformation.businessName }}
            </div>

            <ValidationMessages fieldId = 'businessInformation.businessName'
                                :validationMessages="validationMessages"
                                :modelValue="formData.businessInformation.businessName" />

            <span v-if="'' !== formData.businessInformation.incorporationNumber &&
                        !formData.businessInformation.goodStandingInd &&
                        '' !== formData.businessInformation.goodStandingInd">
                <strong>Your business is not in good standing with BC Registries. You must go to
                        <a href="https://www.bcregistry.ca/business/auth/home/decide-business" target="_blank">BC Registries </a>
                        to resolve this before you can apply for a client number.
                </strong>
            </span> 
        </CollapseCard>

        <CollapseCard title="Mailing address" 
                      id="mailingAddressId"
                      :display="displayCommonSections"
                      defaultOpen>

            <span>
                <strong>This information is from 
                        <a href="https://www.bcregistry.ca/business/auth/home/decide-business" target="_blank">BC Registries</a>. 
                        If it's incorrect, go to BC Registries to update it before continuing.</strong>
            </span>

            <br /><br />
            <AddressSection id="addressListId" 
                            :addresses="formData.location.addresses"
                            :validationMessages="validationMessages" />
            
            <ValidationMessages :fieldId="'location.addresses'"
                                :validationMessages="validationMessages"
                                :modelValue="formData.location.addresses[0]" />
        </CollapseCard>

        <CollapseCard title="Form submitter information" 
                      id="submitterInformationId"
                      :display="displayCommonSections"
                      defaultOpen>

            <span>
                <strong>This information is from your BCeID. If it's incorrect, go to BCeID login page to update it before submitting your form.</strong>
            </span>

            <br /><br />
            <b-row>
                <b-col cols="3">
                    <Label label="First name" 
                           :required="true" />
                    <b-form-input id="submitterFirstNameId"
                                  v-model="formData.submitterInformation.submitterFirstName"
                                  :disabled="formData.submitterInformation.submitterFirstName !== ''" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterFirstName'
                                        :validationMessages="validationMessages"
                                        :modelValue="formData.submitterInformation.submitterFirstName" />
                </b-col>
                <b-col cols="3">
                    <Label label="Last name" 
                           :required="true" />
                    <b-form-input id="submitterLastNameId"
                                  v-model="formData.submitterInformation.submitterLastName"
                                  :disabled="formData.submitterInformation.submitterLastName !== ''" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterLastName'
                                        :validationMessages="validationMessages"
                                        :modelValue="formData.submitterInformation.submitterLastName" />
                </b-col>
                <b-col cols="3">
                    <Label label="Phone number" 
                           :required="true" />
                    <b-form-input id="submitterPhoneNumberId"
                                  v-model="formData.submitterInformation.submitterPhoneNumber"
                                  v-mask="'##########'"
                                  :disabled="formData.submitterInformation.submitterPhoneNumber !== ''" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterPhoneNumber'
                                        :validationMessages="validationMessages"
                                        :modelValue="formData.submitterInformation.submitterPhoneNumber" />
                </b-col>
                <b-col cols="3">
                    <Label label="Email address" 
                           :required="true" />
                    <b-form-input id="submitterEmailId"
                                  v-model="formData.submitterInformation.submitterEmail"
                                  :disabled="formData.submitterInformation.submitterEmail !== ''" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterEmail'
                                        :validationMessages="validationMessages"
                                        :modelValue="formData.submitterInformation.submitterEmail" />
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
import { formDataDto, type Address } from "@/dto/ApplyClientNumberDto";
import CollapseCard from "@/common/CollapseCardComponent.vue";
import Label from "@/common/LabelComponent.vue";
import Note from "@/common/NoteComponent.vue";
import Autocomplete from "@/common/AutocompleteComponent.vue";
import ValidationMessages from "@/common/ValidationMessagesComponent.vue";
import AddressSection from "@/pages/applyclientnumber/AddressSectionComponent.vue";

const props = defineProps({
    submitterInformation: {
        type: Object,
        required: true
    }
  });

//---- Form Data ----//
let formData = ref(formDataDto);

//--- Initializing the Addresses array ---//
addNewAddress(formDataDto.location.addresses);

//--- Initializing the validation error ---//
let validationMessages = ref([] as ValidationMessageType[]);

const addressDataRef = ref([]);
const originalBusinessNames = ref([]);

const businessNames = computed(() => {
    return originalBusinessNames.value.map(conversionFn);
});

async function populateBusinessList(event: any) {
    if (event.length >= 3) {
        const encodedBusinessName = encodeURIComponent(event);        
        useFetchTo(`/api/clients/name/${encodedBusinessName}`, originalBusinessNames, { method:'get' });
        filterSearchData(event);
    }
};

function filterSearchData(event: any) {
    const filteredSearchDataValues = businessNames.value.filter(p => p.text.toLowerCase() === event.toLowerCase());

    if (filteredSearchDataValues.length === 1) {    
        formData.value.businessInformation.incorporationNumber = filteredSearchDataValues[0].value.value;   
        formData.value.businessInformation.legalType = filteredSearchDataValues[0].legalType;
        formData.value.businessInformation.clientType = retrieveClientType(formData.value.businessInformation.legalType);

        const {error: detailsResponse } = useFetchTo(`/api/clients/${formData.value.businessInformation.incorporationNumber}`, addressDataRef, { method:'get' });
        watch(
            [detailsResponse],
            () => {                
                if (409 === detailsResponse.value.response.status) {
                    if (!validationMessages.value.some(msg => msg.fieldId === 'businessInformation.businessName')) {
                        validationMessages.value.push({fieldId: 'businessInformation.businessName',errorMsg: detailsResponse.value.response.data})
                    }
                }
            }
        )
    }
    else {
        resetPartialBusinessInformation();
    }    
    return filteredSearchDataValues;
}

const displayCommonSections = computed(() => {
    if ("" === formData.value.businessInformation.businessType || 
        "" === formData.value.businessInformation.legalType) {
        return false;
    }
    else if ("R" === formData.value.businessInformation.businessType && (
             "" === formData.value.businessInformation.incorporationNumber ||
             "false" === formData.value.businessInformation.goodStandingInd)) {
        return false;
    }
    else {
        return true;
    }
});

function getBusinessName() {
    if ("U" === formData.value.businessInformation.businessType) {
        console.log("props.submitterInformation.bceidBusinessName = " + props.submitterInformation.bceidBusinessName);
        formData.value.businessInformation.incorporationNumber = "";
        formData.value.businessInformation.businessName = props.submitterInformation.bceidBusinessName;
        formData.value.businessInformation.legalType = "SP";
        formData.value.businessInformation.clientType = retrieveClientType(formData.value.businessInformation.legalType);
    }
    else {
        resetPartialBusinessInformation();
    }
};

function resetPartialBusinessInformation() {
    formData.value.businessInformation.businessName = "";
    formData.value.businessInformation.incorporationNumber = "";
    formData.value.businessInformation.businessName = "";
    formData.value.businessInformation.legalType = "";
    formData.value.businessInformation.goodStandingInd = "";
    formData.value.businessInformation.clientType = "";
};

function submit(): void {
    persistValidateData();
};

function retrieveClientType(legalType: string): string {
    switch (legalType) {
        case "A":
        case "B":
        case "BC":
        case "C":
        case "CP":
        case "EPR":
        case "FOR":
        case "LIC":
        case "REG":
            return "C";
        case "S":
        case "XS":
            return "S";
        case "XCP":
            return "A";
        case "SP":
            return "I";
        case "GP":
            return "P";
        case "LP":
        case "XL":
        case "XP":
            return "L";
        default:
            throw new Error("Unknown Legal Type.");
    }
};

const { response, error, fetch: persistValidateData } = usePost('/api/clients/submissions', formData.value, props.submitterInformation, { skip: true });

watch(
    [response],
    () => {
        if (201 === response.value.status) {
            console.log('submission created, id ', response.value.headers['x-sub-id']);
        }        
    }
);

watch(
    [error],
    () => {
        if (400 === error.value.status) {
            validationMessages.value = error.value.data;
        }
    }
);

watch(
   [addressDataRef], 
   () => {         
        formData.value.location.addresses = addressDataRef.value.addresses;
        formData.value.businessInformation.goodStandingInd = addressDataRef.value.goodStanding.toString();
    }
);

</script>

<script lang="ts">
import { computed, defineComponent, ref, watch } from "vue";
import { addNewAddress, useFetchTo, usePost } from "@/services/ForestClientService";
import { conversionFn } from "@/services/FetchService";
import type { ValidationMessageType } from "@/core/CommonTypes";
import { cloneDeep } from "lodash";

export default defineComponent({
    name: "ApplyClientNumber"
});
</script>
  
<style scoped>
  .chefsBlue {
    background: #003366;
  }
</style>