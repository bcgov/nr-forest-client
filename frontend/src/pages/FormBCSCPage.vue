<template>
  <div class="form-header">
    <div class="form-header-title">
      <span class="heading-05" data-scroll="top">
        New client application
      </span>
    </div>
    <error-notification-grouping-component
      :form-data="formData"
      :scroll-to-element-fn="scrollToNewContact"
    />
  </div>
    
  <div class="form-steps-section">
    <span class="heading-04" data-scroll="scroll-0">
      Personal information
    </span>
    <p class="body-compact-01">
      Review the personal information below. It’s from your BC Services card. We use it to know who we're giving a number to and for communicating with clients. 
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
            If something is incorrect visit 
            <a
              href=""
              target="_blank"
              rel="noopener noreferrer"
              @click.prevent="changePersonalInfoModalActive = true"
              >Change your personal information
            </a> 
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
        <p class="label-01">Address</p>
        <p class="body-compact-01">
          {{ formData.businessInformation.address.streetAddress }} <br />
          {{ formData.businessInformation.address.city }}, {{ formData.businessInformation.address.province.value }} <br />
          {{ formData.businessInformation.address.country.text }} <br />
          {{ formData.businessInformation.address.postalCode }}
        </p>
      </div>
    </div>

    <span class="heading-04" data-scroll="scroll-0">
      Contact information
    </span>
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
        :required-label="true"
        :validations="[
          ...getValidations('location.contacts.*.phoneNumber'),
          submissionValidation(`location.contacts[0].phoneNumber`)
        ]"
        :error-message="errorMessage"
        @empty="validation.phoneNumber = !$event"
        @error="validation.phoneNumber = !$event"
      />
    </div>
    
    <div class="frame-01" v-if="otherContacts.length > 0">
      <div v-for="(contact, index) in otherContacts">
        <hr />

        <div class="grouping-09" :data-scroll="`additional-contact-${index + 1}`">
          <span class="heading-03">Additional contact</span>
        </div>

        <contact-group-component
          :key="index + 1"
          :id="index + 1"
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

    <p class="body-compact-01">
      You can add contacts to the account. For example, a person you want to give or receive information on your behalf.
    </p>

    <cds-button
      kind="tertiary"
      @click.prevent="addContact"
      v-if="formData.location.contacts.length < 5">
      <span>Add another contact</span>
      <Add16 slot="icon" />
    </cds-button>

    <hr class="divider" />

    <cds-button
        data-test="wizard-submit-button"
        kind="primary"
        size="lg"
        @click.prevent="submit"
        :disabled="submitBtnDisabled"
      >
      <span>Submit application</span>
      <Check16 slot="icon" />
    </cds-button>

    <cds-modal
      id="help-modal"
      size="sm"
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
            target="_blank">Change your personal information
          </a> 
          to update your name, address or date of birth. Go to your 
          <a 
            href='https://id.gov.bc.ca/account/'
            target="_blank">BC Services account
          </a> 
          to update your email address. 
          You can then log back into this application using your BC Services Card.
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

<script setup lang="ts">
import { ref, reactive, computed, watch, getCurrentInstance, toRef } from "vue";
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
import { useFetchTo, usePost } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
import { useEventBus } from "@vueuse/core";
import {
  codeConversionFn,
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

const { setFocusedComponent } = useFocus();
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
    businessType: getEnumKeyByEnumValue(BusinessTypeEnum, BusinessTypeEnum.U),
    legalType: getEnumKeyByEnumValue(LegalTypeEnum, LegalTypeEnum.SP),
    clientType: getEnumKeyByEnumValue(ClientTypeEnum, ClientTypeEnum.I),
    incorporationNumber: "",
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
  `/api/clients/getCountryByCode/${submitterInformation?.address?.country?.code}`,
  receviedCountry
);

const country = computed(() => {
  return codeConversionFn(receviedCountry.value);
});

watch(country, (newValue) => {
  formData.businessInformation.address = {
    ...formData.businessInformation.address,
    country: { value: newValue.value, text: newValue.text },
    province: codeConversionFn(formData.businessInformation.address.province),
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
    setFocusedComponent("");
  });
};

//Role related data
const roleList = ref([]);
const fetch = () => {
  useFetchTo("/api/clients/activeContactTypeCodes?page=0&size=250", roleList);
};
fetch();

//New contact being added
const otherContacts = computed(() => formData.location.contacts.slice(1));
const addContact = (autoFocus = true) => {
  emptyContact.locationNames.push(defaultLocation);
  const newLength = formData.location.contacts.push(
    JSON.parse(JSON.stringify(emptyContact))
  );
  if (autoFocus) {
    const focusIndex = newLength - 1;
    setFocusedComponent(`role_${focusIndex}`);
  }
  return newLength;
};

const uniqueValues = isUniqueDescriptive();

const removeContact = (index: number) => () => {
  updateContact(undefined, index);
  delete validation[index];
  uniqueValues.remove("Name", index + "");
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

//Validation
const validation = reactive<Record<string, boolean>>({
  0: false,
});

const updateValidState = (index: number, valid: boolean) => {
  if (validation[index] !== valid) {
    validation[index] = valid;
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

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

const bus = useEventBus<ModalNotification>("modal-notification");

const handleRemove = (index: number) => {
  const selectedContact = !isNullOrUndefinedOrBlank(
    formData.location.contacts[index].firstName
  )
    ? `${formData.location.contacts[index].firstName} ${formData.location.contacts[index].lastName}`
    : "Contact #" + index;
  bus.emit({
    message: selectedContact,
    kind: "Contact deleted",
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

let submitBtnDisabled = ref(false);

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
  headers: {
    "x-user-id": submitterInformation?.userId ?? "",
    "x-user-email": submitterInformation?.email ?? "",
    "x-user-name": submitterInformation?.firstName ?? "",
  },
});

const submit = () => {
  errorBus.emit([]);
  notificationBus.emit(undefined);

  if (checkStepValidity(currentTab.value)) {
    submitBtnDisabled.value = true;
    console.log(toRef(formData).value);
    fecthSubmit();
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
      errorMsg: errorItem.errorMsg,
    })
  );
  setScrollPoint("top");
});
</script>
