<template>

    <div style="margin: 24px">
        
        <CollapseCard title="Business Type"
                      id="clientTypeId"
                      defaultOpen>

            <Label label="What type of business are you?" 
                   :required="true" />
            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4">
                <b-form-select id="businessInformationId"
                               v-model="formData.businessInformation.clientType" 
                               :options="clientTypeCodes" />  
            </div>
            <ValidationMessages fieldId = 'businessInformation.clientType'
                                :validationMessages="validationMessages" />
        </CollapseCard>

        <CollapseCard title="Registered business" 
                      id="businessInformationId"
                      defaultOpen>
            <Label label="Please choose one of the options below" 
                   tooltip="Go to <a href='https://www.bcregistry.ca/business/auth/home/decide-business' target='_blank'>BC Registries and Online Services</a>
                            to read more about registered businesses. Sole proprietorships can be registered and unregistered."
                   id="clientTypeLabelId"
                   :required="true" />

            <b-form-group>
                <b-form-radio v-model="formData.businessInformation.businessType" 
                              value="R">
                    I have a BC registered business (corporation, sole proprietorship, society, etc.)
                </b-form-radio>
                <b-form-radio v-model="formData.businessInformation.businessType" 
                              value="U">I have an unregistered sole proprietorship
                </b-form-radio>
            </b-form-group>
            <ValidationMessages fieldId = 'businessInformation.businessType'
                                :validationMessages="validationMessages" />

            <!-- TODO: Ask Andrea about this logic -->
            <div v-if="formData.businessInformation.businessType == 'R'"></div>

            <Label label="Start typing to search for your B.C. registered business" 
                   tooltip="If your business name isn't in the list, go to BC Registries to confirm."
                   id="businessNameLabelId"
                   :required="true" />
       
            <Autocomplete id="businessNameId"
                          :value="formData.businessInformation.businessName"
                          :searchData="businessNames"
                          datalistId="businessNameListId"
                          @updateValue="formData.businessInformation.businessName = $event;                                        
                                        populateBusinessList($event);
                                        filterSearchData($event)" />
            <Note note="The name must be the same as it is in BC Registries" />
            <ValidationMessages fieldId = 'businessInformation.businessName'
                                :validationMessages="validationMessages" />
                                
            <span v-if="'' !== formData.businessInformation.incorporationNumber &&
                        '' !== formData.businessInformation.goodStanding &&
                        !formData.businessInformation.goodStanding">
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
                                  v-model="formData.submitterInformation.submitterFirstName" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterFirstName'
                                        :validationMessages="validationMessages" />
                </b-col>
                <b-col cols="3">
                    <Label label="Last name" 
                           :required="true" />
                    <b-form-input id="submitterLastNameId"
                                  v-model="formData.submitterInformation.submitterLastName" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterLastName'
                                        :validationMessages="validationMessages" />
                </b-col>
                <b-col cols="3">
                    <Label label="Phone number" 
                           :required="true" />
                    <b-form-input id="submitterPhoneNumberId"
                                  v-model="formData.submitterInformation.submitterPhoneNumber"
                                  v-mask="'##########'" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterPhoneNumber'
                                        :validationMessages="validationMessages" />
                </b-col>
                <b-col cols="3">
                    <Label label="Email address" 
                           :required="true" />
                    <b-form-input id="submitterEmailId"
                                  v-model="formData.submitterInformation.submitterEmail" />
                    <ValidationMessages fieldId = 'submitterInformation.submitterEmail'
                                        :validationMessages="validationMessages" />
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
import { formDataDto } from "@/dto/ApplyClientNumberDto";
import CollapseCard from "@/common/CollapseCardComponent.vue";
import Label from "@/common/LabelComponent.vue";
import Note from "@/common/NoteComponent.vue";
import Autocomplete from "@/common/AutocompleteComponent.vue";
import ValidationMessages from "@/common/ValidationMessagesComponent.vue";
import AddressSection from "@/pages/applyclientnumber/AddressSectionComponent.vue";

//---- Form Data ----//
let formData = ref(formDataDto);

//--- Initializing the Addresses array ---//
addNewAddress(formDataDto.location.addresses);

const addressDataRef = ref([]);

const { data: activeClientTypeCodes } = useFetch('/api/clients/activeClientTypeCodes', { method:'get', initialData:[] });
const clientTypeCodes = computed(() => {
    return activeClientTypeCodes.value.map(conversionFn);
});

const originalBusinessNames = ref([]);

const businessNames = computed(() => {
    return originalBusinessNames.value.map(conversionFn);
});

async function populateBusinessList(event: any) {
    if (event.length >= 3) {
        const encodedBusinessName = encodeURIComponent(event);        
        useFetchTo(`/api/clients/name/${encodedBusinessName}`,originalBusinessNames, { method:'get' });
        filterSearchData(event);
    }
};

function filterSearchData(event: any) {
    const filteredSearchDataValues = businessNames.value.filter(p => p.text.toLowerCase() === event.toLowerCase());

    if (filteredSearchDataValues.length === 1) {    
        formData.value.businessInformation.incorporationNumber = filteredSearchDataValues[0].value.value;    
        useFetchTo(`/api/clients/${formData.value.businessInformation.incorporationNumber}`, addressDataRef, { method:'get' });
    }
    else {
        formData.value.businessInformation.incorporationNumber = "";
        formData.value.businessInformation.goodStanding = "";
    }    
    return filteredSearchDataValues;
}

watch(
   [addressDataRef], 
   () => {         
        formData.value.location.addresses = addressDataRef.value.addresses;
        formData.value.businessInformation.goodStanding = addressDataRef.value.goodStanding.toString();
    }
);

const displayCommonSections = computed(() => {
    if (null !== formData.value.businessInformation.clientType && 
            "I" === formData.value.businessInformation.clientType.value) {
        //TODO
        return true;
    }
    else { 
        if (null !== formData.value.businessInformation.clientType && 
            null !== formData.value.businessInformation && 
            null !== formData.value.businessInformation.incorporationNumber &&
            "" !== formData.value.businessInformation.incorporationNumber &&
            "true" === formData.value.businessInformation.goodStanding) {
            return true;
        }
        else {
            return false;
        }
    }
});

//---- Functions ----//
let validationMessages = ref([] as ValidationMessageType[]);

const { response, error, fetch: persistValidateData } = usePost('/api/clients/submissions', formData.value, { skip: true });

watch(
    [response],
    () => {
        if (201 === response.value.status) {
            console.log('submission created, id ', response.value.headers['x-sub-id']);
        }        
    }
)

watch(
    [error],
    () => {
        if (400 === error.value.status) {
            validationMessages.value = error.value.data;
        }
    }
)

function submit(): void {
    persistValidateData();
}

const deepFormDataCopy = computed(() => cloneDeep(formData));
    watch(deepFormDataCopy, (newObj, oldObj) => {
        if (JSON.stringify(newObj.value) !== JSON.stringify(oldObj.value) &&
            validationMessages.value.length > 0) {
            submit();   
        }
}, { deep: true });

</script>

<script lang="ts">
import { computed, defineComponent, ref, watch } from "vue";
import { addNewAddress, useFetch, useFetchTo, usePost } from "@/services/ForestClientService";
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