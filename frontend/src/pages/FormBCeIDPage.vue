<script setup lang="ts">
import { reactive, watch, toRef, ref, getCurrentInstance, computed } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/progress-indicator/index";
import "@carbon/web-components/es/components/notification/index";
// Composables
import { useEventBus, useMediaQuery } from "@vueuse/core";
import { useRouter } from "vue-router";
import { useFocus } from "@/composables/useFocus";
import { usePost } from "@/composables/useFetch";
// Imported Pages
import BusinessInformationWizardStep from "@/pages/bceidform/BusinessInformationWizardStep.vue";
import AddressWizardStep from "@/pages/bceidform/AddressWizardStep.vue";
import ContactWizardStep from "@/pages/bceidform/ContactWizardStep.vue";
import ReviewWizardStep from "@/pages/bceidform/ReviewWizardStep.vue";
// Imported types
import { newFormDataDto } from "@/dto/ApplyClientNumberDto";
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

const errorBus = useEventBus<ValidationMessageType[]>("submission-error-notification");
const notificationBus = useEventBus<ValidationMessageType | undefined>("error-notification");
const exitBus = useEventBus<Record<string, boolean | null>>("exit-notification");
const revalidateBus = useEventBus<void>("revalidate-bus");
const progressIndicatorBus = useEventBus<ProgressNotification>("progress-indicator-bus");

const router = useRouter();
const { setScrollPoint, setFocusedComponent } = useFocus();
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
  const validationErrors: ValidationMessageType[] = error.value.response
    ?.data as ValidationMessageType[];

  validationErrors.forEach((errorItem: ValidationMessageType) =>
    notificationBus.emit(errorItem)
  );
  setScrollPoint("top");
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

  if(!progressData[stepNumber]
    .extraValidations
    .every((validation: any) => runValidation(validation.field, formData, validation.validation, true, true))
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

const onNext = () => {
  notificationBus.emit(undefined);
  if (currentTab.value + 1 < progressData.length) {
    if (checkStepValidity(currentTab.value)) {
      currentTab.value++;
      progressData[currentTab.value - 1].kind = "complete";
      progressData[currentTab.value].kind = "current";
    }
    setScrollPoint("top");
  }
};
const onBack = () => {
  if (currentTab.value - 1 >= 0) {
    currentTab.value--;
    progressData[currentTab.value + 1].kind = "incomplete";
    progressData[currentTab.value].kind = "current";
    setScrollPoint("top");
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

let submitBtnDisabled = ref(false);

const submit = () => {
  errorBus.emit([]);
  notificationBus.emit(undefined);

  if (checkStepValidity(currentTab.value)) {
    submitBtnDisabled.value = true;
    fetch();
  }
};

exitBus.on((event: Record<string, boolean | null>) => {
  endAndLogOut.value = event.goodStanding ? event.goodStanding : false;
  mailAndLogOut.value = event.duplicated ? event.duplicated : false;
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
      setFocusedComponent(`addressname_${index}`);
    });
  }
};

const isSmallScreen = useMediaQuery("(max-width: 671px)");
</script>

<template>
  <div class="form-header">
    <div class="form-header-title">
      <span class="heading-05" data-scroll="top">New client application</span>
      <p class="body-01">All fields are mandatory</p>
    </div>
    <cds-progress-indicator space-equally :vertical="isSmallScreen">
      <cds-progress-step 
        v-for="item in progressData"
        :key="item.step"
        :label="item.title"
        :secondary-label="item.subtitle"
        :state="item.kind"
        :class="item.step <= currentTab ? 'step-active' : 'step-inactive'"          
        :disabled="item.disabled"
        v-shadow="3"
      />
    </cds-progress-indicator>
    <error-notification-grouping-component
      :form-data="formData"
      :scroll-to-element-fn="scrollToNewContact"
      scroll-to-element-name="addNewContact"
    />
  </div>

  <div class="form-steps">

    <div v-if="currentTab == 0" class="form-steps-01">
      <div class="form-steps-01-title">
        <span class="heading-04" data-scroll="scroll-0">Before you begin</span>
        <ol type="1" class="numbered-list body-compact-01">
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
                href="https://www.bceid.ca/"
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
            ref="contactWizardRef"
          />
      </div>
    </div>

    <div v-if="currentTab == 3" class="form-steps-04">

      <div class="form-steps-section form-steps-section-04">
        <span class="heading-04" data-scroll="scroll-3">{{ progressData[3].title}}</span>
        <span class="body-02">
          Review the content and make any changes by using the "Edit" buttons in each section below.
        </span>

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

          <cds-button
            v-if="!isLast && !endAndLogOut && !mailAndLogOut"
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
