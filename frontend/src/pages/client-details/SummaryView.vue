<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import type { ClientDetails, CodeNameType, SaveEvent, UserRole } from "@/dto/CommonTypesDto";
import RegistrationNumber from "@/pages/client-details/RegistrationNumber.vue";
import {
  getFormattedHtml,
  getTagColorByClientStatus,
  goodStanding,
  includesAnyOf,
  removePrefix,
  formatDate,
  preserveUnchangedData,
} from "@/services/ForestClientService";

import Check20 from "@carbon/icons-vue/es/checkmark--filled/20";
import Warning20 from "@carbon/icons-vue/es/warning--filled/20";
import Information16 from "@carbon/icons-vue/es/information/16";
import Edit16 from "@carbon/icons-vue/es/edit/16";
import Save16 from "@carbon/icons-vue/es/save/16";
import Close16 from "@carbon/icons-vue/es/close/16";
import UserAvatar20 from "@carbon/icons-vue/es/user--avatar/20";

// Importing validators
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import {
  resetSubmissionValidators,
  submissionValidation,
} from "@/helpers/validators/SubmissionValidators";
import { useFetchTo } from "@/composables/useFetch";
import { useEventBus } from "@vueuse/core";

const props = defineProps<{
  data: ClientDetails;
  userRoles: UserRole[];
}>();

const emit = defineEmits<{
  (e: "save", payload: SaveEvent<ClientDetails>): void;
}>();

const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

let originalData: ClientDetails;
const formData = ref<ClientDetails>();

const isEditing = ref(false);
const hasAnyChange = ref(false);

const formatBirthdate = (birthdate: string) => new Date(birthdate).toISOString().split("T")[0];

const formatClientData = (data: ClientDetails) => {
  const formattedData = JSON.parse(JSON.stringify(data));
  if (formattedData.client.birthdate) {
    try {
      formattedData.client.birthdate = formatBirthdate(formattedData.client.birthdate);
    } catch {
      // Intentionally ignoring formatting errors – fallback to original value
    }
  }
  return formattedData;
};

const resetFormData = () => {
  originalData = formatClientData(props.data);
  formData.value = formatClientData(props.data);
  hasAnyChange.value = false;
};

resetFormData();

watch(
  formData,
  () => {
    if (isEditing.value) {
      const updatedData = preserveUnchangedData(formData.value, originalData);
      hasAnyChange.value = JSON.stringify(updatedData) !== JSON.stringify(originalData);
    }
  },
  { deep: true },
);

const saving = ref(false);

const edit = () => {
  resetFormData();
  isEditing.value = true;
  saving.value = false;
};

const cancel = () => {
  isEditing.value = false;
};

const lockEditing = () => {
  isEditing.value = false;
};

const setSaving = (value: boolean) => {
  saving.value = value;
};

defineExpose({ lockEditing, setSaving });

const fieldIdList = [
  "clientName",
  "legalFirstName",
  "legalMiddleName",
  "acronym",
  "doingBusinessAs",
  "clientType",
  "registrationNumber",
  "workSafeBCNumber",
  "birthdate",
  "clientIdType",
  "clientIdentification",
  "clientStatus",
  "notes",
] as const;

type FieldId = (typeof fieldIdList)[number];

type FieldConfigPath<T extends keyof ClientDetails> = {
  path: T;
  fields: (keyof ClientDetails[T])[];
};

type FieldConfigRoot = {
  path?: "root";
  fields: (keyof ClientDetails)[];
};

type FieldPath = keyof ClientDetails | "root";

type FieldConfig<T extends FieldPath = FieldPath> = T extends keyof ClientDetails
  ? FieldConfigPath<T>
  : FieldConfigRoot;

/**
 * Local fields that either don't belong in the `client` property or whose name doesn't match any
 * `client` field.
 */
type MapFieldId = Exclude<FieldId, keyof ClientDetails["client"]>;

/**
 * Maps local field names to data model fields with type-checking.
 * The local fields that are not in this map will use the default config, which is:
 * {
 *   path: "client"
 *   fields: [the same as the local field name]
 * }
 */
const dataModelMap: {
  [key in MapFieldId]: FieldConfig;
} = {
  doingBusinessAs: {
    path: "root",
    fields: ["doingBusinessAs"],
  },
  acronym: {
    path: "client",
    fields: ["clientAcronym"],
  },
  clientType: {
    path: "client",
    fields: ["clientTypeCode"],
  },
  registrationNumber: {
    path: "client",
    fields: ["registryCompanyTypeCode", "corpRegnNmbr"],
  },
  workSafeBCNumber: {
    path: "client",
    fields: ["wcbFirmNumber"],
  },
  clientIdType: {
    path: "client",
    fields: ["clientIdTypeCode"],
  },
  clientStatus: {
    path: "client",
    fields: ["clientStatusCode"],
  },
  notes: {
    path: "client",
    fields: ["clientComment"],
  },
};

const getRemovedFields = () => {
  const result: FieldId[] = [];
  Object.keys(originalFieldsEditability.value).forEach((field: FieldId) => {
    if (originalFieldsEditability.value[field] && !fieldsEditability.value[field]) {
      result.push(field);
    }
  });
  return result;
};

const save = () => {
  const updatedData = preserveUnchangedData(formData.value, originalData);
  const removedFields = getRemovedFields();

  // Clear the value of removed fields
  removedFields.forEach((localField: FieldId) => {
    let path = "client";
    let fields = [localField];
    if (dataModelMap[localField]) {
      ({ path, fields } = dataModelMap[localField]);
    }
    const data = path === "root" ? updatedData : updatedData[path];
    fields.forEach((field) => {
      data[field] = null;
    });
  });

  const patch = jsonpatch.compare(originalData, updatedData);

  emit("save", {
    patch,
    updatedData,
    action: {
      infinitive: "update",
      pastParticiple: "updated",
    },
    operationType: "update",
  });
};

const validation = reactive<Record<FieldId, boolean>>({
  clientName: true,
  legalFirstName: true,
  legalMiddleName: true,
  acronym: true,
  doingBusinessAs: true,
  clientType: true,
  registrationNumber: true,
  workSafeBCNumber: true,
  birthdate: true,
  clientIdType: true,
  clientIdentification: true,
  clientStatus: true,
  notes: true,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) => accumulator && currentValue,
    true,
  );

const editRoles: Record<FieldId, UserRole[]> = {
  clientName: ["CLIENT_ADMIN"],
  legalFirstName: ["CLIENT_ADMIN"],
  legalMiddleName: ["CLIENT_ADMIN"],
  acronym: ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"],
  doingBusinessAs: ["CLIENT_ADMIN"],
  clientType: ["CLIENT_ADMIN"],
  registrationNumber: ["CLIENT_ADMIN"],
  workSafeBCNumber: ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"],
  birthdate: ["CLIENT_ADMIN"],
  clientIdType: ["CLIENT_ADMIN"],
  clientIdentification: ["CLIENT_ADMIN"],
  clientStatus: ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"],
  notes: ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"],
};

const clientNameLabel = computed(() =>
  formData.value.client.clientTypeCode === "I" ? "Last name" : "Client name",
);

// Types whose standing status can be checked with BC Registries
const bcRegisteredTypes = ["A", "C", "L", "P", "S", "U"];

// All Types that can have a Registration number
const companyLikeTypes = [...bcRegisteredTypes, "B", "T"];

const canEditClientStatus = () => {
  const { clientStatusCode } = props.data.client;
  if (props.userRoles.includes("CLIENT_ADMIN")) {
    return true;
  }
  if (["SPN"].includes(clientStatusCode)) {
    return props.userRoles.includes("CLIENT_SUSPEND");
  }
  if (["ACT"].includes(clientStatusCode)) {
    return true;
  }

  // status is (DEC or DAC) and userRoles doesn't include CLIENT_ADMIN
  return false;
};

/**
 * Tells whether the field is editable at all, according to the client's data provided.
 * Note: it doesn't mean the current user should be allowed to do it.
 */
const isFieldEditable: Record<FieldId, (data: ClientDetails) => boolean> = {
  clientName: () => true,
  legalFirstName: (data) => data.client.clientTypeCode === "I",
  legalMiddleName: (data) => data.client.clientTypeCode === "I",
  acronym: () => true,
  doingBusinessAs: () => true,
  clientType: () => true,
  registrationNumber: (data) => companyLikeTypes.includes(data.client.clientTypeCode),
  workSafeBCNumber: () => true,
  birthdate: (data) => data.client.clientTypeCode === "I",
  clientIdType: (data) => data.client.clientTypeCode === "I",
  clientIdentification: (data) => data.client.clientTypeCode === "I",
  clientStatus: canEditClientStatus,
  notes: () => true,
};

const fieldsEditability = computed<Record<FieldId, boolean>>(() => {
  const result = {} as Record<FieldId, boolean>;
  Object.keys(isFieldEditable).forEach((field) => {
    result[field] = isFieldEditable[field](formData.value);
  });
  return result;
});

watch(fieldsEditability, () => {
  const removedFields = getRemovedFields();

  // Removed fields should be considered valid
  removedFields.forEach((localField: FieldId) => {
    validation[localField] = true;
  });
});

const originalFieldsEditability = computed<Record<FieldId, boolean>>(() => {
  const result = {} as Record<FieldId, boolean>;
  Object.keys(isFieldEditable).forEach((field) => {
    result[field] = isFieldEditable[field](props.data);
  });
  return result;
});

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);

const displayEditable = (fieldId: FieldId) =>
  isEditing.value &&
  includesAnyOf(props.userRoles, editRoles[fieldId]) &&
  fieldsEditability.value[fieldId];

const displayReadonly = (fieldId: FieldId) => !isEditing.value || !displayEditable(fieldId);

const rawClientRegistrationNumber = computed(() => {
  const { registryCompanyTypeCode, corpRegnNmbr } = props.data.client;
  return `${registryCompanyTypeCode || ""}${corpRegnNmbr || ""}`;
});

/**
 * This value will only be available if both Type and Number are present
 * */
const safeClientRegistrationNumber = computed(() => {
  const { registryCompanyTypeCode, corpRegnNmbr } = props.data.client;
  if (!registryCompanyTypeCode || !corpRegnNmbr) {
    return undefined;
  }
  return `${registryCompanyTypeCode}${corpRegnNmbr}`;
});

const dateOfBirth = computed(() => {
  const { birthdate } = props.data.client;
  if (birthdate) {
    if (birthdate.length === 4) {
      return birthdate;
    }

    // if masked (month and day)
    if (birthdate.includes("*")) {
      return birthdate.slice(0, 4);
    }

    return formatBirthdate(birthdate);
  }
  return "";
});

const birthdateLabel = computed(() =>
  dateOfBirth.value.length > 4 ? "Date of birth" : "Year of birth",
);

const updateClientType = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.client.clientTypeCode = value.code;
  }
};

const updateClientIdType = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.client.clientIdTypeCode = value.code;
  }
};

const updateClientStatus = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.client.clientStatusCode = value.code;
  }
};

watch(
  formData,
  () => {
    resetSubmissionValidators();
  },
  {
    deep: true,
  },
);

const originalClient = computed(() => props.data.client);

const bcRegistryError = ref<boolean>(false);

const goodStandingInd = ref<string>();

const goodStandingIndUri = computed(() =>
  `/api/clients/${originalClient.value.clientNumber}/good-standing`
);

const { loading: goodStandingIndLoading, error } = useFetchTo(goodStandingIndUri, goodStandingInd);

watch(error, () => {
  if (
    error.value.response?.status >= 500 ||
    error.value.response?.status === 408
  ) {
    bcRegistryError.value = true;
  }
});

watch(goodStandingInd, () => {
  if (goodStandingInd.value) {
    originalClient.value.goodStandingInd = goodStandingInd.value;
  }
});

const additionalClientIdentificationValidations = computed(() => {
  const suffix = formData.value.client.clientIdTypeCode === "OTHR" ? "OTHR" : "nonOTHR";
  return getValidations(`client.clientIdentification-${suffix}`);
});

watch(
  () => formData.value.client.clientIdTypeCode,
  () => {
    nextTick(() => revalidateBus.emit(["input-clientIdentification"]));
  },
);

const clientIdentificationMask = "U".repeat(40);
</script>

<template>
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

  <div class="grouping-10 no-padding">
    <read-only-component label="Client number" id="clientNumber">
      <span class="body-compact-01">{{ originalClient.clientNumber }}</span>
    </read-only-component>
    <read-only-component
      label="Acronym"
      id="acronym"
      v-if="displayReadonly('acronym') && originalClient.clientAcronym"
    >
      <span class="body-compact-01">{{ originalClient.clientAcronym }}</span>
    </read-only-component>
    <read-only-component
      label="Doing business as"
      id="doingBusinessAs"
      v-if="displayReadonly('doingBusinessAs') && props.data.doingBusinessAs"
    >
      <span class="body-compact-01">{{ props.data.doingBusinessAs }}</span>
    </read-only-component>
    <read-only-component label="Client type" id="clientType">
      <span class="body-compact-01">{{ originalClient.clientTypeDesc }}</span>
    </read-only-component>
    <read-only-component
      label="Registration number"
      id="registrationNumber"
      v-if="displayReadonly('registrationNumber') && rawClientRegistrationNumber"
    >
      <span class="body-compact-01">{{ rawClientRegistrationNumber }}</span>
    </read-only-component>
    <read-only-component
      label="WorkSafeBC number"
      id="workSafeBCNumber"
      v-if="displayReadonly('workSafeBCNumber') && originalClient.wcbFirmNumber"
    >
      <span class="body-compact-01">{{ originalClient.wcbFirmNumber }}</span>
    </read-only-component>
    <read-only-component
      label="BC Registries standing"
      id="goodStanding"
      v-if="
        safeClientRegistrationNumber && bcRegisteredTypes.includes(originalClient.clientTypeCode)
      "
    >
      <div class="internal-grouping-01">
        <span v-if="goodStandingIndLoading">
          <cds-skeleton-text v-shadow="1" class="heading-03-skeleton" />
        </span>
        <span v-else class="icon-label-inline">
          <span class="body-compact-01 default-typography">{{
            goodStanding(originalClient.goodStandingInd)
          }}</span>
          
          <Check20 v-if="originalClient.goodStandingInd === 'Y'" class="good" />

          <cds-tooltip v-if="originalClient.goodStandingInd !== 'Y'">
            <Warning20 class="warning" />
            <cds-tooltip-content>
              Check BC Registries for the reason
            </cds-tooltip-content>
          </cds-tooltip>
        </span>
      </div>
    </read-only-component>
    <read-only-component
      :label="originalClient.clientIdTypeDesc"
      id="identification"
      v-if="
        displayReadonly('clientIdType') &&
        displayReadonly('clientIdentification') &&
        originalClient.clientIdTypeDesc &&
        originalClient.clientIdentification
      "
    >
      <span class="body-compact-01">{{ originalClient.clientIdentification }}</span>
    </read-only-component>
    <read-only-component
      :label="birthdateLabel"
      id="dateOfBirth"
      v-if="displayReadonly('birthdate') && dateOfBirth"
    >
      <span class="body-compact-01">{{ dateOfBirth }}</span>
    </read-only-component>
    <read-only-component
      label="Client status"
      id="clientStatus"
      v-if="displayReadonly('clientStatus')"
    >
      <span class="body-compact-01">
        <cds-tag :type="getTagColorByClientStatus(originalClient.clientStatusDesc)">
          <span>{{ originalClient.clientStatusDesc }}</span>
        </cds-tag>
      </span>
    </read-only-component>
  </div>
  <div
    class="grouping-10 no-padding"
    v-if="displayReadonly('notes') && originalClient.clientComment"
  >
    <read-only-component label="Notes" id="notes">
      <span class="body-compact-01" style="display: inline;">
        <span v-dompurify-html="getFormattedHtml(originalClient.clientComment)"></span>
        <span 
          class="icon-label-inline" 
          v-if="originalClient.clientCommentUpdateUser && originalClient.clientCommentUpdateDate"
        > | Note by <UserAvatar20 /> 
          {{ removePrefix(originalClient.clientCommentUpdateUser) }} &middot; 
          {{ formatDate(originalClient.clientCommentUpdateDate) }}
        </span>
      </span>
    </read-only-component>
  </div>
  <div class="grouping-10 no-padding" v-if="canEdit && !isEditing">
    <cds-button id="summaryEditBtn" kind="tertiary" size="md" @click="edit">
      <span class="width-unset">Edit client information</span>
      <Edit16 slot="icon" />
    </cds-button>
  </div>
  <div class="form-edit no-padding" v-if="isEditing">
    <text-input-component
      id="input-legalFirstName"
      v-if="displayEditable('legalFirstName')"
      label="First name"
      placeholder=""
      autocomplete="off"
      v-model="formData.client.legalFirstName"
      :validations="[
        ...getValidations('businessInformation.firstName'),
        submissionValidation(`businessInformation.firstName`),
      ]"
      enabled
      required
      required-label
      @empty="validation.legalFirstName = !$event"
      @error="validation.legalFirstName = !$event"
    />
    <text-input-component
      id="input-legalMiddleName"
      v-if="displayEditable('legalMiddleName')"
      label="Middle name"
      placeholder=""
      autocomplete="off"
      v-model="formData.client.legalMiddleName"
      :validations="[
        ...getValidations('businessInformation.middleName'),
        submissionValidation(`businessInformation.middleName`),
      ]"
      enabled
      @empty="validation.legalMiddleName = true"
      @error="validation.legalMiddleName = !$event"
    />
    <text-input-component
      id="input-clientName"
      v-if="displayEditable('clientName')"
      :label="clientNameLabel"
      autocomplete="off"
      required
      required-label
      placeholder=""
      v-model="formData.client.clientName"
      :validations="[
        ...getValidations('businessInformation.businessName'),
        submissionValidation('businessInformation.businessName'),
      ]"
      @empty="validation.clientName = !$event"
      @error="validation.clientName = !$event"
    />
    <text-input-component
      id="input-acronym"
      v-if="displayEditable('acronym')"
      label="Acronym"
      placeholder=""
      autocomplete="off"
      v-model="formData.client.clientAcronym"
      :validations="[
        ...getValidations('businessInformation.clientAcronym'),
        submissionValidation('/client/clientAcronym'),
      ]"
      enabled
      @empty="validation.acronym = true"
      @error="validation.acronym = !$event"
    >
      <template #error="{ data }">
        <template v-if="typeof data === 'object' && data?.custom?.match">
          Looks like this acronym belongs to client
          <span>
            <a :href="`/clients/details/${data.custom.match}`" target="_blank" rel="noopener">
              {{ data.custom.match }}</a
            ></span
          >. Try another acronym
        </template>
      </template>
    </text-input-component>
    <text-input-component
      id="input-doingBusinessAs"
      v-if="displayEditable('doingBusinessAs')"
      label="Doing business as"
      placeholder=""
      autocomplete="off"
      v-model="formData.doingBusinessAs"
      :validations="[
        ...getValidations('businessInformation.doingBusinessAs'),
        submissionValidation(`businessInformation.doingBusinessAs`),
      ]"
      enabled
      @empty="validation.doingBusinessAs = true"
      @error="validation.doingBusinessAs = !$event"
    />
    <data-fetcher
      v-if="displayEditable('clientType')"
      url="/api/codes/client-types/legacy"
      :min-length="0"
      :init-value="[]"
      :init-fetch="true"
      :params="{ method: 'GET' }"
      #="{ content }"
    >
      <combo-box-input-component
        id="input-clientType"
        label="Client type"
        :initial-value="content?.find((item) => item.code === formData.client.clientTypeCode)?.name"
        required
        required-label
        :model-value="content"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('client.clientTypeCode'),
          submissionValidation('client.clientTypeCode'),
        ]"
        @update:selected-value="updateClientType($event)"
        @empty="validation.clientType = !$event"
      />
    </data-fetcher>
    <div v-if="displayEditable('registrationNumber')">
      <registration-number
        :model-value="formData"
        :original-value="props.data"
        @valid="validation.registrationNumber = $event"
      />
    </div>
    <div v-if="displayEditable('birthdate')">
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
        id="input-birthdate"
        title="Date of birth"
        :autocomplete="['off', 'off', 'off']"
        v-model="formData.client.birthdate"
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
    <div
      class="horizontal-input-grouping"
      v-if="displayEditable('clientIdType') || displayEditable('clientIdentification')"
    >
      <data-fetcher
        v-if="displayEditable('clientIdType')"
        url="/api/codes/identification-types/legacy"
        :min-length="0"
        :init-value="[]"
        :init-fetch="true"
        :params="{ method: 'GET' }"
        #="{ content }"
      >
        <combo-box-input-component
          id="input-clientIdType"
          label="ID Type"
          :initial-value="
            content?.find((item) => item.code === formData.client.clientIdTypeCode)?.name
          "
          required
          required-label
          :model-value="content"
          :enabled="true"
          tip=""
          :validations="[
            ...getValidations('client.clientIdTypeCode'),
            submissionValidation('client.clientIdTypeCode'),
          ]"
          @update:selected-value="updateClientIdType($event)"
          @empty="validation.clientIdType = !$event"
        />
      </data-fetcher>
      <text-input-component
        id="input-clientIdentification"
        v-if="displayEditable('clientIdentification')"
        label="ID number"
        placeholder=""
        autocomplete="off"
        v-model="formData.client.clientIdentification"
        :mask="clientIdentificationMask"
        :validations="[
          ...getValidations('client.clientIdentification'),
          ...additionalClientIdentificationValidations,
          submissionValidation('client.clientIdentification'),
        ]"
        enabled
        required
        required-label
        @empty="validation.clientIdentification = !$event"
        @error="validation.clientIdentification = !$event"
      />
    </div>
    <text-input-component
      id="input-workSafeBCNumber"
      v-if="displayEditable('workSafeBCNumber')"
      label="WorkSafeBC number"
      mask="######"
      placeholder=""
      autocomplete="off"
      v-model="formData.client.wcbFirmNumber"
      :validations="[
        ...getValidations('businessInformation.workSafeBcNumber'),
        submissionValidation(`businessInformation.workSafeBcNumber`),
      ]"
      enabled
      @empty="validation.workSafeBCNumber = true"
      @error="validation.workSafeBCNumber = !$event"
    />
    <data-fetcher
      v-if="displayEditable('clientStatus')"
      :key="formData.client.clientTypeCode"
      :url="`/api/codes/client-statuses/${formData.client.clientTypeCode}`"
      :min-length="0"
      :init-value="[]"
      :init-fetch="true"
      :params="{ method: 'GET' }"
      #="{ content }"
    >
      <dropdown-input-component
        id="input-clientStatus"
        label="Client status"
        :initial-value="
          content?.find((item) => item.code === formData.client.clientStatusCode)?.name
        "
        required
        required-label
        :model-value="content"
        :enabled="true"
        tip=""
        :validations="[]"
        @update:selected-value="updateClientStatus($event)"
        @empty="validation.clientStatus = !$event"
        #="{ option }"
      >
        <cds-tag :type="getTagColorByClientStatus(option.name)" title="">
          <span>{{ option.name }}</span>
        </cds-tag>
      </dropdown-input-component>
    </data-fetcher>
    <textarea-input-component
      id="input-notes"
      v-if="displayEditable('notes')"
      label="Notes"
      enable-counter
      :max-count="4000"
      :rows="7"
      placeholder=""
      v-model="formData.client.clientComment"
      :enabled="true"
      :validations="[
        ...getValidations('businessInformation.notes'),
        submissionValidation('businessInformation.notes'),
      ]"
      @empty="validation.notes = true"
      @error="validation.notes = !$event"
    >
      <div slot="label-text" class="label-with-icon line-height-0">
        <div class="cds-text-input-label">
          <span>Notes</span>
        </div>
        <cds-tooltip>
          <Information16 />
          <cds-tooltip-content>
            For example, any information about the client, their locations or specific instructions
            for contacting them
          </cds-tooltip-content>
        </cds-tooltip>
      </div>
    </textarea-input-component>
    <div class="form-group-buttons form-group-buttons--stretched">
      <cds-button
        id="summarySaveBtn"
        kind="primary"
        size="md"
        @click="save"
        :disabled="saving || !hasAnyChange || !checkValid()"
      >
        <span class="width-unset">Save changes</span>
        <Save16 slot="icon" />
      </cds-button>
      <cds-button
        id="summaryCancelBtn"
        kind="tertiary"
        size="md"
        @click="cancel"
        :disabled="saving"
      >
        <span class="width-unset">Cancel</span>
        <Close16 slot="icon" />
      </cds-button>
    </div>
  </div>
</template>

<style scoped>
.internal-grouping-01 {
  display: flex;
  gap: 0.5rem;
}

.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
