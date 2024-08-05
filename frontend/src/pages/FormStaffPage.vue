<script setup lang="ts">
import { ref, reactive, computed, watch, toRef } from "vue";
// Carbon
import "@carbon/web-components/es/components/ui-shell/index";
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/tooltip/index";
// Composables
import { useRouter } from "vue-router";
import { useEventBus } from "@vueuse/core";
import { usePost } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
import { isSmallScreen, isTouchScreen } from "@/composables/useScreenSize";
import {
  BusinessTypeEnum,
  ClientTypeEnum,
  LegalTypeEnum,
  type CodeNameType,
  type ValidationMessageType,
  type FuzzyMatcherEvent,
  type FuzzyMatchResult,
} from "@/dto/CommonTypesDto";
import {
  locationName as defaultLocation,
  emptyContact,
  newFormDataDto,
  type Contact,
  type Address,
  type FormDataDto,
} from "@/dto/ApplyClientNumberDto";
import { getEnumKeyByEnumValue, convertFieldNameToSentence } from "@/services/ForestClientService";
// Imported global validations
import { validate, runValidation, addValidation, getValidations } from "@/helpers/validators/StaffFormValidations";
import { isContainedIn } from "@/helpers/validators/GlobalValidators";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";

// Imported Pages
import IndividualClientInformationWizardStep from "@/pages/staffform/IndividualClientInformationWizardStep.vue";
import FirstNationClientInformationWizardStep from "@/pages/staffform/FirstNationClientInformationWizardStep.vue";
import LocationsWizardStep from "@/pages/staffform/LocationsWizardStep.vue";
import ContactsWizardStep from "@/pages/staffform/ContactsWizardStep.vue";
import ReviewWizardStep from "@/pages/staffform/ReviewWizardStep.vue";
// @ts-ignore
import ArrowRight16 from "@carbon/icons-vue/es/arrow--right/16";


const clientTypesList: CodeNameType[] = [
  {
    code: "BCR",
    name: "BC registered business",
  },
  {
    code: "R",
    name: "First Nation",
  },
  {
    code: "G",
    name: "Government",
  },
  {
    code: "I",
    name: "Individual",
  },
  {
    code: "F",
    name: "Ministry of Forests",
  },
  {
    code: "U",
    name: "Unregistered company",
  },
];

const notificationBus = useEventBus<ValidationMessageType | undefined>("error-notification");
const errorBus = useEventBus<ValidationMessageType[]>("submission-error-notification");
const overlayBus = useEventBus<boolean>('overlay-event');
const fuzzyBus = useEventBus<FuzzyMatcherEvent>("fuzzy-error-notification");

// Route related
const router = useRouter();

const { setScrollPoint } = useFocus();

let formData = reactive<FormDataDto>({ ...newFormDataDto() });

const locations = computed(() =>
  formData.location.addresses.map((address: any) => address.locationName)
);
addValidation(
  "location.contacts.*.locationNames.*.text",
  isContainedIn(locations, "Location name must be one of the locations")
);

// Tab system
const progressData = reactive([
  {
    title: "Client information",
    subtitle: "Step 1",
    kind: "current",
    disabled: false,
    valid: false,
    step: 0,
    fields: [
      "businessInformation.businessType",
      "businessInformation.businessName",
      "businessInformation.clientType",
    ],
    extraValidations: [],
  },
  {
    title: "Locations",
    subtitle: "Step 2",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 1,
    fields: [
      "location.addresses.*.locationName",
      "location.addresses.*.complementaryAddressOne",
      "location.addresses.*.complementaryAddressTwo",
      "location.addresses.*.country.text",
      "location.addresses.*.province.text",
      "location.addresses.*.city",
      "location.addresses.*.streetAddress",
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value !== "CA" && $.location.addresses.*.country.value !== "US")',
      "location.addresses.*.emailAddress",
      "location.addresses.*.businessPhoneNumber",
      "location.addresses.*.secondaryPhoneNumber",
      "location.addresses.*.faxNumber",
      "location.addresses.*.notes",
    ],
    extraValidations: [],
  },
  {
    title: "Contacts",
    subtitle: "Step 3",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 2,
    fields: [
      "location.contacts.*.locationNames.*.text",
      "location.contacts.*.contactType.text",
      "location.contacts.*.firstName",
      "location.contacts.*.lastName",
      "location.contacts.*.email",
      "location.contacts.*.phoneNumber",
      "location.contacts.*.secondaryPhoneNumber",
      "location.contacts.*.faxNumber",
    ],
    extraValidations: [],
  },
  {
    title: "Review",
    subtitle: "Step 4",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 3,
    fields: [
      "businessInformation.businessType",
      "businessInformation.businessName",
      "businessInformation.clientType",
      "businessInformation.notes",
      "location.addresses.*.locationName",
      "location.addresses.*.complementaryAddressOne",
      "location.addresses.*.complementaryAddressTwo",
      "location.addresses.*.country.text",
      "location.addresses.*.province.text",
      "location.addresses.*.city",
      "location.addresses.*.streetAddress",
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value !== "CA" && $.location.addresses.*.country.value !== "US")',
      "location.addresses.*.emailAddress",
      "location.addresses.*.businessPhoneNumber",
      "location.addresses.*.secondaryPhoneNumber",
      "location.addresses.*.faxNumber",
      "location.addresses.*.notes",
      "location.contacts.*.locationNames.*.text",
      "location.contacts.*.contactType.text",
      "location.contacts.*.firstName",
      "location.contacts.*.lastName",
      "location.contacts.*.email",
      "location.contacts.*.phoneNumber",
      "location.contacts.*.secondaryPhoneNumber",
      "location.contacts.*.faxNumber",
    ],
    extraValidations: [],
  },
]);
const currentTab = ref(0);

const isLast = computed(() => currentTab.value === progressData.length - 1);
const isFirst = computed(() => currentTab.value === 0);

const checkStepValidity = (stepNumber: number): boolean => {
  progressData.forEach((step: any) => {
    if (step.step <= stepNumber) {
      step.valid = validate(step.fields, formData, true);
    }
  });

  if (!progressData[stepNumber].valid) {
    // Stop here so the step basic validation messages don't get cleared.
    return false;
  }

  if (
    !progressData[stepNumber].extraValidations.every((validation: any) =>
      runValidation(validation.field, formData, validation.validation, true, true),
    )
  )
    return false;

  return progressData[stepNumber].valid;
};

const validateStep = (valid: boolean) => {
  progressData[currentTab.value].valid = valid;
  if (valid) {
    const nextStep = progressData.find((step: any) => step.step === currentTab.value + 1);
    if (nextStep) nextStep.disabled = false;
  }
};

const onCancel = () => {
  router.push("/");
};

const lookForMatches = (onEmpty: () => void) => {
  overlayBus.emit({ isVisible: true, message: "", showLoading: true });
  fuzzyBus.emit(undefined);
  errorBus.emit([]);
  notificationBus.emit(undefined);

  const { response, error, handleErrorDefault } = usePost(
    "/api/clients/matches",
    toRef(formData).value,
    {
      skipDefaultErrorHandling: true,
      headers: {
        "X-STEP": `${currentTab.value + 1}`,
      },
    },
  );

  watch([response], () => {
    if (response.value.status === 204) {
      overlayBus.emit({ isVisible: false, message: "", showLoading: false });
      onEmpty();
    }
  });

  watch([error], () => {
    // Disable the overlay
    overlayBus.emit({ isVisible: false, message: "", showLoading: false });

    if (error.value.response?.status === 409) {
      fuzzyBus.emit({
        id: "global",
        matches: error.value.response.data as FuzzyMatchResult[],
      });
    } else {
      handleErrorDefault();
    }

    setScrollPoint("top-notification");
  });
};

const moveToNextStep = () => {
  currentTab.value++;
  progressData[currentTab.value - 1].kind = "complete";
  progressData[currentTab.value].kind = "current";
  setScrollPoint("step-title");
};

const onNext = () => {
  //This fixes the index
  formData.location.addresses.forEach((address: Address, index: number) => (address.index = index));
  formData.location.contacts.forEach((contact: Contact, index: number) => (contact.index = index));

  notificationBus.emit(undefined);
  if (currentTab.value + 1 < progressData.length) {
    if (checkStepValidity(currentTab.value)) {
      lookForMatches(moveToNextStep);
    } else {
      setScrollPoint("top-notification");
    }
  }
};

const onBack = () => {
  notificationBus.emit(undefined);
  if (currentTab.value - 1 >= 0) {
    currentTab.value--;
    progressData[currentTab.value + 1].kind = "incomplete";
    progressData[currentTab.value].kind = "current";
    setScrollPoint("step-title");
    setTimeout(revalidateBus.emit, 1000);
  }
};

const clientType = ref<CodeNameType>();

const updateClientType = (value: CodeNameType | undefined) => {
  if (value) {
    clientType.value = value;

    // reset formData
    formData = newFormDataDto();

    switch (value.code) {
      case "I": {

        Object.assign(formData.businessInformation, {
          businessType: getEnumKeyByEnumValue(BusinessTypeEnum, BusinessTypeEnum.U),
          legalType: getEnumKeyByEnumValue(LegalTypeEnum, LegalTypeEnum.SP),
          clientType: getEnumKeyByEnumValue(ClientTypeEnum, ClientTypeEnum.I),
          goodStandingInd: "Y",
        });

        // Initialize the "primary" contact - the individual him/herself
        const applicantContact: Contact = {
          ...emptyContact,
          locationNames: [defaultLocation],
          contactType: { value: "BL", text: "Billing" },
        };
        formData.location.contacts[0] = applicantContact;                
        break;
      }
      default:
        break;
    }
  } else {
    clientType.value = null;
  }
};

const validation = reactive<Record<string, boolean>>({});

const revalidateBus = useEventBus<void>("revalidate-bus");

const goToStep = (index: number, skipCheck: boolean = false) => {
  if (skipCheck || (index <= currentTab.value && checkStepValidity(index)))
    currentTab.value = index;
  else notificationBus.emit({ fieldId: "missing.info", errorMsg: "" });
  revalidateBus.emit();
};

const submitBtnDisabled = ref(false);

const submit = () => {
  errorBus.emit([]);
  notificationBus.emit(undefined);

  const {
    response,
    error,
    fetch: fetchSubmission,
    handleErrorDefault,
  } = usePost("/api/clients/submissions/staff", toRef(formData).value, {
    skip: true,
    skipDefaultErrorHandling: true,
  });

  watch([response], () => {
    if (response.value.status === 201) {
      overlayBus.emit({ isVisible: false, message: "", showLoading: false });
      router.push(
        { 
          name: "staff-confirmation", 
          state: { 
            clientNumber: response.value.headers['x-client-id'],
            clientEmail: formData.location.contacts[0].email,
          }
        } 
      );
    }
  });

  watch([error], () => {
    // reset the button to allow a new submission attempt
    submitBtnDisabled.value = false;
    //Disable the overlay
    overlayBus.emit({ isVisible: false, message: "", showLoading: false });

    if(error.value.response?.status === 400) {
      const validationErrors: ValidationMessageType[] = error.value.response?.data;

      validationErrors.forEach((errorItem: ValidationMessageType) =>
        notificationBus.emit({
          fieldId: "server.validation.error",
          fieldName: convertFieldNameToSentence(errorItem.fieldId),
          errorMsg: errorItem.errorMsg,
        }),
      );
    } else if(error.value.response?.status === 408) {      
      router.push(
        { 
          name: "staff-processing", 
          params: { 
            submissionId: error.value.response.headers['x-sub-id'],
            clientEmail: formData.location.contacts[0].email
          }
        } 
      );
    } else{
      handleErrorDefault();
    }

    setScrollPoint("top-notification");
  });

  if (checkStepValidity(currentTab.value)) {
    submitBtnDisabled.value = true;
    overlayBus.emit({ isVisible: true, message: "", showLoading: true });
    fetchSubmission();
  }
};
</script>

<template>
  <div id="screen" class="submission-content">
    <div class="form-header" role="header">
      <div class="form-header-title">
        <h1>
          <div data-scroll="top" class="header-offset"></div>
          Create client
        </h1>
      </div>

      <div class="sr-only" role="status">
        Current step: {{ progressData[currentTab].title }}. Step {{ currentTab + 1 }} of
        {{ progressData.length }}.
      </div>

      <cds-progress-indicator space-equally :vertical="isSmallScreen" aria-label="Form steps">
        <cds-progress-step
          v-for="item in progressData"
          ref="cdsProgressStepArray"
          :key="item.step"
          :label="item.title"
          :secondary-label="item.subtitle"
          :state="item.kind"
          :class="item.step <= currentTab ? 'step-active' : 'step-inactive'"
          :disabled="item.disabled"
          v-shadow="3"
          :aria-current="item.step === currentTab ? 'step' : 'false'"
        />
      </cds-progress-indicator>
    </div>

    <div class="form-steps-staff" role="main">
      <div class="errors-container hide-when-less-than-two-children">
        <!--
        The parent div is necessary to avoid the div.header-offset below from interfering in the flex flow.
        -->
        <div data-scroll="top-notification" class="header-offset"></div>
        <error-notification-grouping-component :form-data="formData" />
        <fuzzy-match-notification-grouping-component
          id="global"
          :business-name="formData.businessInformation.businessName"
        />
      </div>
      <div v-if="currentTab == 0" class="form-steps-01">
        <div class="form-steps-section">
          <h2 data-focus="focus-0" tabindex="-1">
            <div data-scroll="step-title" class="header-offset"></div>
            {{ progressData[0].title}}
          </h2>
          <dropdown-input-component
            id="clientType"
            label="Client type"
            :initial-value="clientType?.name"
            required
            required-label
            :model-value="clientTypesList"
            :enabled="true"
            tip=""
            :validations="[
              ...getValidations('businessInformation.clientType'),
              submissionValidation('businessInformation.clientType'),
            ]"
            @update:selected-value="updateClientType($event)"
            @empty="validation.type = !$event"
          />
          <individual-client-information-wizard-step
            v-if="clientType?.code === 'I'"
            :active="currentTab == 0"
            :data="formData"
            @valid="validateStep"
          />
          <first-nation-client-information-wizard-step
            v-if="clientType?.code === 'R'"
            :active="currentTab == 0"
            :data="formData"
            @valid="validateStep"
          />
        </div>
      </div>

      <div v-if="currentTab == 1" class="form-steps-02">
        <div class="form-steps-section">
          <h2 data-focus="focus-0" tabindex="-1">
            <div data-scroll="step-title" class="header-offset"></div>
            {{ progressData[1].title}}
          </h2>
          <locations-wizard-step
            :active="currentTab == 1"
            :data="formData"
            @valid="validateStep"
            :max-locations="25"
          />
        </div>
      </div>

      <div v-if="currentTab == 2" class="form-steps-03">
        <div class="form-steps-section">
          <h2 data-focus="focus-0" tabindex="-1">
            <div data-scroll="step-title" class="header-offset"></div>
            {{ progressData[2].title}}
          </h2>
          <contacts-wizard-step
            :active="currentTab == 2"
            :data="formData"
            @valid="validateStep"
            :max-contacts="25"
          />
        </div>
      </div>
      
      <div v-if="currentTab == 3" class="form-steps-04">
        <div class="form-steps-section form-steps-section-04">
          <h2 data-scroll="scroll-3" data-focus="focus-3" tabindex="-1">
            <div data-scroll="step-title" class="header-offset"></div>
            {{ progressData[3].title}}
          </h2>
          <span class="body-02">
            Make any changes by using the "Edit" buttons in each section below
          </span>

          <review-wizard-step
              v-model:data="formData"
              :active="currentTab == 3"
              @valid="validateStep"
              :goToStep="goToStep"
            />
        </div>
      </div>

      <div class="form-footer" role="footer">
        <div class="form-footer-group">
          <div class="form-footer-group-next">
            <span class="body-compact-01" v-if="!isLast && !progressData[currentTab].valid">
              All required fields must be filled out correctly to enable the "Next" button below
            </span>
            <div class="form-footer-group-buttons">
              <cds-button
                v-if="isFirst"
                kind="secondary"
                size="lg"
                :disabled="!isFirst"
                v-on:click="onCancel"
                data-test="wizard-cancel-button"
              >
                <span>Cancel</span>
              </cds-button>

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
                v-if="isLast"
                  data-test="wizard-submit-button"
                  kind="primary"
                  size="lg"
                  @click.prevent="submit"
                  :disabled="submitBtnDisabled"
                >
                <span>Create client</span>
                <Check16 slot="icon" />
              </cds-button>

              <cds-tooltip v-if="!isLast">
                <cds-button
                  id="nextBtn"
                  kind="primary"
                  size="lg"
                  :disabled="progressData[currentTab].valid === false"
                  v-on:click="onNext"
                  data-test="wizard-next-button"
                >
                  <span>Next</span>
                  <ArrowRight16 slot="icon" />
                </cds-button>
                <cds-tooltip-content
                  v-if="!isTouchScreen"
                  v-show="progressData[currentTab].valid === false"
                >
                  All required fields must be filled in correctly.
                </cds-tooltip-content>
              </cds-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>  
  </div>
</template>
