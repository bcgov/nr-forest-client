<script setup lang="ts">
import { ref, reactive, computed, watch, toRef, onMounted } from "vue";
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
import { BusinessTypeEnum, CodeNameType, LegalTypeEnum } from "@/dto/CommonTypesDto";
import type {
  FormDataDto,
  ForestClientDetailsDto,
} from "@/dto/ApplyClientNumberDto";
// Importing helper functions
import { retrieveClientType, exportAddress } from "@/helpers/DataConversors";
// @ts-ignore
import Information16 from "@carbon/icons-vue/es/information/16";
import Check16 from "@carbon/icons-vue/es/checkmark--filled/20";
import Warning16 from "@carbon/icons-vue/es/warning--filled/20";
import { locationName } from '../../dto/ApplyClientNumberDto';

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
const exitBus = useEventBus<Record<string, boolean | null>>("exit-notification");


// Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch(
  () => formData.value,
  () => emit("update:data", formData.value)
);

const validation = reactive<Record<string, boolean>>({
  business: false,
  workSafeBCNumber: true,
  doingBusinessAs: true,
  acronym: true,
});

// Defining the error message toggles
const showAutoCompleteInfo = ref<boolean>(false);
const showGoodStandingError = ref<boolean>(false);
const showDuplicatedError = ref<boolean>(false);
const showNonPersonSPError = ref<boolean>(false);
const showUnsupportedClientTypeError = ref<boolean>(false);
const showDetailsLoading = ref<boolean>(false);
const detailsData = ref(null);
const soleProprietorOwner = ref<string>("");
const receivedClientType = ref<CodeNameType>();

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

const showFields = computed(() => {
  return (
    formData.value.businessInformation.clientType &&
    formData.value.businessInformation.registrationNumber &&
    formData.value.businessInformation.legalType &&
    detailsData.value
  );
});

const standingMessage = computed(() =>{
  if(formData.value.businessInformation.goodStandingInd === 'Y') return 'Good standing'
  if(formData.value.businessInformation.goodStandingInd === 'N') return 'Not in good standing'
  return 'Unknow'
});

//TODO: Either load from BE or add to DataConversors.ts
const legalTypeText = computed(() => {
  if (formData.value.businessInformation.legalType === "C") {
    return "Corporation";
  }
  if (formData.value.businessInformation.legalType === "SP") {
    return "Sole proprietorship";
  }
  if (formData.value.businessInformation.legalType === "GP") {
    return "General Partnership";
  }
  return formData.value.businessInformation.legalType + " (Unknown)";
});

const autoCompleteUrl = computed(
  () =>
    `/api/clients/name/${formData.value.businessInformation.businessName || ""}`
);
const autoCompleteResult = ref<BusinessSearchResult>();
  watch([autoCompleteResult], () => {
  // reset business validation state
  validation.business = false;
  detailsData.value = null;

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
    const {
      error,
      loading: detailsLoading,
      handleErrorDefault,
    } = useFetchTo(`/api/clients/${autoCompleteResult.value.code}`, detailsData, {
      skipDefaultErrorHandling: true,
    });

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
          `/api/codes/client-types/${formData.value.businessInformation.clientType}`,
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
      handleErrorDefault();
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
        
    const receivedContacts = forestClientDetails.contacts ?? [];
    receivedContacts.forEach((contact) => {
      contact.locationNames = [];
    });

    formData.value.location.contacts = forestClientDetails.contacts;

    const receivedAddresses = forestClientDetails.addresses ?? [];
    receivedAddresses.forEach((address) => {
      address.complementaryAddressOne = null;
      address.complementaryAddressTwo = null;
      address.businessPhoneNumber = null;
      address.secondaryPhoneNumber = null;
      address.faxNumber = null;
      address.emailAddress = null;
      address.notes = null;
    });
    formData.value.location.addresses = exportAddress(receivedAddresses);

    formData.value.businessInformation.goodStandingInd =
      forestClientDetails.goodStanding ? "Y" : forestClientDetails.goodStanding === false ? "N" : null;
    
    toggleErrorMessages(
      (forestClientDetails.goodStanding ?? true) ? false : true,
      null
    );

    validation.business = (forestClientDetails.goodStanding ?? true);

    emit("update:data", formData.value);

    // reset soleProprietorOwner
    soleProprietorOwner.value = "";

    if (forestClientDetails.contacts.length > 0) {
      soleProprietorOwner.value = forestClientDetails.contacts[0].lastName;
    }

    console.log("Data loaded", formData.value);
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
        tip=""
        v-model="formData.businessInformation.businessName"
        :contents="content"
        :validations="[]"
        :loading="loading"
        @update:selected-value="autoCompleteResult = $event"
        @update:model-value="validation.business = false"
        />
        <cds-inline-loading status="active" v-if="showDetailsLoading">Loading client details...</cds-inline-loading>
    </data-fetcher>

    <cds-inline-notification
      v-shadow="2"
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

    <div v-if="showFields" class="read-only-box">

      <cds-inline-notification
      v-shadow="2"
      v-if="formData.businessInformation.goodStandingInd === 'Y'"
      low-contrast="true"
      open="true"
      kind="info"
      hide-close-button="true"
      title="Read-only">
        <div class="body-compact-01">This information is from BC Registries</div>
      </cds-inline-notification>

      <read-only-component label="Type" v-if="showFields">    
        <span class="body-compact-01">{{ legalTypeText }}</span>
      </read-only-component>

      <hr class="divider" />

      <read-only-component label="Registration number" v-if="showFields">
        <template v-slot:icon>
          <Information16 />
        </template>
        <template v-slot:default>
        <span class="body-compact-01">{{ formData.businessInformation.registrationNumber }}</span>    
        </template>
      </read-only-component>

      <hr class="divider" />

      <cds-inline-notification
        v-shadow="2"
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
        v-shadow="2"
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

      <read-only-component label="BC Registries standing" v-if="showFields">
        <div class="internal-grouping-01">
          <span class="body-compact-01">{{ standingMessage }}</span>
          <Check16 v-if="formData.businessInformation.goodStandingInd === 'Y'" class="good" />
          <Warning16 v-if="formData.businessInformation.goodStandingInd !== 'Y'" class="warning" />
        </div>
      </read-only-component>

    </div>

    <text-input-component
      id="workSafeBCNumber"
      v-if="showFields"
      label="WorkSafeBC number"
      placeholder=""
      autocomplete="off"
      v-model="formData.businessInformation.workSafeBCNumber"
      :validations="[]"
      enabled
      @empty="validation.workSafeBCNumber = true"
      @error="validation.workSafeBCNumber = !$event"
    />

    <text-input-component
      id="doingBusinessAs"
      v-if="showFields"
      label="Doing business as"
      placeholder=""
      autocomplete="off"
      v-model="formData.businessInformation.doingBusinessAs"
      :validations="[]"
      enabled
      @empty="validation.doingBusinessAs = true"
      @error="validation.doingBusinessAs = !$event"
    />

    <text-input-component
      id="acronym"
      v-if="showFields"
      label="Acronym"
      placeholder=""
      autocomplete="off"
      v-model="formData.businessInformation.acronym"
      :validations="[]"
      enabled
      @empty="validation.acronym = true"
      @error="validation.acronym = !$event"
    />

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

//TODO: Add some other mocks to test the different scenarios
//TODO: check why is not marking as valid
//TODO: Add validators
//TODO: Add tests to the component
//TODO: Add a new entry to the submission e2e