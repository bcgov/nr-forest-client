<script setup lang="ts">
import { ref, reactive, computed, watch, toRef } from "vue";
// Carbon
import "@carbon/web-components/es/components/ui-shell/index";
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/tooltip/index";
// Composables
import { useRouter } from "vue-router";
import { useEventBus } from "@vueuse/core";
import { useFetchTo, usePost } from "@/composables/useFetch";
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
  type UserRole,
} from "@/dto/CommonTypesDto";
import {
  emptyContact,
  newFormDataDto,
  type Contact,
  type Address,
  type FormDataDto,
  defaultLocation,
  defaultContactType,
} from "@/dto/ApplyClientNumberDto";
import {
  getEnumKeyByEnumValue,
  convertFieldNameToSentence,
} from "@/services/ForestClientService";
// Imported global validations
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import {
  submissionValidation,
  resetSubmissionValidators,
} from "@/helpers/validators/SubmissionValidators";

// Imported Pages
import IndividualClientInformationWizardStep from "@/pages/staffform/IndividualClientInformationWizardStep.vue";
import FirstNationClientInformationWizardStep from "@/pages/staffform/FirstNationClientInformationWizardStep.vue";
import CombinedClientInformationWizardStep from "@/pages/staffform/CombinedClientInformationWizardStep.vue";
import BcRegisteredClientInformationWizardStep from "@/pages/staffform/BcRegisteredClientInformationWizardStep.vue";
import LocationsWizardStep from "@/pages/staffform/LocationsWizardStep.vue";
import ContactsWizardStep from "@/pages/staffform/ContactsWizardStep.vue";
import ReviewWizardStep from "@/pages/staffform/ReviewWizardStep.vue";

// Session
import ForestClientUserSession from "@/helpers/ForestClientUserSession";

// @ts-ignore
import ArrowRight16 from "@carbon/icons-vue/es/arrow--right/16";
import Check16 from "@carbon/icons-vue/es/checkmark/16";

const userRoles = ForestClientUserSession.authorities as UserRole[];

const isAdminInd = computed(() => userRoles.includes("CLIENT_ADMIN"));

const clientTypesList = computed(() => {
  const list: CodeNameType[] = [
    { code: "I", name: "Individual" },
    { code: "BCR", name: "BC registered business" },
    { code: "R", name: "First Nation" },
    { code: "G", name: "Government" },
    { code: "F", name: "Ministry of Forests" }
  ];

  if (isAdminInd.value) {
    list.push({ code: "U", name: "Unregistered company" });
  }

  return list;
});

const notificationBus = useEventBus<ValidationMessageType | undefined>(
  "error-notification"
);
const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);
const overlayBus = useEventBus<boolean>("overlay-event");
const fuzzyBus = useEventBus<FuzzyMatcherEvent>("fuzzy-error-notification");
const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

// Route related
const router = useRouter();

const { setScrollPoint } = useFocus();

const formData = reactive<FormDataDto>({ ...newFormDataDto() });

// Tab system
const progressData = reactive([
  {
    title: "Client information",
    subtitle: "Step 1",
    kind: "current",
    disabled: false,
    valid: false,
    step: 0,
  },
  {
    title: "Locations",
    subtitle: "Step 2",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 1,
  },
  {
    title: "Contacts",
    subtitle: "Step 3",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 2,
  },
  {
    title: "Review",
    subtitle: "Step 4",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 3,
  },
]);
const currentTab = ref(0);

const isLast = computed(() => currentTab.value === progressData.length - 1);
const isFirst = computed(() => currentTab.value === 0);

const checkStepValidity = (stepNumber: number): boolean =>
  progressData[stepNumber].valid;

const validateStep = (valid: boolean) => {
  progressData[currentTab.value].valid = valid;
  if (valid) {
    const nextStep = progressData.find(
      (step: any) => step.step === currentTab.value + 1
    );
    if (nextStep) nextStep.disabled = false;
  }
};

const onCancel = () => {
  router.push("/");
};

const matchError = ref(false);
const isExactMatch = ref(false);

watch(formData, () => {
  reviewStatement.value = false;

  if (matchError.value) {
    fuzzyBus.emit(undefined);
    resetSubmissionValidators();
    revalidateBus.emit();
    matchError.value = false;
  }
});

const reviewStatement = ref(false);

watch(reviewStatement, (reviewed) => {
  validateStep(reviewed);
});

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
    }
  );

  watch([response], () => {
    if (response.value.status === 204) {
      matchError.value = false;
      overlayBus.emit({ isVisible: false, message: "", showLoading: false });
      onEmpty();
    }
  });

  const getFuzzyNotificationId = (field: string) => {
    let id = "global";
    const parts = field.split(".");

    // Example: location.addresses[0]
    if (parts[0] === "location") {
      id = parts[0] + "-" + parts[1];

      // Result: location-addresses-0
      id = id.replace(/\[/g, "-").replace(/]/g, "");
    }
    return id;
  };

  watch([error], () => {
    // Disable the overlay
    overlayBus.emit({ isVisible: false, message: "", showLoading: false });

    let firstNonGlobalId: string;

    if (error.value.response?.status === 409) {
      const data: FuzzyMatchResult[] = error.value.response.data as FuzzyMatchResult[];

      matchError.value = true;
      isExactMatch.value = data.some((match) => match.fuzzy === false);

      const fuzzyEventList: Record<string, FuzzyMatcherEvent> = {};
      for (const result of data) {
        const { field } = result;
        const id = getFuzzyNotificationId(field);
        let fuzzyEvent = fuzzyEventList[id];
        if (!fuzzyEvent) {
          fuzzyEvent = {
            id,
            matches: [],
          };
          fuzzyEventList[id] = fuzzyEvent;
        }

        // Results with the same derived id are grouped in the same event
        fuzzyEvent.matches.push(result);
      }
      for (const id in fuzzyEventList) {
        const fuzzyEvent = fuzzyEventList[id];
        fuzzyBus.emit(fuzzyEvent);
        if (!firstNonGlobalId && id !== "global") {
          firstNonGlobalId = id;
        }
      }
    } else {
      handleErrorDefault();
    }

    if (firstNonGlobalId) {
      setScrollPoint(firstNonGlobalId);
    } else {
      setScrollPoint("top-notification");
    }
  });
};

const moveToNextStep = () => {
  currentTab.value++;
  progressData[currentTab.value - 1].kind = "complete";
  progressData[currentTab.value].kind = "current";
  setScrollPoint("step-title");

  // reset matcherError
  matchError.value = false;

  // reset reviewStatement
  reviewStatement.value = false;

  resetSubmissionValidators();

  fuzzyBus.emit(undefined);

  //This fixes the index
  formData.location.addresses.forEach(
    (address: Address, index: number) => (address.index = index)
  );
  formData.location.contacts.forEach(
    (contact: Contact, index: number) => (contact.index = index)
  );
};

const onNext = () => {
  //This will trigger and force the validation of the current step
  //We don't pass any field id here to force the validation of all fields
  revalidateBus.emit();

  //This is now inside a setTimeout to allow the revalidateBus to finish
  setTimeout(() => {
    notificationBus.emit(undefined);
    if (currentTab.value + 1 < progressData.length) {
      if (reviewStatement.value || checkStepValidity(currentTab.value)) {
        if (reviewStatement.value) {
          moveToNextStep();
        } else {
          lookForMatches(moveToNextStep);
        }
      } else {
        setScrollPoint("top-notification");
      }
    }
  }, 1);
};

const onBack = () => {
  notificationBus.emit(undefined);
  if (currentTab.value - 1 >= 0) {
    currentTab.value--;
    progressData[currentTab.value + 1].kind = "incomplete";
    progressData[currentTab.value].kind = "current";
    setScrollPoint("step-title");
    setTimeout(revalidateBus.emit, 1000);

    // reset matcherError
    matchError.value = false;

    // reset reviewStatement
    reviewStatement.value = false;
  }
};

// Initialize the "primary" contact - the individual him/herself
const applicantContact: Contact = {
  ...emptyContact,
  locationNames: [{ ...defaultLocation }],
  contactType: defaultContactType,
};

const clientType = ref<CodeNameType>();

const updateClientType = (value: CodeNameType | undefined) => {
  fuzzyBus.emit(undefined);
  if (value) {
    clientType.value = value;

    // reset formData
    Object.assign(formData, newFormDataDto());
    formData.notifyClientInd = "Y";

    const commonBusinessInfo = {
      businessType: getEnumKeyByEnumValue(BusinessTypeEnum, BusinessTypeEnum.U),
      legalType: getEnumKeyByEnumValue(LegalTypeEnum, LegalTypeEnum.SP),
      goodStandingInd: "Y",
    };

    const updateFormData = (clientTypeEnum?: ClientTypeEnum) => {
      Object.assign(formData.businessInformation, commonBusinessInfo);
      if (clientTypeEnum) {
        formData.businessInformation.clientType = getEnumKeyByEnumValue(
          ClientTypeEnum,
          clientTypeEnum
        );
      }
      formData.location.contacts[0] = applicantContact;
    };

    switch (value.code) {
      case "I":
        updateFormData(ClientTypeEnum.I);
        break;
      case "R":
        updateFormData(undefined);
        break;
      case "G":
        updateFormData(ClientTypeEnum.G);
        break;
      case "F":
        updateFormData(ClientTypeEnum.F);
        break;
      case "U":
        updateFormData(ClientTypeEnum.U);
        break;
      default:
        break;
    }
  } else {
    clientType.value = null;
  }
};

const validation = reactive<Record<string, boolean>>({});

const goToStep = (index: number, skipCheck: boolean = false) => {
  if (skipCheck || (index <= currentTab.value && checkStepValidity(index)))
    currentTab.value = index;
  else notificationBus.emit({ fieldId: "missing.info", errorMsg: "" });
  revalidateBus.emit();
};

const submitBtnDisabled = ref(false);
const nextBtnDisabled = ref(false);

const submit = () => {
  revalidateBus.emit();
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
      router.push({
        name: "staff-confirmation",
        state: {
          clientNumber: response.value.headers["x-client-id"],
          clientEmail: formData.location.contacts[0].email,
          notifyClientInd: formData.notifyClientInd
        },
      });
    }
  });

  watch([error], () => {
    // reset the button to allow a new submission attempt
    submitBtnDisabled.value = false;
    //Disable the overlay
    overlayBus.emit({ isVisible: false, message: "", showLoading: false });

    if (error.value.response?.status === 400) {
      const validationErrors: ValidationMessageType[] =
        error.value.response?.data;

      validationErrors.forEach((errorItem: ValidationMessageType) =>
        notificationBus.emit({
          fieldId: "server.validation.error",
          fieldName: convertFieldNameToSentence(errorItem.fieldId),
          errorMsg: errorItem.errorMsg,
        })
      );
    } else if (error.value.response?.status === 408) {
      router.push({
        name: "staff-processing",
        params: {
          submissionId: error.value.response.headers["x-sub-id"],
        },
        state: {
          clientEmail: formData.location.contacts[0].email,
          notifyClientInd: formData.notifyClientInd
        },
      });
    } else {
      handleErrorDefault();
    }

    setScrollPoint("top-notification");
  });

  setTimeout(() => {
    if (checkStepValidity(currentTab.value)) {
      submitBtnDisabled.value = true;
      overlayBus.emit({ isVisible: true, message: "", showLoading: true });
      fetchSubmission();
    }
  }, 1);
};

const submissionLimitCheck = ref([]);

const { error: submissionLimitError, 
        handleErrorDefault: submissionLimitHandleError 
      } = useFetchTo(
  "/api/submission-limit",
  submissionLimitCheck,
  {
    skipDefaultErrorHandling: true,
  }
);

watch(submissionLimitError, () => {
  if (submissionLimitError.value.response?.status === 400) {
    const validationErrors: ValidationMessageType[] =
      submissionLimitError.value.response?.data;

    validationErrors.forEach((errorItem: ValidationMessageType) =>
      notificationBus.emit({
        fieldId: "server.validation.error",
        fieldName: "",
        errorMsg: errorItem.errorMsg,
      })
    );

    nextBtnDisabled.value = true;
    return;
  }
  submissionLimitHandleError();
});
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

          <combo-box-input-component
            id="clientType"
            label="Client type"
            :initial-value="clientType?.name || ''"
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
            :user-roles="userRoles"
            @valid="validateStep"
          />
          
          <bc-registered-client-information-wizard-step
            v-if="clientType?.code === 'BCR'"
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

          <combined-client-information-wizard-step
            v-if="clientType?.code === 'G' || clientType?.code === 'F' || clientType?.code === 'U'"
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
            <simple-checkbox-input-component
              v-if="matchError && !isExactMatch"
              groupId="reviewStatementGroup"
              checkboxId="reviewStatement"
              label="Review statement"
              required-label
              v-model="reviewStatement"
              checkbox-label="I've reviewed the possible matching records and they don't correspond to the client I'm creating."
            />
            <span class="body-compact-01" v-if="!isLast && !progressData[currentTab].valid">
              All required fields must be filled out correctly to enable the "Next" button below
            </span>
            <div class="form-group-buttons">
              <cds-button
                v-if="isFirst"
                kind="secondary"
                size="lg"
                :disabled="!isFirst"
                v-on:click="onCancel"
                data-test="wizard-cancel-button"
                data-text="Cancel"
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
                data-text="Back"
              >
                <span>Back</span>
              </cds-button>

              <cds-button
                v-if="isLast"
                  data-test="wizard-submit-button"
                  data-text="Submit"
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
                  :disabled="progressData[currentTab].valid === false || nextBtnDisabled"
                  v-on:click="onNext"
                  data-test="wizard-next-button"
                  data-text="Next"
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
