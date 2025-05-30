<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import type { ClientDetails, CodeNameType, UserRole } from "@/dto/CommonTypesDto";
import RegistrationNumber from "@/pages/client-details/RegistrationNumber.vue";
import {
  getFormattedHtml,
  getTagColorByClientStatus,
  goodStanding,
  includesAnyOf,
} from "@/services/ForestClientService";
import { greenDomain } from "@/CoreConstants";

import Check20 from "@carbon/icons-vue/es/checkmark--filled/20";
import Warning20 from "@carbon/icons-vue/es/warning--filled/20";
import Information16 from "@carbon/icons-vue/es/information/16";
import Edit16 from "@carbon/icons-vue/es/edit/16";
import Save16 from "@carbon/icons-vue/es/save/16";
import Close16 from "@carbon/icons-vue/es/close/16";

// Importing validators
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import {
  resetSubmissionValidators,
  submissionValidation,
} from "@/helpers/validators/SubmissionValidators";

const props = defineProps<{
  data: ClientDetails;
  userRoles: UserRole[];
}>();

const emit = defineEmits<{
  (e: "save", value: jsonpatch.Operation[]): void;
}>();

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
      hasAnyChange.value = JSON.stringify(formData.value) !== JSON.stringify(originalData);
    }
  },
  { deep: true },
);

const edit = () => {
  resetFormData();
  isEditing.value = true;
};

const cancel = () => {
  isEditing.value = false;
};

const lockEditing = () => {
  isEditing.value = false;
};

defineExpose({
  lockEditing,
});

const save = () => {
  const patch = jsonpatch.compare(originalData, formData.value);
  emit("save", patch);
};

const fieldIdList = [
  "clientName",
  "legalFirstName",
  "legalMiddleName",
  "acronym",
  "doingBusinessAs",
  "registrationNumber",
  "workSafeBCNumber",
  "birthdate",
  "clientIdType",
  "clientIdentification",
  "clientStatus",
  "notes",
] as const;

type FieldId = (typeof fieldIdList)[number];

const validation = reactive<Record<FieldId, boolean>>({
  clientName: true,
  legalFirstName: true,
  legalMiddleName: true,
  acronym: true,
  doingBusinessAs: true,
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

const companyLikeTypes = ["A", "C", "L", "P", "S", "R", "T", "U"];

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
 * Tells whether the field is editable at all, according to the client's type.
 * Note: it doesn't mean the current user should be allowed to do it.
 */
const isFieldEditable: Record<FieldId, () => boolean> = {
  clientName: () => true,
  legalFirstName: () => props.data.client.clientTypeCode === "I",
  legalMiddleName: () => props.data.client.clientTypeCode === "I",
  acronym: () => true,
  doingBusinessAs: () => true,
  registrationNumber: () => companyLikeTypes.includes(props.data.client.clientTypeCode),
  workSafeBCNumber: () => true,
  birthdate: () => props.data.client.clientTypeCode === "I",
  clientIdType: () => props.data.client.clientTypeCode === "I",
  clientIdentification: () => props.data.client.clientTypeCode === "I",
  clientStatus: canEditClientStatus,
  notes: () => true,
};

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);

const displayEditable = (fieldId: FieldId) =>
  isEditing.value &&
  includesAnyOf(props.userRoles, editRoles[fieldId]) &&
  isFieldEditable[fieldId]();

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

const client = computed(() => props.data.client);
</script>

<template>
  <div class="grouping-10 no-padding">
    <read-only-component label="Client number" id="clientNumber">
      <span class="body-compact-01">{{ client.clientNumber }}</span>
    </read-only-component>
    <read-only-component
      label="Acronym"
      id="acronym"
      v-if="displayReadonly('acronym') && client.clientAcronym"
    >
      <span class="body-compact-01">{{ client.clientAcronym }}</span>
    </read-only-component>
    <read-only-component
      label="Doing business as"
      id="doingBusinessAs"
      v-if="displayReadonly('doingBusinessAs') && props.data.doingBusinessAs"
    >
      <span class="body-compact-01">{{ props.data.doingBusinessAs }}</span>
    </read-only-component>
    <read-only-component label="Client type" id="clientType">
      <span class="body-compact-01">{{ client.clientTypeDesc }}</span>
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
      v-if="displayReadonly('workSafeBCNumber') && client.wcbFirmNumber"
    >
      <span class="body-compact-01">{{ client.wcbFirmNumber }}</span>
    </read-only-component>
    <read-only-component
      label="BC Registries standing"
      id="goodStanding"
      v-if="safeClientRegistrationNumber"
    >
      <div class="internal-grouping-01">
        <span class="body-compact-01 default-typography">{{
          goodStanding(client.goodStandingInd)
        }}</span>
        <Check20 v-if="client.goodStandingInd === 'Y'" class="good" />
        <Warning20 v-if="client.goodStandingInd !== 'Y'" class="warning" />
      </div>
    </read-only-component>
    <read-only-component
      :label="client.clientIdTypeDesc"
      id="identification"
      v-if="
        displayReadonly('clientIdType') &&
        displayReadonly('clientIdentification') &&
        client.clientIdTypeDesc &&
        client.clientIdentification
      "
    >
      <span class="body-compact-01">{{ client.clientIdentification }}</span>
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
        <cds-tag :type="getTagColorByClientStatus(client.clientStatusDesc)">
          <span>{{ client.clientStatusDesc }}</span>
        </cds-tag>
      </span>
    </read-only-component>
  </div>
  <div class="grouping-10 no-padding" v-if="displayReadonly('notes') && client.clientComment">
    <read-only-component label="Notes" id="notes">
      <span
        class="body-compact-01"
        v-dompurify-html="getFormattedHtml(client.clientComment)"
      ></span>
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
    <div
      class="horizontal-input-grouping"
      v-if="displayEditable('clientName') || displayEditable('acronym')"
    >
      <text-input-component
        id="input-clientName"
        v-if="displayEditable('clientName')"
        class="grouping-02--width-32rem"
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
        :class="{ 'grouping-02--width-8rem': displayEditable('clientName') }"
        label="Acronym"
        placeholder=""
        mask="NNNNNNNN"
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
          <template v-if="data?.custom?.match">
            Looks like this acronym belongs to client
            <span
              ><a
                :href="`https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${data.custom.match}`"
                target="_blank"
                rel="noopener"
                >{{ data.custom.match }}</a
              ></span
            >. Try another acronym
          </template>
        </template>
      </text-input-component>
    </div>
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
    <div class="horizontal-input-grouping">
      <data-fetcher
        v-if="displayEditable('clientStatus')"
        url="/api/codes/identification-types/legacy"
        :min-length="0"
        :init-value="[]"
        :init-fetch="true"
        :params="{ method: 'GET' }"
        #="{ content }"
      >
        <combo-box-input-component
          id="input-clientIdType"
          v-if="displayEditable('clientIdType')"
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
        :validations="[
          ...getValidations('client.clientIdentification'),
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
      :url="`/api/codes/client-statuses/${client.clientTypeCode}`"
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
        :disabled="!hasAnyChange || !checkValid()"
      >
        <span class="width-unset">Save client information</span>
        <Save16 slot="icon" />
      </cds-button>
      <cds-button id="summaryCancelBtn" kind="tertiary" size="md" @click="cancel">
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
