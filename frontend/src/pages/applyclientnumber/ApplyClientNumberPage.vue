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
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
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
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
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
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
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
          v-model:data="formData"
          :active="active"
          @valid="validateStep"
        />
      </wizard-tab-component>
    </wizard-wrapper-component>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from "vue";
import { formDataDto, type FormDataDto } from "@/dto/ApplyClientNumberDto";

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

//---- Form Data ----//
let formData = reactive<FormDataDto>(formDataDto);

formData.location.contacts = [
  {
    locationNames: [],
    contactType: { value: "", text: "" },
    phoneNumber: "",
    firstName: props.submitterInformation.submitterFirstName,
    lastName: props.submitterInformation.submitterLastName,
    email: props.submitterInformation.submitterEmail,
  },
  ...formData.location.contacts,
];

watch([formData], () => {});

const debug = (x: any) => console.log("DEBUG MAIN", JSON.stringify(x));
</script>

<style scoped>
.chefsBlue {
  background: #003366;
}
</style>
