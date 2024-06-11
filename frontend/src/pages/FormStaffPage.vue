<script setup lang="ts">
import { ref, reactive, computed } from "vue";
// Carbon
import "@carbon/web-components/es/components/ui-shell/index";
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/tooltip/index";
// Composables
import { useRouter } from "vue-router";
import { useEventBus } from "@vueuse/core";
import { isSmallScreen, isTouchScreen } from "@/composables/useScreenSize";
import {
  BusinessTypeEnum,
  ClientTypeEnum,
  LegalTypeEnum,
  type CodeNameType,
  type ModalNotification,
} from "@/dto/CommonTypesDto";
import {
  locationName as defaultLocation,
  emptyContact,
  newFormDataDto,
  type Contact,
  type FormDataDto,
} from "@/dto/ApplyClientNumberDto";
import { getEnumKeyByEnumValue } from "@/services/ForestClientService";
// Imported global validations
import "@/helpers/validators/StaffFormValidations";
// Imported Pages
import IndividualClientInformationWizardStep from "@/pages/staffform/IndividualClientInformationWizardStep.vue";
// @ts-ignore
import ArrowRight16 from "@carbon/icons-vue/es/arrow--right/16";

// Defining the props and emiter to reveice the data and emit an update
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

const toastBus = useEventBus<ModalNotification>("toast-notification");

// Route related
const router = useRouter();

const formData = ref<FormDataDto>({ ...newFormDataDto() });

// Tab system
const progressData = reactive([
  {
    title: "Client information",
    subtitle: "Step 1",
    kind: "current",
    disabled: false,
    valid: false,
    step: 0,
    fields: [],
    extraValidations: [],
  },
  {
    title: "Locations",
    subtitle: "Step 2",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 1,
    fields: [],
    extraValidations: [],
  },
  {
    title: "Contacts",
    subtitle: "Step 3",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 2,
    fields: [],
    extraValidations: [],
  },
  {
    title: "Review",
    subtitle: "Step 4",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 3,
    fields: [],
    extraValidations: [],
  },
]);
const currentTab = ref(0);

const isLast = computed(() => currentTab.value === progressData.length - 1);
const isFirst = computed(() => currentTab.value === 0);

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

const onNext = () => {
  // TODO
  console.log("Next button clicked.");
};

const clientTypeCode = ref<string>(null);

const updateClientType = (value: CodeNameType | undefined) => {
  if (value) {
    clientTypeCode.value = value.code;

    // reset formData
    formData.value = newFormDataDto();

    switch (value.code) {
      case "I": {
        Object.assign(formData.value.businessInformation, {
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
        formData.value.location.contacts[0] = applicantContact;
        break;
      }
      default:
        break;
    }
  } else {
    clientTypeCode.value = null;
  }
};

const validation = reactive<Record<string, boolean>>({});
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

      <div class="hide-when-less-than-two-children">
        <div data-scroll="top-notification" class="header-offset"></div>
      </div>
    </div>

    <div class="form-steps-staff" role="main">
      <div class="form-steps-section">
        <h2 data-focus="focus-0" tabindex="-1">
          <div data-scroll="step-title" class="header-offset"></div>
          Client information
        </h2>
        <dropdown-input-component
          id="clientType"
          label="Client type"
          :initial-value="null"
          required
          required-label
          :model-value="clientTypesList"
          :enabled="true"
          tip=""
          :validations="[]"
          @update:selected-value="updateClientType($event)"
          @empty="validation.type = !$event"
        />
        <individual-client-information-wizard-step
          v-if="clientTypeCode === 'I'"
          :active="currentTab == 0"
          :data="formData"
          @valid="validateStep"
        />
      </div>
      <div class="form-footer" role="footer">
        <div class="form-footer-group">
          <div class="form-footer-group-next">
            <span class="body-compact-01" v-if="!isLast && !progressData[currentTab].valid">
              All fields must be filled out correctly to enable the "Next" button below
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
                data-test="wizard-back-button"
              >
                <span>Back</span>
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
                  All fields must be filled in correctly.
                </cds-tooltip-content>
              </cds-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
