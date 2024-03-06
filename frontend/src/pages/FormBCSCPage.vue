
<script setup lang="ts">
import { ref, reactive, computed, watch, toRef } from "vue";
import {
  newFormDataDto,
  emptyContact,
  locationName as defaultLocation,
} from "@/dto/ApplyClientNumberDto";
import {
  BusinessTypeEnum,
  ClientTypeEnum,
  LegalTypeEnum,
} from "@/dto/CommonTypesDto";
import { useFetch, useFetchTo, usePost } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
import { isTouchScreen } from "@/composables/useScreenSize";
import { useEventBus } from "@vueuse/core";
import {
  codeConversionFn,
  convertFieldNameToSentence,
  getEnumKeyByEnumValue,
} from "@/services/ForestClientService";
import {
  isUniqueDescriptive,
  isNullOrUndefinedOrBlank,
  runValidation,
  validate,
  getValidations,
} from "@/helpers/validators/GlobalValidators";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
import { useRouter } from "vue-router";
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import type {
  CodeNameType,
  ModalNotification,
  ValidationMessageType,
} from "@/dto/CommonTypesDto";
import Add16 from "@carbon/icons-vue/es/add/16";
import Check16 from "@carbon/icons-vue/es/checkmark/16";
import Logout16 from "@carbon/icons-vue/es/logout/16";
import ForestClientUserSession from "@/helpers/ForestClientUserSession";

const { safeSetFocusedComponent } = useFocus();
const errorMessage = ref<string | undefined>("");
const submitterInformation = ForestClientUserSession.user;

const submitterContact: Contact = {
  locationNames: [defaultLocation],
  contactType: { value: "BL", text: "Billing" },
  phoneNumber: "",
  firstName: submitterInformation?.firstName ?? "",
  lastName: submitterInformation?.lastName ?? "",
  email: submitterInformation?.email ?? "",
};

let formDataDto = ref<FormDataDto>({ ...newFormDataDto() });

const figmaFormatter = new Intl.DateTimeFormat("en-US", {
  year: "numeric",
  month: "short",
  day: "numeric",
});

const birthdate = new Date(submitterInformation?.birthdate ?? "");

const figmaFormattedDate = figmaFormatter.format(birthdate);

const year = birthdate.getFullYear();
const month = (birthdate.getMonth() + 1).toString().padStart(2, '0');
const day = birthdate.getDate().toString().padStart(2, '0');
const formattedDate = `${year}-${month}-${day}`;

//---- Form Data ----//
let formData = reactive<FormDataDto>({
  ...formDataDto.value,
  businessInformation: {
    district: "",
    businessType: getEnumKeyByEnumValue(BusinessTypeEnum, BusinessTypeEnum.U),
    legalType: getEnumKeyByEnumValue(LegalTypeEnum, LegalTypeEnum.SP),
    clientType: getEnumKeyByEnumValue(ClientTypeEnum, ClientTypeEnum.I),
    registrationNumber: "",
    businessName: submitterInformation?.name ?? "",
    goodStandingInd: "Y",
    birthdate: formattedDate,
    address: submitterInformation?.address,
  },
  location: {
    addresses: [],
    contacts: [submitterContact],
  },
});

const receviedCountry = ref({} as CodeNameType);

useFetchTo(
  `/api/countries/${submitterInformation?.address?.country?.value}`,
  receviedCountry
);

const country = computed(() => {
  return codeConversionFn(receviedCountry.value);
});

watch(country, (newValue) => {
  formData.businessInformation.address = {
    ...formData.businessInformation.address,
    country: { value: newValue.value, text: newValue.text },
    postalCode: formData.businessInformation.address.postalCode.replace(
      /\s/g,
      ""
    ),
    locationName: defaultLocation.text,
  };

  formData.location.addresses.push(formData.businessInformation.address);
});

//Scroll Fn
const { setScrollPoint } = useFocus();

const scrollToNewContact = () => {
  setScrollPoint("", undefined, () => {
    safeSetFocusedComponent("");
  });
};

//Role related data
const roleList = ref([]);
const fetch = () => {
  useFetchTo("/api/codes/contactTypes?page=0&size=250", roleList);
};
fetch();

let lastContactId = -1; // The first contactId to be generated minus 1.
const getNewContactId = () => ++lastContactId;

// Associate each contact to a unique id, permanent for the lifecycle of this component.
const contactsIdMap = new Map<Contact, number>(
  formData.location.contacts.map((contact) => [contact, getNewContactId()]),
);

//New contact being added
const otherContacts = computed(() => formData.location.contacts.slice(1));
const addContact = (autoFocus = true) => {
  const newContact = JSON.parse(JSON.stringify(emptyContact));
  newContact.locationNames.push(defaultLocation);
  const newLength = formData.location.contacts.push(newContact);

  /*
  For some reason we need to get the contact from the array to make it work.
  It doesn't work with the value from newContact as key.
  */
  const contact = formData.location.contacts[newLength - 1];
  contactsIdMap.set(contact, getNewContactId());
  if (autoFocus) {
    const focusIndex = newLength - 1;
    safeSetFocusedComponent(`firstName_${focusIndex}`);
  }
  return newLength;
};

const uniqueValues = isUniqueDescriptive();

const removeContact = (index: number) => () => {
  const contact = formData.location.contacts[index];
  const contactId = contactsIdMap.get(contact);
  contactsIdMap.delete(contact);

  updateContact(undefined, index);
  delete validation[contactId];
  uniqueValues.remove("Name", contactId + "");
  bus.emit({
    active: false,
    message: "",
    kind: "",
    toastTitle: "",
    handler: () => {},
  });
};

const revalidate = ref(false);

const updateContact = (value: Contact | undefined, index: number) => {
  if (index < formData.location.contacts.length) {
    if (value) formData.location.contacts[index] = value;
    else {
      const contactCopy: Contact[] = [...formData.location.contacts];
      contactCopy.splice(index, 1);
      formData.location.contacts = contactCopy;
    }
  }
  revalidate.value = !revalidate.value;
};

// Validation
const validation = reactive<Record<string, boolean>>({
  district: false,
  0: false,
});

const updateValidState = (index: number, valid: boolean) => {
  const contactId = contactsIdMap.get(formData.location.contacts[index]);
  if (validation[contactId] !== valid) {
    validation[contactId] = valid;
  }
};

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

watch([validation], () => {
  const valid = checkValid();
  emit("valid", valid);
  submitBtnDisabled.value = !valid;
});
emit("valid", false);

const bus = useEventBus<ModalNotification>("modal-notification");

const handleRemove = (index: number) => {
  const selectedContact = !isNullOrUndefinedOrBlank(
    formData.location.contacts[index].firstName
  )
    ? `${formData.location.contacts[index].firstName} ${formData.location.contacts[index].lastName}`
    : "Contact #" + index;
  bus.emit({
    name: selectedContact,
    message: `“${selectedContact}” additional contact was deleted`,
    kind: "contact",
    toastTitle: "The additional contact was deleted",
    handler: removeContact(index),
    active: true,
  });
};

defineExpose({
  addContact,
});

const changePersonalInfoModalActive = ref(false);

const progressData = reactive([
  {
    title: "New client application",
    subtitle: "Step 1",
    kind: "current",
    disabled: false,
    valid: false,
    step: 0,
    fields: [
      "businessInformation.district",
      "businessInformation.businessType",
      "businessInformation.businessName",
      "location.contacts.*.contactType.text",
      "location.contacts.*.firstName",
      "location.contacts.*.lastName",
      "location.contacts.*.email",
      "location.contacts.*.phoneNumber",
    ],
    extraValidations: [],
  },
]);

const submitBtnDisabled = ref(true);

const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);

const notificationBus = useEventBus<ValidationMessageType | undefined>(
  "error-notification"
);

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

// Submission
const router = useRouter();

const {
  response,
  error,
  fetch: fecthSubmit,
} = usePost("/api/clients/submissions", toRef(formData).value, {
  skip: true,
});

const submit = () => {
  errorBus.emit([]);
  notificationBus.emit(undefined);

  if (checkStepValidity(currentTab.value)) {
    submitBtnDisabled.value = false;
    console.log(toRef(formData).value);
    fecthSubmit();
  } else {
    submitBtnDisabled.value = true;
  }
};

watch([response], () => {
  if (response.value.status === 201) {
    router.push({ name: "confirmation" });
  }
});

watch([error], () => {
  const validationErrors: ValidationMessageType[] =
    error.value.response?.data ?? ([] as ValidationMessageType[]);

  validationErrors.forEach((errorItem: ValidationMessageType) =>
    notificationBus.emit({
      fieldId: "server.validation.error",
      fieldName: convertFieldNameToSentence(errorItem.fieldId),
      errorMsg: errorItem.errorMsg,
    })
  );
  setScrollPoint("top-notification");
});

const { error:validationError } = useFetch(`/api/clients/individual/${ForestClientUserSession.user?.userId.split('\\').pop()}?lastName=${ForestClientUserSession.user?.lastName}`);
watch([validationError], () => {
  if (validationError.value.response?.status === 409) {
    updateValidState(-1, false); //-1 to define the error as global
    notificationBus.emit({
      fieldId: "server.validation.error",
      fieldName: '',
      errorMsg: validationError.value.response?.data ?? "",
    })
  }  
});

const districtsList = ref([]);
useFetchTo("/api/districts?page=0&size=250", districtsList);

const formattedDistrictsList = computed(() =>
  districtsList.value.map((district) => ({
    ...district,
    name: `${district.code} - ${district.name}`,
  })),
);

const districtInitialValue = computed(() =>
  districtsList.value.find((district) => district.code === formData.businessInformation.district),
);

const updateDistrict = (value: CodeNameType | undefined) => {
  if (value) {
    formData.businessInformation.district = value.code;
  }
};
</script>

<template>
  <div class="form-header">
    <div class="form-header-title">
      <h1 data-scroll="top">
        New client application
      </h1>
    </div>
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
    
  <div class="form-steps-section">
    <h2 data-scroll="scroll-0">
      Personal information
    </h2>
    <p class="body-compact-01">
      Review the information below. It’s from your BC Services card. We use it to know who we're giving a number to and for communicating with clients. 
    </p>

    <div class="card">
      <div>
        <cds-inline-notification
          v-shadow="2"
          low-contrast="true"
          open="true"
          kind="info"
          hide-close-button="true"
          title="">
          <p class="cds--inline-notification-content">
            <strong>Read-only: </strong>
            If something is incorrect
            <a
              href=""
              target="_blank"
              rel="noopener noreferrer"
              @click.prevent="changePersonalInfoModalActive = true"
              >change your personal information</a
            >
            and then restart your application.
          </p>
        </cds-inline-notification>
        <br /><br />

        <p class="label-01">Full name</p>
        <p class="body-compact-01">{{ formData.businessInformation.businessName }}</p>
      </div>
      <hr class="divider" />
      <div>
        <p class="label-01">Date of birth</p>
        <p class="body-compact-01">{{ figmaFormattedDate }}</p>
      </div>
      <hr class="divider" />
      <div>
        <p class="label-01">Email address</p>
        <p class="body-compact-01">{{ formData.location.contacts[0].email }}</p>
      </div>
      <hr class="divider" />
      <div>
        <p class="label-01">Mailing address</p>
        <p class="body-compact-01">
          {{ formData.businessInformation.address.streetAddress }} <br />
          {{ formData.businessInformation.address.city }}, {{ formData.businessInformation.address.province.value }} <br />
          {{ formData.businessInformation.address.country.text }} <br />
          {{ formData.businessInformation.address.postalCode }}
        </p>
      </div>
    </div>

    <hr class="divider" />

    <h2 data-scroll="focus-0">Natural resource district</h2>
    <p class="body-02">
      Select the district your application should go to. If you don’t know the district
      <a
        href="https://www2.gov.bc.ca/gov/content/industry/forestry/managing-our-forest-resources/ministry-of-forests-lands-and-natural-resource-operations-region-district-contacts"
        target="_blank"
        rel="noopener noreferrer"
        >check this map</a
      >.
    </p>
    <dropdown-input-component
      id="district"
      label="District"
      :initial-value="districtInitialValue?.name"
      required
      required-label
      :model-value="formattedDistrictsList"
      :enabled="true"
      tip=""
      :validations="[
        ...getValidations('businessInformation.district.text'),
        submissionValidation('businessInformation.district.text'),
      ]"
      @update:selected-value="updateDistrict($event)"
      @empty="validation.district = !$event"
    />

    <hr class="divider" />

    <h2 data-scroll="scroll-0">
      Contact information
    </h2>
    <p class="body-compact-01">
      We need your phone number to communicate with you.
    </p>

    <div class="grouping-01">
      <text-input-component
        id="phoneNumberId"
        label="Phone number"
        placeholder="( ) ___-____"
        :enabled="true"
        v-model="formData.location.contacts[0].phoneNumber"
        mask="(###) ###-####"
        required
        :required-label="true"
        :validations="[
          ...getValidations('location.contacts.*.phoneNumber'),
          submissionValidation(`location.contacts[0].phoneNumber`)
        ]"
        :error-message="errorMessage"
        @empty="validation[0] = !$event"
        @error="validation[0] = !$event"
      />
    </div>
    
    <div class="frame-01" v-if="otherContacts.length > 0">
      <div v-for="(contact, index) in otherContacts">
        <hr />

        <div class="grouping-09" :data-scroll="`additional-contact-${index + 1}`">
          <h3>Additional contact</h3>
        </div>

        <contact-group-component
          :key="contactsIdMap.get(contact)"
          :id="contactsIdMap.get(contact)"
          v-bind:modelValue="contact"
          :roleList="roleList"
          :validations="[uniqueValues.add]"
          :enabled="true"
          :revalidate="revalidate"
          :hide-address-name-field="true"
          :required-label="true"
          @update:model-value="updateContact($event, index + 1)"
          @valid="updateValidState(index + 1, $event)"
          @remove="handleRemove(index + 1)"
        />
      </div>
    </div>

    <p 
      class="body-compact-01"
      v-if="formData.location.contacts.length < 5">
      You can add contacts to the account. For example, a person you want to give or receive information on your behalf.
    </p>

    <cds-button
      kind="tertiary"
      @click.prevent="addContact"
      v-if="formData.location.contacts.length < 5">
      <span>Add another contact</span>
      <Add16 slot="icon" />
    </cds-button>

    <p 
      class="body-compact-01"
      v-if="formData.location.contacts.length >= 5">
      You can only add a maximum of 5 contacts.
    </p>

    <hr class="divider" />

    <div class="form-footer-group-next">
      <span class="body-compact-01" v-if="submitBtnDisabled">
        All fields must be filled in correctly to enable the “Submit application” button below.
      </span>
      <cds-tooltip>
        <cds-button
          data-test="wizard-submit-button"
          kind="primary"
          size="lg"
          v-on:click="submit"
          :disabled="submitBtnDisabled"
        >
          <span>Submit application</span>
          <Check16 slot="icon" />
        </cds-button>
        <cds-tooltip-content v-if="!isTouchScreen" v-show="submitBtnDisabled">
          All fields must be filled in correctly.
        </cds-tooltip-content>
      </cds-tooltip>
    </div>

    <cds-modal
      id="address-change-bc-modal"
      size="md"
      :open="changePersonalInfoModalActive"
      @cds-modal-closed="changePersonalInfoModalActive = false"
    >
      <cds-modal-header>
        <cds-modal-close-button></cds-modal-close-button>
        <cds-modal-heading>
          Change your personal information and logout
        </cds-modal-heading>
      </cds-modal-header>
      <cds-modal-body>
        <p>
          Visit
          <a
            href='https://www2.gov.bc.ca/gov/content/governments/government-id/bc-services-card/your-card/change-personal-information' 
            target="_blank"
            rel="noopener noreferrer"
            >Change your personal information</a
          >
          to update your name, address or date of birth.<br /><br />
          Go to your
          <a href="https://id.gov.bc.ca/account/" target="_blank" rel="noopener noreferrer">BC Services account</a>
          to update your email address.<br /><br />
          You can then log back into this application using your BC Services Card.<br /><br />
          Your data will not be saved.
        </p>
      </cds-modal-body>
      <cds-modal-footer>
        <cds-modal-footer-button 
          kind="secondary"
          data-modal-close
          class="cds--modal-close-btn">
          Cancel
        </cds-modal-footer-button>
        
        <cds-modal-footer-button 
          kind="danger" 
          class="cds--modal-submit-btn"
          v-on:click="$session?.logOut"
        >
          Logout
          <Logout16 slot="icon" />
        </cds-modal-footer-button>

      </cds-modal-footer>
    </cds-modal>

  </div>

</template>
