<template>
  <div style="margin: 24px">
    <wizard-wrapper-component
      title="New client application"
      subtitle="All fields are mandatory unless noted"
      v-slot="slotProps"
    >
      <wizard-tab-component
        title="Business Information"
        sub-title="Enter the business information"
        :index="0"
        :valid="false"
        :wizard="slotProps"        
      >
        <template v-slot:pre-header>
          <h4 class="form-header">Before you begin</h4>
          <ol type="1" class="bulleted-list">
            <li>A registered business must be in good standing with BC Registries</li>
            <li>You must be able to receive email to {{ submitterContact.email }}</li>
          </ol>
        <hr />
        </template>
        <template v-slot="{ validateStep, active }">
          <business-information-wizard-step
            v-if="active"
            v-model:data="formData"
            :active="active"
            @valid="validateStep"
            @update:data="update($event,'business')"
          />
        </template>
      </wizard-tab-component>

      <wizard-tab-component
        title="Address"
        :index="1"
        :valid="false"
        :wizard="slotProps"
      >
      <template v-slot:header>      
        <h3 class="inner-heading">Mailing address</h3>
        <p class="inner-text inner-text-spaced">This is the primary address you will receive mail.</p>
        <p class="inner-text inner-text-spaced">If you’d like another address, for example a seed orchard or if your street address is different from your mailing address, select the ”Add another address” button below.</p>
      </template>
      <template v-slot="{ validateStep, active }">
        <address-wizard-step
          v-if="active"
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
          @update:data="update($event,'address')"
        />
      </template>
      </wizard-tab-component>

      <wizard-tab-component
        title="Contacts"
        :index="2"
        :valid="false"
        :wizard="slotProps"        
      >
      <template v-slot:header>
        <h3 class="inner-heading">Add authorized people to the account</h3>
        <p class="inner-text inner-text-spaced">Your first name, last name and email address are from your BCeID. If they're incorrect, <a href="https://bceid.ca" target="_blank">go to BCeID</a> to update them
        before submitting your form. Be sure to add your phone number, location and role.</p>
        <p class="inner-text inner-text-spaced">To add another contact to the account, select "Add another contact" button below.</p>
      </template>
      <template v-slot="{ validateStep, active }">
        <contact-wizard-step
          v-if="active"
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
          @update:data="update($event,'contact')"
        />
        </template>
      </wizard-tab-component>

      <wizard-tab-component
        title="Review Application"
        sub-title='Review the content and make any changes by navigating through the steps above or using the "Edit" buttons in each section below.'
        :index="3"
        :valid="false"
        :wizard="slotProps"
        v-slot="{ validateStep, active }"
      >
        <review-wizard-step
          v-if="active"
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
          @update:data="update($event,'review')"
        />
      </wizard-tab-component>
    </wizard-wrapper-component>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from "vue";
import { formDataDto, type FormDataDto, type Contact } from "@/dto/ApplyClientNumberDto";

import WizardWrapperComponent from "@/components/wizard/WizardWrapperComponent.vue";
import WizardTabComponent from "@/components/wizard/WizardTabComponent.vue";

import BusinessInformationWizardStep from "@/pages/applyform/BusinessInformationWizardStep.vue";
import AddressWizardStep from "@/pages/applyform/AddressWizardStep.vue";
import ContactWizardStep from "@/pages/applyform/ContactWizardStep.vue";
import ReviewWizardStep from "@/pages/applyform/ReviewWizardStep.vue";

const props = defineProps({
  submitterInformation: {
    type: Object,
    required: true,
  },
});

const submitterContact: Contact = {
    locationNames: [],
    contactType: { value: "", text: "" },
    phoneNumber: "",
    firstName: props.submitterInformation.submitterFirstName,
    lastName: props.submitterInformation.submitterLastName,
    email: props.submitterInformation.submitterEmail,
  };

//---- Form Data ----//
let formData = reactive<FormDataDto>({...formDataDto,location:{ addresses:formDataDto.location.addresses,contacts:[submitterContact]} });

function getObjectDiff<T>(obj1: T, obj2: T): Partial<T> {
  function isObject(value: any): boolean {
    return typeof value === "object" && value !== null;
  }

  const diff: Partial<T> = {};

  for (let key in obj1) {
    if (obj1.hasOwnProperty(key)) {
      if (isObject(obj1[key]) && isObject(obj2[key])) {
        const nestedDiff = getObjectDiff(obj1[key], obj2[key]);
        if (Object.keys(nestedDiff).length > 0) {
          diff[key] = nestedDiff;
        }
      } else if (obj1[key] !== obj2[key]) {
        diff[key] = obj2[key];
      }
    }
  }

  return diff;
}

watch([formData],() => {});


const update = (value: any,who:string) => {
}

</script>

<style scoped>
</style>
