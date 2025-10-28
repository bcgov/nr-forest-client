<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/inline-loading/index";
import "@carbon/web-components/es/components/notification/index";
// Importing composables
import { useEventBus } from "@vueuse/core";
import { useFetchTo } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
// Importing types
import type {
  BusinessSearchResult,
  ProgressNotification,
  FuzzyMatcherEvent,
  ValidationMessageType
} from "@/dto/CommonTypesDto";
import {
  type FormDataDto,
  type ForestClientDetailsDto,
  indexedEmptyAddress,
  indexedEmptyContact,
  newFormDataDto,
  defaultLocation,
  defaultContactType,
} from "@/dto/ApplyClientNumberDto";
import { adminEmail, getEnumKeyByEnumValue, getObfuscatedEmailLink } from "@/services/ForestClientService";
import { BusinessTypeEnum } from "@/dto/CommonTypesDto";
// Importing helper functions
import { retrieveClientType, retrieveLegalTypeDesc } from "@/helpers/DataConverters";
import { formatAddresses } from "@/dto/ApplyClientNumberDto";
// Importing validators
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
// @ts-ignore
import Information16 from "@carbon/icons-vue/es/information/16";
import Check16 from "@carbon/icons-vue/es/checkmark--filled/20";
import Warning20 from "@carbon/icons-vue/es/warning--filled/20";

// Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{
  data: FormDataDto;
  active: boolean;
  autoFocus?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

//Defining the event bus to send notifications up
const progressIndicatorBus = useEventBus<ProgressNotification>(
  "progress-indicator-bus"
);
const exitBus =
  useEventBus<Record<string, boolean | null>>("exit-notification");
const fuzzyBus = useEventBus<FuzzyMatcherEvent>("fuzzy-error-notification");
const errorBus = useEventBus<ValidationMessageType[]>(
  "submission-error-notification"
);

// Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
const showUnsupportedLegalTypeError = ref<boolean>(false); 

watch(
  () => formData.value,
  () => emit("update:data", formData.value)
);

const getNewValidation = () => ({
  business: false,
  workSafeBCNumber: true,
  doingBusinessAs: true,
  acronym: true,
});

const validation = reactive<Record<string, boolean>>(getNewValidation());

const resetValidation = () => {
  for (const key in validation) {
    delete validation[key];
  }
  Object.assign(validation, getNewValidation());
};

const showDetailsLoading = ref<boolean>(false);
const detailsData = ref(null);
const soleProprietorOwner = ref<string>("");
const bcRegistryError = ref<boolean>(false);
const showOnError = ref<boolean>(false);

const showFields = computed(() => {
  return (
    formData.value.businessInformation.clientType &&
    formData.value.businessInformation.registrationNumber &&
    formData.value.businessInformation.legalType &&
    validation.business
  );
});

const standingMessage = computed(() => {
  if (formData.value.businessInformation.goodStandingInd === "Y")
    return "Good standing";
  if (formData.value.businessInformation.goodStandingInd === "N")
    return "Not in good standing";
  return "Unknown";
});

//We're using DataConverters.ts for this since it's not being saved to the database, so creating a table isn't necessary.
const legalTypeText = computed(() =>
  retrieveLegalTypeDesc(formData.value.businessInformation.legalType)
);

const autoCompleteUrl = computed(
  () =>
    `/api/clients/name/${formData.value.businessInformation.businessName || ""}`
);

const standingValue = (goodStanding: boolean | null | undefined) => {
  if (goodStanding === true) return "Y";
  if (goodStanding === false) return "N";
  return null;
};

const autoCompleteResult = ref<BusinessSearchResult>();
watch([autoCompleteResult], () => {
  // reset business validation state
  resetValidation();

  detailsData.value = null;
  bcRegistryError.value = false;
  showOnError.value = false;

  // reset businessInformation
  Object.assign(formData.value.businessInformation, {
    ...newFormDataDto().businessInformation,
    businessName: formData.value.businessInformation.businessName,
  });

  if (autoCompleteResult.value && autoCompleteResult.value.code) {
    formData.value.businessInformation.registrationNumber = autoCompleteResult.value.code;
    formData.value.businessInformation.legalType = autoCompleteResult.value.legalType;
    formData.value.businessInformation.clientType = retrieveClientType(
      autoCompleteResult.value.legalType
    );
    formData.value.businessInformation.businessType = getEnumKeyByEnumValue(BusinessTypeEnum, BusinessTypeEnum.R);

    emit("update:data", formData.value);

    if (formData.value.businessInformation.clientType === "RSP") {
      validation.birthdate = false;
    }

    const toggleErrorMessages = (
      unsupportedLegalType: boolean | null = null,
    ) => {
      showUnsupportedLegalTypeError.value = unsupportedLegalType ?? false;

      if (unsupportedLegalType) {
        progressIndicatorBus.emit({ kind: "disabled", value: true });
        exitBus.emit({
          unsupportedLegalType
        });
      } else {
        progressIndicatorBus.emit({ kind: "disabled", value: false });
        exitBus.emit({
          unsupportedLegalType: false
        });
      }
    };

    //Also, we will load the backend data to fill all the other information as well
    const {
      error,
      loading: detailsLoading,
      handleErrorDefault,
    } = useFetchTo(
      `/api/clients/${autoCompleteResult.value.code}`,
      detailsData,
      {
        skipDefaultErrorHandling: true,
      }
    );

    showDetailsLoading.value = true;
    watch(error, () => {
      if (error.value.response?.status === 409) {
        validation.business = false;
        showOnError.value = true;

        errorBus.emit(
          [
            {
              fieldId: "businessInformation.businessName",
              fieldName: "Client name",
              errorMsg: "Client already exists",
            },
          ],
          {
            skipNotification: true,
          }
        );

        fuzzyBus.emit({
          id: "global",
          matches: [
            {
              field: "businessInformation.clientName",
              match: (error.value.response.data as string).split(
                "client number"
              )[1],
              fuzzy: false,
              partialMatch: false,
            },
          ],
        });
        return;
      }

      //Here we only care for 5xx and 408 errors
      if (
        error.value.response?.status >= 500 ||
        error.value.response?.status === 408
      ) {
        handleErrorDefault();
        bcRegistryError.value = true;
        return;
      }

      if (error.value.response?.status === 406) {
        toggleErrorMessages(true);
        return;
      }

      //All other know errors are fine and can proceed
      validation.business = true;
      emit("update:data", formData.value);
    });

    watch(
      [detailsLoading],
      () => (showDetailsLoading.value = detailsLoading.value)
    );
  }
});

watch([detailsData], () => {
  if (detailsData.value) {
    fuzzyBus.emit({ id: "", matches: [] });
    const forestClientDetails: ForestClientDetailsDto = detailsData.value;

    if (
      formData.value.businessInformation.clientType === "RSP" &&
      !forestClientDetails.isOwnedByPerson
    ) {
      errorBus.emit(
        [
          {
            fieldId: "businessInformation.businessName",
            fieldName: "Client name",
            errorMsg: "This sole proprietor is not owned by a person",
          },
        ],
        {
          skipNotification: true,
        }
      );

      fuzzyBus.emit(
        {
          id: "global",
          matches: [
            {
              field: "businessInformation.notOwnedByPerson",
              match: "",
              fuzzy: false,
              partialMatch: false,
            },
          ],
        },
        {
          title: "Sole proprietor not owned by a person",
          message:
            'This sole proprietor is not owned by a person. Please select the "Unregistered company" client type to proceed.',
        }
      );
      validation.soleproprietor = false;
    } else {
      validation.soleproprietor = true;
    }

    if (formData.value.businessInformation.clientType === "RSP") {
      formData.value.businessInformation.doingBusinessAs =
        forestClientDetails.name;
      formData.value.businessInformation.firstName =
        forestClientDetails.contacts[0]?.firstName;
      formData.value.businessInformation.lastName =
        forestClientDetails.contacts[0]?.lastName;
    }

    const receivedAddresses = forestClientDetails.addresses ?? [ indexedEmptyAddress(0) ];
    receivedAddresses.forEach((address) => {
      address.complementaryAddressOne = null;
      address.complementaryAddressTwo = null;
      address.businessPhoneNumber = null;
      address.secondaryPhoneNumber = null;
      address.faxNumber = null;
      address.emailAddress = null;
      address.notes = null;
    });
    formData.value.location.addresses = formatAddresses(receivedAddresses);

    const receivedContacts = forestClientDetails.contacts ?? [ indexedEmptyContact(0) ];
    receivedContacts.forEach((contact) => {
      contact.locationNames = [];
    });

    formData.value.location.contacts = receivedContacts;

    if (formData.value.location.contacts.length == 0) {
      formData.value.location.contacts = [ indexedEmptyContact(0) ];
    }
    formData.value.location.contacts.forEach((contact) => {
      contact.locationNames = [
        // The default location `value` with the updated name
        {
          value: defaultLocation.value,
          text: formData.value.location.addresses[0].locationName,
        },
      ];
      contact.contactType = defaultContactType;
    });

    formData.value.businessInformation.goodStandingInd = standingValue(
      forestClientDetails.goodStanding
    );

    //FSADT1-1388 standing is not a factor that prevents a submission
    validation.business = true;

    emit("update:data", formData.value);

    // reset soleProprietorOwner
    soleProprietorOwner.value = "";

    if (forestClientDetails.contacts.length > 0) {
      soleProprietorOwner.value = forestClientDetails.contacts[0].lastName;
    }
  }
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", checkValid());

if (formData.value.businessInformation.businessName) {
  validation.business = true;
}
watch(
  () => formData.value.businessInformation.businessName,
  () => {
    fuzzyBus.emit(undefined);

    formData.value.businessInformation.registrationNumber = "";
    formData.value.businessInformation.legalType = "";
    formData.value.businessInformation.clientType = "";

    showOnError.value = false;
  },
);

const { setFocusedComponent } = useFocus();
onMounted(() => {
  if (props.autoFocus) {
    setFocusedComponent("focus-0", 0);
  }
});
</script>

<template>
  <div class="frame-01">
    <data-fetcher
      v-model:url="autoCompleteUrl"
      :min-length="3"
      :init-value="[]"
      :init-fetch="false"
      :disabled="!formData.businessInformation.businessName"
      #="{ content, loading, error }"
    >
      <AutoCompleteInputComponent
        id="businessName"
        label="Client name"
        autocomplete="off"
        required
        required-label
        tip="Enter client name and then select from the list"
        v-model="formData.businessInformation.businessName"
        :contents="content"
        :validations="[
          ...getValidations('businessInformation.businessName'),
          submissionValidation('businessInformation.businessName'),
          ]"
        :loading="loading"
        @update:selected-value="autoCompleteResult = $event"
        @update:model-value="validation.business = false"
        />
        <cds-inline-loading status="active" v-if="showDetailsLoading">Loading client details...</cds-inline-loading>
        
        <cds-inline-notification
          data-text="Client information"
          v-shadow="2"
          id="bcRegistryDownNotification"
          v-if="bcRegistryError || (error?.response?.status ?? false)"
          low-contrast="true"
          open="true"
          kind="error"
          hide-close-button="true"
          title="BC Registries is down">
            <span class="body-compact-01"> You'll need to try again later.</span>      
        </cds-inline-notification>

        <cds-inline-notification
          data-text="Client information"
          v-if="showUnsupportedLegalTypeError"
          hide-close-button="true"
          low-contrast="true"
          open="true"
          kind="error"
          title="Legal type not supported"
        >
          <p class="cds--inline-notification-content">
            The legal type of this client is not supported. Please email
            <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span> for help.
          </p>
        </cds-inline-notification>
    </data-fetcher>

    <cds-inline-notification
      data-text="Client information"
      v-shadow="2"
      id="bcRegistrySearchNotification"
      v-if="!formData.businessInformation.clientType"
      low-contrast="true"
      open="true"
      kind="info"
      hide-close-button="true"
      title="If the business does not appear in the list">
      <div class="cds--inline-notification-content">
        <ol type="1" class="bulleted-list">
          <li class="body-compact-01">Ask the applicant if it’s registered with BC Registries</li>
          <li class="body-compact-01">If it’s not registered, tell them it must be before the application can go ahead</li>
        </ol>
      </div>
    </cds-inline-notification>

    <div v-if="showFields || showOnError" class="read-only-box">

      <cds-inline-notification
      data-text="Client information"
      id="readOnlyNotification"
      v-shadow="2"
      low-contrast="true"
      open="true"
      kind="info"
      hide-close-button="true"
      title="Read-only">
        <div class="body-compact-01">This information is from BC Registries</div>
      </cds-inline-notification>

      <read-only-component label="Type" id="legalType">    
        <span class="body-compact-01">{{ legalTypeText }}</span>
      </read-only-component>

      <hr class="divider" />

      <read-only-component label="Registration number" id="registrationNumber">
        <template v-slot:default>
        <span class="body-compact-01">{{ formData.businessInformation.registrationNumber }}</span>    
        </template>
      </read-only-component>

      <hr class="divider" />

      <cds-inline-notification
      data-text="Client information"
        v-shadow="2"
        id="notGoodStandingNotification"
        v-if="formData.businessInformation.goodStandingInd === 'N'"
        low-contrast="true"
        open="true"
        kind="warning"
        hide-close-button="true"
        title="Not in good standing">
        <div class="body-compact-01">Check <a
              href="https://www.bcregistry.gov.bc.ca/"
              target="_blank"
              rel="noopener noreferrer"
              >BC Registries</a
            > for the reason.</div>
        <div class="body-compact-01"> You may go ahead but inform applicant of their standing.</div>
      </cds-inline-notification>

      <cds-inline-notification
      data-text="Client information"
        v-shadow="2"
        id="unknownStandingNotification"
        v-if="formData.businessInformation.goodStandingInd !== 'N' && formData.businessInformation.goodStandingInd !== 'Y'"
        low-contrast="true"
        open="true"
        kind="warning"
        hide-close-button="true"
        title="Unknown standing">
        <div class="body-compact-01">Check <a
              href="https://www.bcregistry.gov.bc.ca/"
              target="_blank"
              rel="noopener noreferrer"
              >BC Registries</a
            > before going ahead.</div>
      </cds-inline-notification>

      <read-only-component label="BC Registries standing" id="goodStanding">
        <div class="internal-grouping-01">
          <span class="body-compact-01">{{ standingMessage }}</span>
          <Check16 v-if="formData.businessInformation.goodStandingInd === 'Y'" class="good" />
          <Warning20 v-if="formData.businessInformation.goodStandingInd !== 'Y'" class="warning" />
        </div>
      </read-only-component>

    </div>

    <template v-if="showFields || bcRegistryError || showOnError">
      <div v-if="formData.businessInformation.clientType === 'RSP'">
        <div class="label-with-icon line-height-0 parent-label">
          <div class="cds-text-input-label">
            <span class="cds-text-input-required-label">* </span>
            <span>Date of birth</span>
          </div>
          <cds-tooltip>
            <Information16 />
            <cds-tooltip-content>
              We need the applicant's birthdate to confirm their identity
            </cds-tooltip-content>
          </cds-tooltip>
        </div>
        <date-input-component
          id="birthdate"
          title="Date of birth"
          :autocomplete="['bday-year', 'bday-month', 'bday-day']"
          v-model="formData.businessInformation.birthdate"
          :enabled="true"
          :validations="[
            ...getValidations('businessInformation.birthdate'),
            submissionValidation('businessInformation.birthdate'),
          ]"
          :year-validations="[...getValidations('businessInformation.birthdate.year')]"
          @error="validation.birthdate = !$event"
          @possibly-valid="validation.birthdate = $event"
          required
        />
      </div>

      <text-input-component
        id="workSafeBCNumber"
        label="WorkSafeBC number"
        mask="######"
        placeholder=""
        autocomplete="off"
        v-model="formData.businessInformation.workSafeBcNumber"
        :validations="[
          ...getValidations('businessInformation.workSafeBcNumber'),
          submissionValidation(`businessInformation.workSafeBcNumber`),
        ]"
        enabled
        @empty="validation.workSafeBCNumber = true"
        @error="validation.workSafeBCNumber = !$event"
      />

      <text-input-component
        id="doingBusinessAs"
        v-if="formData.businessInformation.clientType !== 'RSP'"
        label="Doing business as"
        placeholder=""
        autocomplete="off"
        v-model="formData.businessInformation.doingBusinessAs"
        :validations="[
          ...getValidations('businessInformation.doingBusinessAs'),
          submissionValidation(`businessInformation.doingBusinessAs`),
        ]"
        enabled
        @empty="validation.doingBusinessAs = true"
        @error="validation.doingBusinessAs = !$event"
      />

      <text-input-component
        id="acronym"
        label="Acronym"
        placeholder=""
        mask="NNNNNNNN"
        autocomplete="off"
        v-model="formData.businessInformation.clientAcronym"
        :validations="[
          ...getValidations('businessInformation.clientAcronym'),
          submissionValidation(`businessInformation.clientAcronym`),
        ]"
        enabled
        @empty="validation.acronym = true"
        @error="validation.acronym = !$event"
      />
    </template>
  </div>
</template>

<style scoped>
.read-only-box{
  margin-bottom: 1rem;
  border-radius: 0.75rem;  
  border: solid 1px var(--border-subtle-02);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 2.5rem;
}

.internal-grouping-01{
  display: flex;
  gap: 0.5rem;
}
</style>
