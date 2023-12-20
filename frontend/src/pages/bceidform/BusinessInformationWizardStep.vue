<script setup lang="ts">
import { watch, computed, ref, reactive } from "vue";
// Carbon
import "@carbon/web-components/es/components/inline-loading/index";
import "@carbon/web-components/es/components/notification/index";
// Importing composables
import { useEventBus } from "@vueuse/core";
import { useFetchTo } from "@/composables/useFetch";
// Importing types
import {
  BusinessSearchResult,
  ClientTypeEnum,
  ProgressNotification,
} from "@/dto/CommonTypesDto";
import { BusinessTypeEnum } from "@/dto/CommonTypesDto";
import type {
  FormDataDto,
  ForestClientDetailsDto,
} from "@/dto/ApplyClientNumberDto";
// Importing validators
import { getValidations } from "@/helpers/validators/GlobalValidators";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
// Importing helper functions
import { retrieveClientType, exportAddress } from "@/helpers/DataConversors";
// Importing session
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
import { getEnumKeyByEnumValue } from "@/services/ForestClientService";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>();

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
      formData.value.businessInformation.clientType === ClientTypeEnum[ClientTypeEnum.RSP]),
);

// validation.birthdate initialization
validation.birthdate = !showBirthDate.value || !!formData.value.businessInformation.businessName;

const autoCompleteUrl = computed(
  () => `/api/clients/name/${formData.value.businessInformation.businessName || ""}`
);

const showAutoCompleteInfo = ref<boolean>(false);
const showGoodStandingError = ref<boolean>(false);
const showDuplicatedError = ref<boolean>(false);
const showDetailsLoading = ref<boolean>(false);
const detailsData = ref(null);

const toggleErrorMessages = (
  goodStanding: boolean | null,
  duplicated: boolean | null
) => {
  showGoodStandingError.value = goodStanding ?? false;
  showDuplicatedError.value = duplicated ?? false;

  if (goodStanding || duplicated) {
    progressIndicatorBus.emit({ kind: "disabled", value: true });
    exitBus.emit({ goodStanding, duplicated });
  } else {
    progressIndicatorBus.emit({ kind: "disabled", value: false });
    exitBus.emit({ goodStanding: false, duplicated: false });
  }
};

//Using this as we have to handle the selected result to get
//incorporation number and client type
const autoCompleteResult = ref<BusinessSearchResult>();
watch([autoCompleteResult], () => {
  // reset business validation state
  validation.business = false;

  if (autoCompleteResult.value && autoCompleteResult.value.code) {
    toggleErrorMessages(false, false);

    formData.value.businessInformation.incorporationNumber =
      autoCompleteResult.value.code;
    formData.value.businessInformation.legalType =
      autoCompleteResult.value.legalType;
    formData.value.businessInformation.clientType = retrieveClientType(
      autoCompleteResult.value.legalType
    );
    showAutoCompleteInfo.value = false;

    emit("update:data", formData.value);

    const config = {
      headers: {
        "x-user-email": formData.value.location.contacts[0].email,
        "x-user-id": formData.value.location.contacts[0].firstName,
      },
    };

    //Also, we will load the backend data to fill all the other information as well
    const { error, loading: detailsLoading } = useFetchTo(
      `/api/clients/${autoCompleteResult.value.code}`,
      detailsData,
      config
    );

    showDetailsLoading.value = true;
    watch(error, () => {
      if (error.value.response?.status === 409) {
        toggleErrorMessages(null, true);
        return;
      }
      if (error.value.response?.status === 404) {
        toggleErrorMessages(null, null);
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
});

watch([detailsData], () => {
  if (detailsData.value) {
    const forestClientDetails: ForestClientDetailsDto = detailsData.value;
    formData.value.location.contacts = [
      formData.value.location.contacts[0],
      ...forestClientDetails.contacts,
    ];
    formData.value.location.addresses = exportAddress(
      forestClientDetails.addresses
    );
    formData.value.businessInformation.goodStandingInd =
      forestClientDetails.goodStanding ? "Y" : "N";
    toggleErrorMessages(!forestClientDetails.goodStanding, null);
    validation.business = forestClientDetails.goodStanding;

    emit("update:data", formData.value);
  }
});

// -- Unregistered Proprietorship
watch([selectedOption], () => {
  if (selectedOption.value === BusinessTypeEnum.U) {
    const fromName = `${ForestClientUserSession.user?.firstName} ${ForestClientUserSession.user?.lastName}`;

    formData.value.businessInformation.businessType = getEnumKeyByEnumValue(BusinessTypeEnum, BusinessTypeEnum.U);
    formData.value.businessInformation.clientType = getEnumKeyByEnumValue(ClientTypeEnum, ClientTypeEnum.USP);
    formData.value.businessInformation.businessName = ForestClientUserSession
      .user?.businessName
      ? ForestClientUserSession.user?.businessName
      : fromName;
    validation.business = true;
    formData.value.businessInformation.goodStandingInd = "Y";
    emit("update:data", formData.value);
  } else {
    formData.value.businessInformation.businessName = "";
    validation.business = false;
    showAutoCompleteInfo.value = true;
  }
});

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
</script>

<template>
  <radio-input-component
    id="businessType"
    label="Type of business"
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
      v-if="(showAutoCompleteInfo && selectedOption === BusinessTypeEnum.R) || showGoodStandingError || showDuplicatedError">
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
              <a href="mailto:BCRegistries&#64;gov.bc.ca">BCRegistries&#64;gov.bc.ca</a>.
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
        <p  class="cds--inline-notification-content">
          Looks like “{{ formData.businessInformation.businessName }}” has a
          client number. Select the 'Receive email and logout' button below to
          have it sent to you at {{ formData.location.contacts[0].email }}
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

  <div
    v-if="
      validation.business &&
      (selectedOption === BusinessTypeEnum.U ||
        formData.businessInformation.clientType === ClientTypeEnum[ClientTypeEnum.RSP])
    "
  >
    <p class="body-02 date-label">
      We need the proprietor's birthdate to confirm their identity
      <span class="cds-text-input-required-label">(required)</span>
    </p>
    <date-input-component
      id="birthdate"
      title="Date of birth"
      v-model="formData.businessInformation.birthdate"
      :enabled="true"
      :validations="[...getValidations('businessInformation.birthdate')]"
      :year-validations="[...getValidations('businessInformation.birthdate.year')]"
      @error="validation.birthdate = !$event"
      @possibly-valid="validation.birthdate = $event"
    />
  </div>
</template>
