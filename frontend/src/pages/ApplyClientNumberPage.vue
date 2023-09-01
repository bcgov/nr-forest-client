<script setup lang="ts">
import { reactive, watch, toRef, ref, getCurrentInstance, computed } from 'vue'
// Carbon
import '@carbon/web-components/es/components/button/index';
// Composables
import { useEventBus } from "@vueuse/core";
import { useRouter } from "vue-router";
import { useFocus } from "@/composables/useFocus";
import { usePost } from "@/composables/useFetch";
// Imported Pages
import BusinessInformationWizardStep from "@/pages/applyform/BusinessInformationWizardStep.vue";
import AddressWizardStep from "@/pages/applyform/AddressWizardStep.vue";
import ContactWizardStep from "@/pages/applyform/ContactWizardStep.vue";
import ReviewWizardStep from "@/pages/applyform/ReviewWizardStep.vue";
// Imported types
import { newFormDataDto } from "@/dto/ApplyClientNumberDto";
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import type {
  ValidationMessageType,
  ModalNotification,
} from "@/dto/CommonTypesDto";
// Imported User session
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
// Imported global validations
import {
  addValidation,
  validate,
} from "@/helpers/validators/ExternalFormValidations";
// @ts-ignore
import ArrowRight16 from "@carbon/icons-vue/es/arrow--right/16";
// @ts-ignore
import Save16 from "@carbon/icons-vue/es/save/16";
// @ts-ignore
import LogOut16 from "@carbon/icons-vue/es/logout/16";
// @ts-ignore
import Check16 from "@carbon/icons-vue/es/checkmark/16";
import { isContainedIn } from "@/helpers/validators/GlobalValidators";

const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);
const generalErrorBus = useEventBus<string>("general-error-notification");
const exitBus =
  useEventBus<Record<string, boolean | null>>("exit-notification");
const toastBus = useEventBus<ModalNotification>("toast-notification");

const router = useRouter();
const { setScrollPoint } = useFocus();
const submitterInformation = ForestClientUserSession.user;
const instance = getCurrentInstance();
const session = instance?.appContext.config.globalProperties.$session;

const submitterContact: Contact = {
  locationNames: [],
  contactType: { value: "", text: "" },
  phoneNumber: "",
  firstName: session?.user?.firstName ?? "",
  lastName: session?.user?.lastName ?? "",
  email: session?.user?.email ?? "",
};

let formDataDto = ref<FormDataDto>({ ...newFormDataDto() });

//---- Form Data ----//
let formData = reactive<FormDataDto>({
  ...formDataDto.value,
  location: {
    addresses: formDataDto.value.location.addresses,
    contacts: [submitterContact],
  },
});

const locations = computed(() =>
  formData.location.addresses.map((address: any) => address.locationName)
);

const { response, error, fetch } = usePost(
  "/api/clients/submissions",
  toRef(formData).value,
  {
    skip: true,
    headers: {
      "x-user-id": submitterInformation?.userId ?? "",
      "x-user-email": submitterInformation?.email ?? "",
      "x-user-name": submitterInformation?.firstName ?? "",
    },
  }
);

watch([response], () => {
  if (response.value.status === 201) {
    router.push({ name: "confirmation" });
  }
});

watch([error], () => {
  if (error.response?.status === 400) {
    const validationErrors: ValidationMessageType[] = error.response
      ?.data as ValidationMessageType[];
    const fieldIds = [
      "businessInformation.businessType",
      "businessInformation.legalType",
      "businessInformation.clientType",
    ];

    const matchingFields = validationErrors.find((item) =>
      fieldIds.includes(item.fieldId)
    );
    if (matchingFields) {
      generalErrorBus.emit(
        `There was an error submitting your application. ${matchingFields.errorMsg}`
      );
      setScrollPoint("top");
    }
  } else {
    generalErrorBus.emit(
      `There was an error submitting your application. ${error.response?.data}`
    );
    setScrollPoint("top");
  }
});

addValidation(
  "location.contacts.*.locationNames.*.text",
  isContainedIn(locations)
);

// Tab system
const progressData = reactive([
  {
    title: "Business Information",
    subtitle: "Step 1",
    kind: "current",
    enabled: true,
    valid: false,
    step: 0,
    fields: [
      "businessInformation.businessType",
      "businessInformation.businessName",
      "businessInformation.clientType",
    ],
  },
  {
    title: "Address",
    subtitle: "Step 2",
    kind: "queued",
    enabled: true,
    valid: false,
    step: 1,
    fields: [
      "location.addresses.*.locationName",
      "location.addresses.*.country.text",
      "location.addresses.*.province.text",
      "location.addresses.*.city",
      "location.addresses.*.streetAddress",
      'location.addresses.*.postalCode(location.addresses.*.country.text === "CA")',
      'location.addresses.*.postalCode(location.addresses.*.country.text === "US")',
      'location.addresses.*.postalCode(location.addresses.*.country.text !== "CA" && location.addresses.*.country.text !== "US")',
    ],
  },
  {
    title: "Contacts",
    subtitle: "Step 3",
    kind: "queued",
    enabled: true,
    valid: false,
    step: 2,
    fields: [
      "location.contacts.*.locationNames.*.text",
      "location.contacts.*.contactType.text",
      "location.contacts.*.firstName",
      "location.contacts.*.lastName",
      "location.contacts.*.email",
      "location.contacts.*.phoneNumber",
    ],
  },
  {
    title: "Review",
    subtitle: "Step 4",
    kind: "queued",
    enabled: true,
    valid: false,
    step: 3,
    fields: [
      "businessInformation.businessType",
      "businessInformation.businessName",
      "location.addresses.*.locationName",
      "location.addresses.*.country.text",
      "location.addresses.*.province.text",
      "location.addresses.*.city",
      "location.addresses.*.streetAddress",
      'location.addresses.*.postalCode(location.addresses.*.country.text === "CA")',
      'location.addresses.*.postalCode(location.addresses.*.country.text === "US")',
      'location.addresses.*.postalCode(location.addresses.*.country.text !== "CA" && location.addresses.*.country.text !== "US")',
      "location.contacts.*.locationNames.*.text",
      "location.contacts.*.contactType.text",
      "location.contacts.*.firstName",
      "location.contacts.*.lastName",
      "location.contacts.*.email",
      "location.contacts.*.phoneNumber",
    ],
  },
]);

const currentTab = ref(0);

const stateIcon = (index: number) => {
  if (currentTab.value == index) return "current";
  if (currentTab.value > index || progressData[index].valid) return "complete";
  return "queued";
};

const checkStepValidity = (stepNumber: number): boolean => {
  progressData.forEach((step: any) => {
    if (step.step <= stepNumber) {
      step.valid = validate(step.fields, formData);
    }
  });
  return progressData[stepNumber].valid;
};

const isLast = computed(() => currentTab.value === progressData.length - 1);
const isFirst = computed(() => currentTab.value === 0);
const isCurrentValid = computed(() => progressData[currentTab.value].valid);
const isNextAvailable = computed(() => !isCurrentValid.value || isLast.value);
const isFormValid = computed(() =>
  progressData.every((entry: any) => entry.valid)
);
const endAndLogOut = ref<boolean>(false);
const mailAndLogOut = ref<boolean>(false);

const goToStep = (index: number) => {
  if (checkStepValidity(index)) currentTab.value = index;
};

const onNext = () => {
  if (currentTab.value + 1 < progressData.length) {
    if (checkStepValidity(currentTab.value)) {
      currentTab.value++;
      progressData[currentTab.value - 1].kind = stateIcon(currentTab.value - 1);
      progressData[currentTab.value].kind = stateIcon(currentTab.value);
      setScrollPoint("top");
    }
  }
};
const onBack = () => {
  if (currentTab.value - 1 >= 0) {
    currentTab.value--;
    progressData[currentTab.value + 1].kind = stateIcon(currentTab.value + 1);
    progressData[currentTab.value].kind = stateIcon(currentTab.value);
    setScrollPoint("top");
  }
};
const validateStep = (valid: boolean) => {
  progressData[currentTab.value].valid = valid;
};
const saveChange = () => {
  if (checkStepValidity(currentTab.value)) {
    toastBus.emit({
      message: `“${progressData[currentTab.value].title
        }” changes were saved successfully.`,
      kind: "Success",
      toastTitle: "",
      active: true,
      handler: () => { },
    });
    goToStep(3);
  }
};

const processAndLogOut = () => {
  if (mailAndLogOut.value) {
    usePost(
      "/api/clients/mail",
      {
        incorporation: formData.businessInformation.incorporationNumber,
        name: formData.businessInformation.businessName,
        userName: submitterInformation?.name ?? "",
        userId: submitterInformation?.userId ?? "",
        mail: submitterInformation?.email ?? "",
      },
      {}
    );
  }
  session?.logOut();
};

const submit = () => {
  errorBus.emit([]);
  generalErrorBus.emit("");
  if (checkStepValidity(currentTab.value)) {
    fetch();
  }
};

exitBus.on((event: Record<string, boolean | null>) => {
  endAndLogOut.value = event.goodStanding ? event.goodStanding : false;
  mailAndLogOut.value = event.duplicated ? event.duplicated : false;
});

const globalErrorMessage = ref<string>("");
generalErrorBus.on((event: string) => (globalErrorMessage.value = event));
</script>

<template>
  <div class="form-header">
    <div class="form-header-title">
      <span class="heading-05" data-scroll="top">New client application</span>
      <p class="body-01">All fields are mandatory</p>
    </div>
      <wizard-progress-indicator-component
      :model-value="progressData"
      @go-to="goToStep"
    />
  </div>

  <div class="form-steps">

    <div v-if="currentTab == 0" class="form-steps-01">
      <div class="form-steps-01-title">
        <span class="heading-04" data-scroll="scroll-0">Before you begin</span>
        <ol type="1" class="bulleted-list body-compact-01">
          <li>
            A registered business must be in good standing with BC
            Registries
          </li>
          <li>
            You must be able to receive email at
            {{ submitterContact.email }}
          </li>
        </ol>
      </div>

      <hr class="divider" />

      <div class="form-steps-section">
        <label class="heading-04" data-scroll="focus-0">{{ progressData[0].title}}</label>
        <div class="frame-01">
          <business-information-wizard-step
              v-model:data="formData"
              :active="currentTab == 0"
              @valid="validateStep"
          />
        </div>
      </div>
    </div>

    <div v-if="currentTab == 1" class="form-steps-02">
      <div class="form-steps-section">
        <span class="heading-04" data-scroll="scroll-1">{{ progressData[1].title}}</span>
        
        <div class="form-steps-section-01">
            <p class="body-01 heading-compact-01-dark">
              This is the primary address where you will receive mail.
            </p>
        </div>
      
        <address-wizard-step
            v-model:data="formData"
            :active="currentTab == 1"
            @valid="validateStep"
          />
      </div>    
    </div>

    <div v-if="currentTab == 2" class="form-steps-03">
      <div class="form-steps-section">
        <span class="heading-04" data-scroll="scroll-2">{{ progressData[2].title}}</span>
        
        <div class="form-steps-section-01">
          <span class="heading-03"
              >Add authorized people to the account</span
            >
            <p class="body-01 heading-compact-01-dark">
              Your first name, last name and email address are from your BCeID.
              If they're incorrect,
              <a
                href="https://bceid.ca"
                target="_blank"
                rel="noopener noreferrer"
                >go to BCeID</a
              >
              to update them before submitting your form. 
              <br /><br />Be sure to add your phone number, location and role.
            </p>
        </div>
      
        <contact-wizard-step
            v-model:data="formData"
            :active="currentTab == 2"
            @valid="validateStep"
          />
      </div>
    </div>

    <div v-if="currentTab == 3" class="form-steps-04">

      <display-block-component
      v-if="globalErrorMessage"
      kind="error"
      title="Your application could not be submitted"
      :subtitle="globalErrorMessage"
    >
    </display-block-component>


      <div class="form-steps-section form-steps-section-04">
          <span class="heading-04" data-scroll="scroll-3">{{ progressData[3].title}}</span>
          <span class="body-02">Review the content and make any changes by navigating through the steps above or using the "Edit" buttons in each section below.</span>

        <review-wizard-step
            v-model:data="formData"
            :active="currentTab == 3"
            @valid="validateStep"
            :goToStep="goToStep"
          />

      </div>
    </div>

    <hr class="divider"/>
  </div>

  <div class="form-footer">
    <div class="form-footer-group">
      <div class="form-footer-group-next">

        <span class="body-compact-01" v-if="!isLast && !progressData[currentTab].valid"
        >All fields must be filled out correctly to enable the "Next" button
        below</span
      >
      <div class="form-footer-group-buttons">
        <cds-button
            v-if="!isFirst"
            kind="secondary"
            size="lg"
            :disabled="isFirst"
            v-on:click="onBack"
            data-test="wizard-back-button"
          >
          <span>Back</span>
        </cds-button>

        <cds-button
          v-if="!isLast && !isFormValid && !endAndLogOut && !mailAndLogOut"
          id="nextBtn"
          kind="primary"
          size="lg"
          v-on:click="onNext"
          :disabled="progressData[currentTab].valid === false"
          data-test="wizard-next-button"
          tooltip-position="top"
          tooltip-text="All fields must be filled out correctly"
          :open-tooltip="progressData[currentTab].valid === false"
          >
          <span>Next</span>
          <ArrowRight16 slot="icon" />
        </cds-button>

        <cds-button
          v-if="isLast && !endAndLogOut && !mailAndLogOut"
            data-test="wizard-submit-button"
            kind="primary"
            size="lg"
            @click.prevent="submit"
          >
          <span>Submit application</span>
          <Check16 slot="icon" />
        </cds-button>

        <cds-button
          data-test="wizard-save-button"
          kind="primary"
          size="lg"
          :disabled="isNextAvailable"
          v-if="!isLast && isFormValid && !endAndLogOut && !mailAndLogOut"
          @click.prevent="saveChange"
        >
          <span>Save</span>
          <Save16 slot="icon" />
        </cds-button>

        <cds-button
          data-test="wizard-logout-button"
          kind="primary"
          size="lg"
          v-show="!isLast && (endAndLogOut || mailAndLogOut)"
          @click.prevent="processAndLogOut"
        >
          <span
            >{{ endAndLogOut ? 'End application' : 'Receive email' }} and
            logout</span
          >
          <LogOut16 slot="icon" />
        </cds-button>

        </div>
      </div>
    
    </div>
  </div>
</template>
