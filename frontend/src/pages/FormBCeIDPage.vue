<script setup lang="ts">
import { reactive, watch, toRef, ref, getCurrentInstance, computed, nextTick } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/progress-indicator/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/tooltip/index";
import type { CDSProgressStep } from "@carbon/web-components";
// Composables
import { useEventBus } from "@vueuse/core";
import { useRouter } from "vue-router";
import { useFocus } from "@/composables/useFocus";
import { usePost, useFetchTo } from "@/composables/useFetch";
import { isSmallScreen, isTouchScreen } from "@/composables/useScreenSize";
// Imported Pages
import BusinessInformationWizardStep from "@/pages/bceidform/BusinessInformationWizardStep.vue";
import AddressWizardStep from "@/pages/bceidform/AddressWizardStep.vue";
import ContactWizardStep from "@/pages/bceidform/ContactWizardStep.vue";
import ReviewWizardStep from "@/pages/bceidform/ReviewWizardStep.vue";
// Imported types
import { newFormDataDto, locationName as defaultLocation } from "@/dto/ApplyClientNumberDto";
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import type {
  ValidationMessageType,
  ProgressNotification,
  CodeDescrType,
} from "@/dto/CommonTypesDto";
// Imported User session
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
// Imported global validations
import { addValidation } from "@/helpers/validators/BCeIDFormValidations";

import {
  runValidation,
  validate,
  isContainedIn,
} from "@/helpers/validators/GlobalValidators";

// @ts-ignore
import ArrowRight16 from "@carbon/icons-vue/es/arrow--right/16";
// @ts-ignore
import LogOut16 from "@carbon/icons-vue/es/logout/16";
// @ts-ignore
import Check16 from "@carbon/icons-vue/es/checkmark/16";
import { convertFieldNameToSentence } from "@/services/ForestClientService";

const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);
const notificationBus = useEventBus<ValidationMessageType | undefined>(
  "error-notification"
);
const exitBus =
  useEventBus<Record<string, boolean | null>>("exit-notification");
const revalidateBus = useEventBus<void>("revalidate-bus");
const progressIndicatorBus = useEventBus<ProgressNotification>(
  "progress-indicator-bus"
);

const router = useRouter();
const { setScrollPoint, safeSetFocusedComponent } = useFocus();
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

const features = instance.appContext.config.globalProperties.$features;
if (!features.BCEID_MULTI_ADDRESS) {
  submitterContact.locationNames = [{ ...defaultLocation }];
}

let formDataDto = ref<FormDataDto>({ ...newFormDataDto() });

//---- Form Data ----//
let formData = reactive<FormDataDto>({
  ...formDataDto.value,
  businessInformation: {
    ...formDataDto.value.businessInformation,
    birthdate: "",
  },
  location: {
    addresses: formDataDto.value.location.addresses,
    contacts: [submitterContact],
  },
});

const locations = computed(() =>
  formData.location.addresses.map((address: any) => address.locationName)
);

const associatedLocations = computed(() =>
  formData.location.contacts
    .map((contact: Contact) => contact.locationNames)
    .map((locationNames: CodeDescrType[]) =>
      locationNames.map((locationName: CodeDescrType) => locationName.text)
    )
    .reduce(
      (accumulator: string[], current: string[]) => accumulator.concat(current),
      []
    )
);

const { response, error, fetch: post } = usePost(
  "/api/clients/submissions",
  toRef(formData).value,
  {
    skip: true,
  }
);

watch([response], () => {
  if (response.value.status === 201) {
    router.push({ name: "confirmation" });
  }
});

watch([error], () => {
  const validationErrors: ValidationMessageType[] = error.value.response?.data ?? 
    [] as ValidationMessageType[];

  validationErrors.forEach((errorItem: ValidationMessageType) =>
    notificationBus.emit({
      fieldId: "server.validation.error",
      fieldName: convertFieldNameToSentence(errorItem.fieldId),
      errorMsg: errorItem.errorMsg,
    })
  );
  setScrollPoint("top-notification");
});

addValidation(
  "location.contacts.*.locationNames.*.text",
  isContainedIn(locations)
);

// Tab system
const progressData = reactive([
  {
    title: "Business information",
    subtitle: "Step 1",
    kind: "current",
    disabled: false,
    valid: false,
    step: 0,
    fields: [
      "businessInformation.district",
      "businessInformation.businessType",
      "businessInformation.businessName",
      "businessInformation.clientType",
    ],
    extraValidations: [],
  },
  {
    title: "Address",
    subtitle: "Step 2",
    kind: "incomplete",
    disabled: true,
    valid: false,
    step: 1,
    fields: [
      "location.addresses.*.locationName",
      "location.addresses.*.country.text",
      "location.addresses.*.province.text",
      "location.addresses.*.city",
      "location.addresses.*.streetAddress",
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value !== "CA" && $.location.addresses.*.country.value !== "US")',
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
    ],
    extraValidations: [
      {
        field: "location.addresses.*.locationName",
        validation: isContainedIn(
          associatedLocations,
          "Looks like “${item}” doesn’t have a contact. You must associate it with an existing contact or add a new contact before submitting the application again."
        ),
      },
    ],
  },
  {
    title: "Review application",
    subtitle: "Step 4",
    kind: "incomplete",
    disabled: true,
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
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")',
      'location.addresses.*.postalCode($.location.addresses.*.country.value !== "CA" && $.location.addresses.*.country.value !== "US")',
      "location.contacts.*.locationNames.*.text",
      "location.contacts.*.contactType.text",
      "location.contacts.*.firstName",
      "location.contacts.*.lastName",
      "location.contacts.*.email",
      "location.contacts.*.phoneNumber",
    ],
    extraValidations: [],
  },
]);

const currentTab = ref(0);

const checkStepValidity = (stepNumber: number): boolean => {
  progressData.forEach((step: any) => {
    if (step.step <= stepNumber) {
      step.valid = validate(step.fields, formData, true);
    }
  });

  if (
    !progressData[stepNumber].extraValidations.every((validation: any) =>
      runValidation(
        validation.field,
        formData,
        validation.validation,
        true,
        true
      )
    )
  )
    return false;

  return progressData[stepNumber].valid;
};

const isLast = computed(() => currentTab.value === progressData.length - 1);
const isFirst = computed(() => currentTab.value === 0);
const endAndLogOut = ref<boolean>(false);
const mailAndLogOut = ref<boolean>(false);

const goToStep = (index: number, skipCheck: boolean = false) => {
  if (skipCheck || (index <= currentTab.value && checkStepValidity(index)))
    currentTab.value = index;
  else notificationBus.emit({ fieldId: "missing.info", errorMsg: "" });
  revalidateBus.emit();
};


/*
We do not auto focus the first step in the first rendering to prevent skipping form instructions.
The differenct might only be noticeable when using a screen reader.
*/
const autoFocusFirstStep = ref(false);

const onNext = () => {
  notificationBus.emit(undefined);
  if (currentTab.value + 1 < progressData.length) {
    if (checkStepValidity(currentTab.value)) {
      // allow the first step to be auto focused in case the user returns to it later.
      autoFocusFirstStep.value = true;

      currentTab.value++;
      progressData[currentTab.value - 1].kind = "complete";
      progressData[currentTab.value].kind = "current";
      setScrollPoint("step-title");
    } else {
      setScrollPoint("top-notification");
    }
  }
};
const onBack = () => {
  if (currentTab.value - 1 >= 0) {
    currentTab.value--;
    progressData[currentTab.value + 1].kind = "incomplete";
    progressData[currentTab.value].kind = "current";
    setScrollPoint("step-title");
    setTimeout(revalidateBus.emit, 1000);
  }
};
const validateStep = (valid: boolean) => {
  progressData[currentTab.value].valid = valid;
  if (valid) {
    const nextStep = progressData.find(
      (step: any) => step.step === currentTab.value + 1
    );
    if (nextStep) nextStep.disabled = false;
  }
};

const processAndLogOut = () => {
  if (mailAndLogOut.value) {
    usePost(
      "/api/ches/duplicate",
      {
        registrationNumber: formData.businessInformation.registrationNumber,
        name: formData.businessInformation.businessName,
        userName: `${ForestClientUserSession.user?.firstName} ${ForestClientUserSession.user?.lastName}` ?? "",
        userId: ForestClientUserSession.user.userId ?? "",
        email: ForestClientUserSession.user.email ?? "",
      },
      {}
    );
  }
  session?.logOut();
};

let submitBtnDisabled = ref(false);

const submit = () => {
  errorBus.emit([]);
  notificationBus.emit(undefined);

  if (checkStepValidity(currentTab.value)) {
    submitBtnDisabled.value = true;
    post();
  }
};

exitBus.on((event: Record<string, boolean | null>) => {
  endAndLogOut.value = event.goodStanding ? event.goodStanding : false;
  mailAndLogOut.value = event.duplicated ? event.duplicated : false;
  endAndLogOut.value = event.nonPersonSP ? event.nonPersonSP : endAndLogOut.value;
  endAndLogOut.value = event.unsupportedClientType || endAndLogOut.value;
});

progressIndicatorBus.on((event: ProgressNotification) => {
  if (event.kind === "disabled") {
    [1, 2, 3]
      .filter((index: number) => event.value || index < currentTab.value + 1)
      .forEach(
        (index: number) =>
          (progressData[index].disabled = event.value as boolean)
      );
  }
  if (event.kind === "navigate") goToStep(event.value as number, true);
  if (event.kind === "error") {
    (event.value as number[]).forEach((index: number) => {
      progressData[index].valid = false;
      progressData[index].kind = "invalid";
    });
  }
});

const contactWizardRef = ref<InstanceType<typeof ContactWizardStep> | null>(
  null
);

const scrollToNewContact = () => {
  if (contactWizardRef.value) {
    // Skip auto-focus so to do it only when scroll is done.
    const index = contactWizardRef.value.addContact(false) - 1;
    setScrollPoint(`additional-contact-${index}`, undefined, () => {
      safeSetFocusedComponent(`firstName_${index}`);
    });
  }
};

const districtsList = ref([]);
useFetchTo("/api/districts?page=0&size=250", districtsList);
const formattedDistrictsList = computed(() =>
  districtsList.value.map((district) => ({
    ...district,
    name: `${district.code} - ${district.name}`,
  })),
);

const cdsProgressStepArray = ref<InstanceType<typeof CDSProgressStep>[] | null>(null);

watch(cdsProgressStepArray, async (array) => {
  if (array) {
    // wait for the DOM updates to complete
    await nextTick();

    for (const step of array) {
      const div = step.shadowRoot?.querySelector("div");
      if (div) {
        // Make the step unfocusable.
        div.tabIndex = -1;
      }
      const p = step.shadowRoot?.querySelector("p[role='button']");
      if (p) {
        // Prevent the step from being treated a button.
        p.removeAttribute("role");
      }
    }
  }
});

const individualValidInd = ref(false);
const setIndividualValidInd = (valid: boolean) => {
  individualValidInd.value = valid;
};
</script>

<template>
  <div class="form-header" role="header">
    <div class="form-header-title">
      <h1>
        <div data-scroll="top" class="header-offset"></div>
        New client application
      </h1>
    </div>
    <div class="sr-only" role="status">
      Current step: {{ progressData[currentTab].title }}. Step {{ currentTab + 1 }} of {{ progressData.length }}.
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
    <div class="hide-when-less-than-two-children"><!--
      This div is necessary to avoid the div.header-offset below from interfering in the flex flow.
    -->
      <div data-scroll="top-notification" class="header-offset"></div>
      <error-notification-grouping-component
        :form-data="formData"
        :scroll-to-element-fn="scrollToNewContact"
      />
    </div>
  </div>

  <div class="form-steps" role="main">
    <div v-if="currentTab == 0" class="form-steps-01">
      <div class="form-steps-01-title">
        <h2>Before you begin</h2>
        <ol type="1" class="bulleted-list body-compact-01">
          <li>A registered business must be in good standing with BC Registries</li>
          <li>
            You must be able to receive email at
            <b>{{ submitterContact.email }}</b>
          </li>
        </ol>
      </div>

      <hr class="divider" />

      <div class="form-steps-section">
        <business-information-wizard-step
          v-model:data="formData"
          :active="currentTab == 0"
          :title="progressData[0].title"
          :districts-list="formattedDistrictsList"
          @valid="validateStep"
          :auto-focus="autoFocusFirstStep"
          :individual-valid-ind="individualValidInd"
          :set-individual-valid-ind="setIndividualValidInd"
        />
      </div>
    </div>

    <div v-if="currentTab == 1" class="form-steps-02">
      <div class="form-steps-section">
        <h2 data-scroll="scroll-1" data-focus="focus-1" tabindex="-1">
          <div data-scroll="step-title" class="header-offset"></div>
          {{ progressData[1].title }}
        </h2>

        <div class="form-steps-section-01">
          <h3>This is the primary address where you will receive mail.</h3>
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
        <h2 data-scroll="scroll-2" data-focus="focus-2" tabindex="-1">
          <div data-scroll="step-title" class="header-offset"></div>
          {{ progressData[2].title }}
        </h2>

        <div class="form-steps-section-01">
          <h3>Add authorized people to the account</h3>
          <p class="body-02 light-theme-text-text-primary">
            Review your name and email address. They’re from your BCeID.
          </p>
        </div>

        <contact-wizard-step
          v-model:data="formData"
          :active="currentTab == 2"
          @valid="validateStep"
          ref="contactWizardRef"
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
          Review the content and make any changes by using the "Edit" buttons in each section below.
        </span>

        <review-wizard-step
            v-model:data="formData"
            :districts-list="formattedDistrictsList"
            :active="currentTab == 3"
            @valid="validateStep"
            :goToStep="goToStep"
          />

      </div>
    </div>

    <hr v-if="currentTab < 3" class="divider"/>
  </div>

  <div class="form-footer" role="footer">
    <div class="form-footer-group">
      <div class="form-footer-group-next">

        <span class="body-compact-01" 
              v-if="!isLast && !progressData[currentTab].valid">
          All fields must be filled out correctly to enable the "Next" button below
        </span>
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

          <cds-tooltip v-if="!isLast && !endAndLogOut && !mailAndLogOut">
            <cds-button
              id="nextBtn"
              kind="primary"
              size="lg"
              v-on:click="onNext"
              :disabled="progressData[currentTab].valid === false"
              data-test="wizard-next-button"
              >
              <span>Next</span>
              <ArrowRight16 slot="icon" />
            </cds-button>
            <cds-tooltip-content v-if="!isTouchScreen" v-show="progressData[currentTab].valid === false">
              All fields must be filled in correctly.
            </cds-tooltip-content>
          </cds-tooltip>

          <cds-button
            v-if="isLast && !endAndLogOut && !mailAndLogOut"
              data-test="wizard-submit-button"
              kind="primary"
              size="lg"
              @click.prevent="submit"
              :disabled="submitBtnDisabled"
            >
            <span>Submit application</span>
            <Check16 slot="icon" />
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
