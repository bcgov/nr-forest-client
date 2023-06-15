<template>
  <div style="margin: 24px">
    <wizard-wrapper-component
      title="New client application"
      subtitle="All fields are mandatory unless noted"
      v-slot="slotProps"
    >
      <wizard-tab-component
        title="Business Information"
        :index="0"
        :valid="false"
        :wizard="slotProps"
        v-slot="{ validateStep, active }"
      >
        <business-information-wizard-step
          v-if="active"
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
          @update:data="update($event,'business')"
        />
      </wizard-tab-component>

      <wizard-tab-component
        title="Address"
        :index="1"
        :valid="false"
        :wizard="slotProps"
        v-slot="{ validateStep, active }"
      >
        <address-wizard-step
          v-if="active"
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
          @update:data="update($event,'address')"
        />
      </wizard-tab-component>

      <wizard-tab-component
        title="Contacts"
        :index="2"
        :valid="false"
        :wizard="slotProps"
        v-slot="{ validateStep, active }"
      >
        <contact-wizard-step
          v-if="active"
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
          @update:data="update($event,'contact')"
        />
      </wizard-tab-component>

      <wizard-tab-component
        title="Review Application"
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
