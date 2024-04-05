<script setup lang="ts">
import { watch, computed, ref, reactive, getCurrentInstance, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/inline-loading/index";
import "@carbon/web-components/es/components/notification/index";
// Importing composables
import { useEventBus } from "@vueuse/core";
import { useFetch, useFetchTo } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
// Importing types
import {
  BusinessSearchResult,
  ClientTypeEnum,
  ProgressNotification,
} from "@/dto/CommonTypesDto";
import { BusinessTypeEnum, CodeNameType } from "@/dto/CommonTypesDto";
import { locationName as defaultLocation } from "@/dto/ApplyClientNumberDto";
import type {
  FormDataDto,
  ForestClientDetailsDto,
} from "@/dto/ApplyClientNumberDto";
// Importing validators
import { getValidations } from "@/helpers/validators/GlobalValidators";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
// Importing helper functions
import { retrieveClientType, exportAddress } from "@/helpers/DataConversors";
import {
  getEnumKeyByEnumValue,
  openMailtoLink,
  getObfuscatedEmail,
} from "@/services/ForestClientService";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{
  data: FormDataDto;
  active: boolean;
  title: string;
  districtsList: Array<CodeNameType>;
  autoFocus?: boolean;
  individualValid?: boolean;
  setIndividualValid?: (valid: boolean) => void;
}>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

const instance = getCurrentInstance();
const features = instance.appContext.config.globalProperties.$features;
const ForestClientUserSession = instance.appContext.config.globalProperties.$session;

//Defining the event bus to send notifications up
const progressIndicatorBus = useEventBus<ProgressNotification>(
  "progress-indicator-bus"
);
const exitBus =
  useEventBus<Record<string, boolean | null>>("exit-notification");
const generalErrorBus = useEventBus<string>("general-error-notification");

//Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch(
  () => formData.value,
  () => emit("update:data", formData.value)
);

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  businessType: !!formData.value.businessInformation.businessType,
  business: !!formData.value.businessInformation.businessName,
  birthdate: true, // temporary value
  district: false,
  individual: props.individualValid, // occasionally pre-filled value
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", checkValid());

// -- Auto completion --
const selectedOption = computed(() => {
  switch (formData.value.businessInformation.businessType) {
    case "R":
      return BusinessTypeEnum.R;
    case "U":
      return BusinessTypeEnum.U;
    default:
      return BusinessTypeEnum.Unknow;
  }
});

const showBirthDate = computed(
  () =>
    validation.business &&
    (selectedOption.value === BusinessTypeEnum.U ||
      formData.value.businessInformation.clientType ===
        ClientTypeEnum[ClientTypeEnum.RSP])
);

// validation.birthdate initialization
validation.birthdate =
  !showBirthDate.value || !!formData.value.businessInformation.businessName;

const autoCompleteUrl = computed(
  () =>
    `/api/clients/name/${formData.value.businessInformation.businessName || ""}`
);

const showAutoCompleteInfo = ref<boolean>(false);
const showGoodStandingError = ref<boolean>(false);
const showDuplicatedError = ref<boolean>(false);
const showNonPersonSPError = ref<boolean>(false);
const showUnsupportedClientTypeError = ref<boolean>(false);
const showDetailsLoading = ref<boolean>(false);
const detailsData = ref(null);
const soleProprietorOwner = ref<string>("");

const toggleErrorMessages = (
  goodStanding: boolean | null = null,
  duplicated: boolean | null = null,
  nonPersonSP: boolean | null = null,
  unsupportedClientType: boolean | null = null
) => {
  showGoodStandingError.value = goodStanding ?? false;
  showDuplicatedError.value = duplicated ?? false;
  showNonPersonSPError.value = nonPersonSP ?? false;
  showUnsupportedClientTypeError.value = unsupportedClientType ?? false;

  if (goodStanding || duplicated || nonPersonSP || unsupportedClientType) {
    progressIndicatorBus.emit({ kind: "disabled", value: true });
    exitBus.emit({
      goodStanding,
      duplicated,
      nonPersonSP,
      unsupportedClientType,
    });
  } else {
    progressIndicatorBus.emit({ kind: "disabled", value: false });
    exitBus.emit({
      goodStanding: false,
      duplicated: false,
      nonPersonSP: false,
      unsupportedClientType: false,
    });
  }
};

const receivedClientType = ref<CodeNameType>();

//Using this as we have to handle the selected result to get
//registration number and client type
const autoCompleteResult = ref<BusinessSearchResult>();
watch([autoCompleteResult], () => {
  // reset business validation state
  validation.business = false;

  if (autoCompleteResult.value && autoCompleteResult.value.code) {
    toggleErrorMessages(false, false, false);

    formData.value.businessInformation.registrationNumber =
      autoCompleteResult.value.code;
    formData.value.businessInformation.legalType =
      autoCompleteResult.value.legalType;
    formData.value.businessInformation.clientType = retrieveClientType(
      autoCompleteResult.value.legalType
    );
    showAutoCompleteInfo.value = false;

    emit("update:data", formData.value);

    //Also, we will load the backend data to fill all the other information as well
    const { error, loading: detailsLoading } = useFetchTo(
      `/api/clients/${autoCompleteResult.value.code}`,
      detailsData,
      {}
    );

    showDetailsLoading.value = true;
    watch(error, () => {
      if (error.value.response?.status === 409) {
        toggleErrorMessages(null, true, null);
        return;
      }
      if (error.value.response?.status === 422) {
        toggleErrorMessages(null, null, true);
        return;
      }
      if (error.value.response?.status === 406) {
        toggleErrorMessages(null, null, null, true);
        receivedClientType.value = null;
        useFetchTo(
          `/api/codes/clientTypes/${formData.value.businessInformation.clientType}`,
          receivedClientType
        );
        return;
      }
      if (error.value.response?.status === 404) {
        toggleErrorMessages();
        validation.business = true;
        emit("update:data", formData.value);
        return;
      }
      // @ts-ignore
      generalErrorBus.emit(error.value.response?.data.message);
    });

    watch(
      [detailsLoading],
      () => (showDetailsLoading.value = detailsLoading.value)
    );
  }

  if (!showBirthDate.value) {
    validation.individual = true;
  }
});

/**
 * Checks if an individual exists for provided last name.
 * @param {string} lastName - The last name to check.
 */
const checkForIndividualValid = (lastName: string) => {
  const { error: validationError, response: individualResponse } = useFetch(
    `/api/clients/individual/${ForestClientUserSession.user?.userId
      .split("\\")
      .pop()}?lastName=${lastName}`
  );

  // reset validation
  validation.individual = false;

  watch(validationError, (watchValue) => {
    if (watchValue.response?.status === 409) {
      validation.business = false;
      toggleErrorMessages(null, true, null);
      generalErrorBus.emit(watchValue.response?.data ?? "");
    }else{
      validation.individual = true;
    }
  });
  watch(individualResponse, (watchValue) => {
    if(watchValue.status === 200){
      validation.individual = true;
    }
  });
};

watch(
  () => validation.individual,
  () => {
    props.setIndividualValid?.(validation.individual);
  },
);

watch([detailsData], () => {
  if (detailsData.value) {
    const forestClientDetails: ForestClientDetailsDto = detailsData.value;
    
    if (!features.BCEID_MULTI_ADDRESS) {
      forestClientDetails?.contacts?.forEach((contact) => {
        contact.locationNames = [{ ...defaultLocation }];
      });
    }

    formData.value.location.contacts = [
      formData.value.location.contacts[0],
      ...forestClientDetails.contacts,
    ];

    formData.value.location.addresses = exportAddress(
      forestClientDetails.addresses
    );

    formData.value.businessInformation.goodStandingInd =
      (forestClientDetails.goodStanding ?? false) ? "Y" : "N";

    toggleErrorMessages(
      (forestClientDetails.goodStanding ?? false) ? false : true,
      null
    );

    validation.business = forestClientDetails.goodStanding;

    emit("update:data", formData.value);

    // reset soleProprietorOwner
    soleProprietorOwner.value = "";

    if (forestClientDetails.contacts.length > 0) {
      soleProprietorOwner.value = forestClientDetails.contacts[0].lastName;
    }
  }
});

/**
 * Computed property that allows checks if the birthdate is valid and should be shown.
 *
 * @returns {boolean} - Returns true if the birthdate is valid and birthdate should be shown, otherwise returns false.
 */
const individualCheck = computed(() => {
  return showBirthDate.value;
});

watch([individualCheck, selectedOption, soleProprietorOwner], ([individualCheckValue]) => {
  if (individualCheckValue) {
    checkForIndividualValid(
      soleProprietorOwner.value || ForestClientUserSession.user?.lastName
    );
  }
});

watch([selectedOption], () => {
  toggleErrorMessages();

  // Unregistered Proprietorship
  if (selectedOption.value === BusinessTypeEnum.U) {
    const fromName = `${ForestClientUserSession.user?.firstName} ${ForestClientUserSession.user?.lastName}`;

    formData.value.businessInformation.businessType = getEnumKeyByEnumValue(
      BusinessTypeEnum,
      BusinessTypeEnum.U
    );
    formData.value.businessInformation.clientType = getEnumKeyByEnumValue(
      ClientTypeEnum,
      ClientTypeEnum.USP
    );
    formData.value.businessInformation.businessName = ForestClientUserSession
      .user?.businessName
      ? ForestClientUserSession.user?.businessName
      : fromName;
    validation.business = true;
    formData.value.businessInformation.goodStandingInd = "Y";
    emit("update:data", formData.value);
  } else {
    // Registered business
    formData.value.businessInformation.businessName = "";
    validation.business = false;
    showAutoCompleteInfo.value = true;
  }
});

watch(
  () => formData.value.businessInformation.businessName,
  () => {
    toggleErrorMessages();
  }
);

watch(showBirthDate, (value) => {
  if (value) {
    validation.birthdate = !!formData.value.businessInformation.birthdate;
  } else {
    // Reset birthdate.
    formData.value.businessInformation.birthdate = "";

    // Consider birthdate valid so it doesn't interfere with the overall validation status.
    validation.birthdate = true;
  }
});

const bcRegistryEmail = "BCRegistries@gov.bc.ca";

const districtInitialValue = computed(() =>
  props.districtsList.find(
    (district) => district.code === formData.value.businessInformation.district
  )
);

const updateDistrict = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.businessInformation.district = value.code;
  }
};

const { setFocusedComponent } = useFocus();
onMounted(() => {
  if (props.autoFocus) {
    setFocusedComponent("focus-0", 0);
  }
});
</script>

<template>
  <h2 data-focus="focus-0" tabindex="-1">
    <div data-scroll="step-title" class="header-offset"></div>
    <!-- This is not the title of the step, but it's the first section. -->
    Natural resource district
  </h2>
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
    :model-value="districtsList"
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

  <h2>
    {{ title }}
  </h2>

  <div class="frame-01">
    <radio-input-component
      id="businessType"
      label="Type of business"
      required
      required-label
      :initialValue="formData?.businessInformation?.businessType"
      :modelValue="[
        {
          value: 'R',
          text: 'I have a BC registered business (corporation, sole proprietorship, society, etc.)'
        },
        { value: 'U', text: 'I have an unregistered sole proprietorship' }
      ]"
      :validations="[...getValidations('businessInformation.businessType'),submissionValidation('businessInformation.businessType')]"
      @update:model-value="
        formData.businessInformation.businessType = $event ?? ''
      "
      @empty="validation.businessType = !$event"
    />

    <data-fetcher
      v-model:url="autoCompleteUrl"
      :min-length="3"
      :init-value="[]"
      :init-fetch="false"
      #="{ content, loading, error }"
    >
      <AutoCompleteInputComponent
        v-if="selectedOption === BusinessTypeEnum.R"
        id="business"
        label="BC registered business name"
        autocomplete="organization"
        required
        required-label
        tip="Start typing to search for your BC registered business name"
        v-model="formData.businessInformation.businessName"
        :contents="content"
        :validations="[
          ...getValidations('businessInformation.businessName'),
          submissionValidation('businessInformation.businessName'),
          submissionValidation('businessInformation.clientType')
          ]"
        :loading="loading"
        @update:selected-value="autoCompleteResult = $event"
        @update:model-value="validation.business = false"
      />

      <cds-inline-loading status="active" v-if="showDetailsLoading">Loading client details...</cds-inline-loading>
      <div
        class="grouping-02"
        v-if="
          (showAutoCompleteInfo && selectedOption === BusinessTypeEnum.R) ||
          showGoodStandingError ||
          showDuplicatedError ||
          showNonPersonSPError ||
          showUnsupportedClientTypeError
        "
      >
        <cds-inline-notification
          v-shadow="2"
          v-if="showAutoCompleteInfo && selectedOption === BusinessTypeEnum.R"
          low-contrast="true"
          open="true"
          kind="info"
          hide-close-button="true"
          title="If the name of your registered business does not appear in the list, follow these steps:">
          <div class="cds--inline-notification-content">
            <ol type="1" class="bulleted-list">
              <li class="body-compact-01">
                Log into Manage Account in
                <a
                  href="https://www.bceid.ca/"
                  target="_blank"
                  rel="noopener noreferrer"
                  >BCeID</a
                >
                to find your business name
              </li>
              <li class="body-compact-01">
                If your name isn’t there, call BC Registry toll free at
                <a href="tel:18775261526">1-877-526-1526</a> or email them at
                <button id="bcRegistryEmailId" 
                        class="link-button" 
                        @click="openMailtoLink(bcRegistryEmail)" 
                        aria-label="Contact BC Registry via Email">
                  <span v-bind:innerHTML="getObfuscatedEmail(bcRegistryEmail)"></span>
                </button>.
              </li>
            </ol>
          </div>
        </cds-inline-notification>

        <cds-inline-notification
          v-if="showGoodStandingError"
          hide-close-button="true"
          low-contrast="true"
          open="true"
          kind="error"
          title="Not in good standing with BC Registries"
        >
          <p class="cds--inline-notification-content">
            Your request for a client number cannot go ahead because “{{
              formData.businessInformation.businessName
            }}” is not in good standing with BC Registries. Go to your
            <a
              href="https://www.bcregistry.gov.bc.ca/"
              target="_blank"
              rel="noopener noreferrer"
              >BC Registries</a
            >
            account to find out why.
          </p>
        </cds-inline-notification>

        <cds-inline-notification
          v-if="showDuplicatedError"
          hide-close-button="true"
          low-contrast="true"
          open="true"
          kind="error"
          title="Client already exists"
        >
          <p class="cds--inline-notification-content">
            Looks like “{{ formData.businessInformation.businessName }}” has a
            client number. Select the 'Receive email and logout' button below to
            have it sent to you at {{ formData.location.contacts[0].email }}
          </p>
        </cds-inline-notification>

        <cds-inline-notification
          v-if="showNonPersonSPError"
          hide-close-button="true"
          low-contrast="true"
          open="true"
          kind="error"
          title="Unknown sole proprietor"
        >
          <p  class="cds--inline-notification-content">
            We're unable to complete this application because we cannot identify the person who is the sole proprietor. Please email FORHVAP.CLIADMIN@gov.bc.ca for help.
          </p>
        </cds-inline-notification>

        <cds-inline-notification
          v-if="showUnsupportedClientTypeError && receivedClientType"
          hide-close-button="true"
          low-contrast="true"
          open="true"
          kind="error"
          title="Client type not supported"
        >
          <p class="cds--inline-notification-content">
            {{ receivedClientType.name }} client type is not supported. Please email
            FORHVAP.CLIADMIN@gov.bc.ca for help.
          </p>
        </cds-inline-notification>
        
      </div>
    </data-fetcher>

    <text-input-component
      v-if="selectedOption === BusinessTypeEnum.U"
      id="businessName"
      label="Unregistered sole proprietorship"
      placeholder=""
      v-model="formData.businessInformation.businessName"
      :validations="[]"
      :enabled="false"
    />

    <div v-if="showBirthDate">
      <p class="body-02 date-label">
        We need the proprietor's birthdate to confirm their identity
        <span class="cds-text-input-required-label">(required)</span>
      </p>
      <date-input-component
        id="birthdate"
        title="Date of birth"
        :autocomplete="['bday-year', 'bday-month', 'bday-day']"
        v-model="formData.businessInformation.birthdate"
        :enabled="true"
        :validations="[...getValidations('businessInformation.birthdate')]"
        :year-validations="[...getValidations('businessInformation.birthdate.year')]"
        @error="validation.birthdate = !$event"
        @possibly-valid="validation.birthdate = $event"
        required
      />
    </div>
  </div>
</template>
