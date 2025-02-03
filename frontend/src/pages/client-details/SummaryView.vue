<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import type { ClientDetails, CodeNameType } from "@/dto/CommonTypesDto";
import {
  getFormattedHtml,
  getTagColorByClientStatus,
  goodStanding,
} from "@/services/ForestClientService";

import Check20 from "@carbon/icons-vue/es/checkmark--filled/20";
import Warning20 from "@carbon/icons-vue/es/warning--filled/20";
import Information16 from "@carbon/icons-vue/es/information/16";
import Edit16 from "@carbon/icons-vue/es/edit/16";
import Save16 from "@carbon/icons-vue/es/save/16";
import Close16 from "@carbon/icons-vue/es/close/16";

// Importing validators
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";

const props = defineProps<{
  data: ClientDetails;
  userRole: string;
}>();

let originalData: ClientDetails;
const formData = ref<ClientDetails>();

const isEditing = ref(false);
const hasAnyChange = ref(false);

const resetFormData = () => {
  originalData = JSON.parse(JSON.stringify(props.data));
  formData.value = JSON.parse(JSON.stringify(props.data));
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
  isEditing.value = true;
};

const cancel = () => {
  isEditing.value = false;
  resetFormData();
};

const save = () => {
  const diff = jsonpatch.compare(originalData, formData.value);
  console.log({ patch: diff });
  isEditing.value = false;
};

const fieldIdList = [
  "clientName",
  "acronym",
  "doingBusinessAs",
  "clientType",
  "registrationNumber",
  "workSafeBCNumber",
  "clientStatus",
  "notes",
] as const;
const roles = ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR", "CLIENT_VIEWER"] as const;

type FieldId = (typeof fieldIdList)[number];
type Role = (typeof roles)[number];

const validation = reactive<Record<FieldId, boolean>>({
  clientName: true,
  acronym: true,
  doingBusinessAs: true,
  clientType: true,
  registrationNumber: true,
  workSafeBCNumber: true,
  clientStatus: true,
  notes: true,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) => accumulator && currentValue,
    true,
  );

const editRoles: Record<FieldId, Role[]> = {
  clientName: ["CLIENT_ADMIN"],
  acronym: ["CLIENT_ADMIN"],
  doingBusinessAs: ["CLIENT_ADMIN"],
  clientType: ["CLIENT_ADMIN"],
  registrationNumber: ["CLIENT_ADMIN"],
  workSafeBCNumber: ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"],
  clientStatus: ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"],
  notes: ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"],
};

const canEdit = (role: Role) => ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"].includes(role);

const displayEditable = (fieldId: FieldId) =>
  isEditing.value && editRoles[fieldId]?.includes(props.userRole as Role);

const displayReadonly = (fieldId: FieldId) => !isEditing.value || !displayEditable(fieldId);

const clientRegistrationNumber = computed(() => {
  const { registryCompanyTypeCode, corpRegnNmbr } = props.data;
  if (!registryCompanyTypeCode || !corpRegnNmbr) {
    return undefined;
  }
  return `${registryCompanyTypeCode}${corpRegnNmbr}`;
});

const doingBusinessAs = computed(() => {
  const { doingBusinessAs } = props.data;
  if (doingBusinessAs && doingBusinessAs.length) {
    return doingBusinessAs.map((value) => value.doingBusinessAsName).join("\n");
  }
  return undefined;
});

const dateOfBirth = computed(() => {
  if (props.data.birthdate) {
    if (props.data.birthdate.length === 4) {
      return props.data.birthdate;
    }

    // if masked (month and day)
    if (props.data.birthdate.includes("*")) {
      return props.data.birthdate.slice(0, 4);
    }

    return new Date(props.data.birthdate).toISOString().split("T")[0];
  }
  return "";
});

const birthdateLabel = computed(() =>
  dateOfBirth.value.length > 4 ? "Date of birth" : "Year of birth",
);

const updateClientType = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.clientTypeCode = value.code;
  }
};

const updateClientStatus = (value: CodeNameType | undefined) => {
  if (value) {
    formData.value.clientStatusCode = value.code;
  }
};
</script>

<template>
  <div class="grouping-10 no-padding">
    <read-only-component label="Client number" id="clientNumber">
      <span class="body-compact-01">{{ props.data.clientNumber }}</span>
    </read-only-component>
    <read-only-component
      label="Acronym"
      id="acronym"
      v-if="displayReadonly('acronym') && props.data.clientAcronym"
    >
      <span class="body-compact-01">{{ props.data.clientAcronym }}</span>
    </read-only-component>
    <read-only-component
      label="Doing business as"
      id="doingBusinessAs"
      v-if="displayReadonly('doingBusinessAs') && doingBusinessAs"
    >
      <span class="body-compact-01" v-dompurify-html="getFormattedHtml(doingBusinessAs)"></span>
    </read-only-component>
    <read-only-component label="Client type" id="clientType" v-if="displayReadonly('clientType')">
      <span class="body-compact-01">{{ props.data.clientTypeDesc }}</span>
    </read-only-component>
    <read-only-component
      label="Registration number"
      id="registrationNumber"
      v-if="displayReadonly('registrationNumber') && clientRegistrationNumber"
    >
      <span class="body-compact-01">{{ clientRegistrationNumber }}</span>
    </read-only-component>
    <read-only-component
      label="WorkSafeBC number"
      id="workSafeBCNumber"
      v-if="displayReadonly('workSafeBCNumber') && props.data.wcbFirmNumber"
    >
      <span class="body-compact-01">{{ props.data.wcbFirmNumber }}</span>
    </read-only-component>
    <read-only-component
      label="BC Registries standing"
      id="goodStanding"
      v-if="clientRegistrationNumber"
    >
      <div class="internal-grouping-01">
        <span class="body-compact-01 default-typography">{{
          goodStanding(props.data.goodStandingInd)
        }}</span>
        <Check20 v-if="props.data.goodStandingInd === 'Y'" class="good" />
        <Warning20 v-if="props.data.goodStandingInd !== 'Y'" class="warning" />
      </div>
    </read-only-component>
    <read-only-component
      :label="props.data.clientIdTypeDesc"
      id="identification"
      v-if="props.data.clientIdTypeDesc && props.data.clientIdentification"
    >
      <span class="body-compact-01">{{ props.data.clientIdentification }}</span>
    </read-only-component>
    <read-only-component :label="birthdateLabel" id="dateOfBirth" v-if="dateOfBirth">
      <span class="body-compact-01">{{ dateOfBirth }}</span>
    </read-only-component>
    <read-only-component label="Client status" id="status" v-if="displayReadonly('clientStatus')">
      <span class="body-compact-01">
        <cds-tag :type="getTagColorByClientStatus(props.data.clientStatusDesc)">
          <span>{{ props.data.clientStatusDesc }}</span>
        </cds-tag>
      </span>
    </read-only-component>
  </div>
  <div class="grouping-10 no-padding" v-if="displayReadonly('notes') && props.data.clientComment">
    <read-only-component label="Notes" id="notes">
      <span
        class="body-compact-01"
        v-dompurify-html="getFormattedHtml(props.data.clientComment)"
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
    <div class="horizontal-input-grouping">
      <text-input-component
        id="clientName"
        v-if="displayEditable('clientName')"
        class="grouping-02--width-32rem"
        label="Client name"
        autocomplete="off"
        required
        required-label
        placeholder=""
        v-model="formData.clientName"
        :validations="[
          ...getValidations('businessInformation.businessName'),
          submissionValidation('businessInformation.businessName'),
        ]"
        @empty="validation.clientName = !$event"
        @error="validation.clientName = !$event"
      />
      <text-input-component
        id="acronym"
        v-if="displayEditable('acronym')"
        class="grouping-02--width-8rem"
        label="Acronym"
        placeholder=""
        autocomplete="off"
        v-model="formData.clientAcronym"
        :validations="[
          ...getValidations('businessInformation.clientAcronym'),
          submissionValidation(`businessInformation.clientAcronym`),
        ]"
        enabled
        @empty="validation.acronym = true"
        @error="validation.acronym = !$event"
      />
    </div>
    <text-input-component
      id="doingBusinessAs"
      v-if="displayEditable('doingBusinessAs')"
      label="Doing business as"
      placeholder=""
      autocomplete="off"
      v-model="doingBusinessAs"
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
      url="/api/codes/client-types"
      :min-length="0"
      :init-value="[]"
      :init-fetch="true"
      :params="{ method: 'GET' }"
      #="{ content }"
    >
      <combo-box-input-component
        id="clientType"
        label="Client type"
        :initial-value="content?.find((item) => item.code === formData.clientTypeCode)?.name"
        required
        required-label
        :model-value="content"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('clientType.text'),
          submissionValidation('businessInformation.clientType'),
        ]"
        @update:selected-value="updateClientType($event)"
        @empty="validation.clientType = !$event"
      />
    </data-fetcher>
    <text-input-component
      id="registrationNumber"
      v-if="displayEditable('registrationNumber')"
      label="Registration number"
      mask="NNNNNNNNNNN"
      placeholder=""
      autocomplete="off"
      v-model="clientRegistrationNumber"
      :validations="[]"
      enabled
      @empty="validation.workSafeBCNumber = true"
      @error="validation.workSafeBCNumber = !$event"
    />
    <text-input-component
      id="workSafeBCNumber"
      v-if="displayEditable('workSafeBCNumber')"
      label="WorkSafeBC number"
      mask="######"
      placeholder=""
      autocomplete="off"
      v-model="formData.wcbFirmNumber"
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
      url="/api/codes/client-statuses"
      :min-length="0"
      :init-value="[]"
      :init-fetch="true"
      :params="{ method: 'GET' }"
      #="{ content }"
    >
      <dropdown-input-component
        id="clientStatus"
        label="Client status"
        :initial-value="content?.find((item) => item.code === formData.clientStatusCode)?.name"
        required
        required-label
        :model-value="content"
        :enabled="true"
        tip=""
        :validations="[]"
        @update:selected-value="updateClientStatus($event)"
        @empty="validation.clientType = !$event"
        #="{ option }"
      >
        <cds-tag :type="getTagColorByClientStatus(option.name)" title="">
          <span>{{ option.name }}</span>
        </cds-tag>
      </dropdown-input-component>
    </data-fetcher>
    <textarea-input-component
      id="notes"
      v-if="displayEditable('notes')"
      label="Notes"
      enable-counter
      :max-count="4000"
      :rows="7"
      placeholder=""
      v-model="formData.clientComment"
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
            For example, any information about the client, their locations or specific instructions for contacting them
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
