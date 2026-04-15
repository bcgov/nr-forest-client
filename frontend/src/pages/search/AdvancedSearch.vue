<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import type { ClientSearchParameters, CodeNameType } from '@/dto/CommonTypesDto';
import { hasOnlyNamingCharacters, isAscii, isDateAfter, isDateBefore, isMaxSizeMsg, optional } from '@/helpers/validators/GlobalValidators';
import { isValid } from 'date-fns/isValid';

const emit = defineEmits<{
  (e: "search"): void;
}>();

const modelActive = defineModel<boolean>("active", { required: true });

const modelFilters = defineModel<ClientSearchParameters>("filters", { required: true });

const typedIDIR = ref<string>();

const getNamingValidations = (fieldName: string, length = 30) => [
  isMaxSizeMsg(fieldName, length),
  hasOnlyNamingCharacters(fieldName),
];

const validationState = reactive<{ [key in keyof ClientSearchParameters]: boolean}>({});

const isAdvancedDataValid = computed(() => Object.keys(validationState).every((field) => validationState[field]));

const invalidSelectionErrorMessage = "Select a value from the list or clear the input value";

/**
 * This is a "validator" that actually just checks the current validation state, and if false
 * returns the error message for an "invalid selection". Otherwise returns an empty string.
 * @param field - the field name to be checked in the validationState record.
 * @returns the error message when invalid, empty string otherwise
 */
const isInvalidSelectedValue = (field: keyof ClientSearchParameters) => () => {
  if (!validationState[field]) {
    return invalidSelectionErrorMessage;
  }
  return "";
}

const emailCharacters: RegExp = /^[a-zA-Z0-9.@_-]*$/;

const hasOnlyEmailCharacters =
  (
    field: string = "field",
    message: string = `The ${field} can only contain: A-Z, a-z, 0-9, @, underscore or hyphen`
  ) =>
  (value: string): string => {
    if (emailCharacters.test(value)) return "";
    return message;
  };

const validations = reactive<{ [key in keyof ClientSearchParameters]: ((value: string) => string)[]}>({
  clientName: getNamingValidations("business name / last name"),
  firstName: getNamingValidations("first name"),
  middleName: getNamingValidations("middle name"),
  userId: [isInvalidSelectedValue("userId")],
  clientIdentification: [
    isMaxSizeMsg("ID number", 40),
    isAscii("ID number"),
  ],
  contactName: getNamingValidations("contact name", 60),
  emailAddress: [
    isMaxSizeMsg("email", 100),
    hasOnlyEmailCharacters("email"),
  ],
  updatedFromDate: [],
  updatedToDate: [],
});

watch([() => modelFilters.value.updatedFromDate, () => modelFilters.value.updatedToDate], ([newFrom, newTo], [oldFrom, oldTo]) => {
  if (newTo !== oldTo) {
    const isDateValid = isValid(new Date(newTo));
    validations.updatedFromDate = isDateValid ? [
      optional(isDateBefore(
        modelFilters.value.updatedToDate,
        "The “update from date” must be before the “update to date” (or empty)",
      )),
    ] : [];
  }
  if (newFrom !== oldFrom) {
    const isDateValid = isValid(new Date(newFrom));
    validations.updatedToDate = isDateValid ? [
      optional(isDateAfter(
        modelFilters.value.updatedFromDate,
        "The “update to date” must be after the “update from date” (or empty)",
      )),
    ] : [];
  }
});

const clientUsersUrl = computed(
  () =>
    `/api/clients/client-users?userId=${typedIDIR.value || ""}`
);

const stringToCodeName = (value: string): CodeNameType => ({code: value, name: value});

const selectCode = (key: string) => (value: CodeNameType | undefined) => {
  modelFilters.value[key] = value ? value.code : undefined;
  if (value) {
    validationState[key] = true;
  }
};

const getNameFrom = (list: CodeNameType[]) => (code: string) => {
  return list?.find((item) => item.code === code)?.name;
};

const codeNamesToCodes = (
  values: CodeNameType[] | undefined
): string[] => {
  if (values) return values.map((item) => item.code);
  return [];
};

const search = () => {
  emit("search");
  modelActive.value = false;
};
</script>
<template>
  <cds-modal
    id="advanced-modal"
    aria-labelledby="advanced-modal-heading"
    aria-describedby="advanced-modal-body"
    size="sm"
    :open="modelActive"
    @cds-modal-closed="modelActive = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="advanced-modal-heading">
        Advanced search
      </cds-modal-heading>
    </cds-modal-header>
    <cds-modal-body id="advanced-modal-body" data-modal-primary-focus>
      <div class="advanced-form">
        <div class="horizontal-input-grouping-2">
          <text-input-component
            id="clientName"
            label="Business name / Last name"
            placeholder=""
            tip=""
            autocomplete="off"
            v-model="modelFilters.clientName"
            :validations="validations.clientName"
            @empty="validationState.clientName = true"
            @error="validationState.clientName = !$event"
          />
          <text-input-component
            id="firstName"
            label="First name"
            placeholder=""
            tip=""
            autocomplete="off"
            v-model="modelFilters.firstName"
            :validations="validations.firstName"
            @empty="validationState.firstName = true"
            @error="validationState.firstName = !$event"
          />
        </div>
        <div class="horizontal-input-grouping-2">
          <text-input-component
            id="middleName"
            label="Middle name"
            placeholder=""
            tip=""
            autocomplete="off"
            v-model="modelFilters.middleName"
            :validations="validations.middleName"
            @empty="validationState.middleName = true"
            @error="validationState.middleName = !$event"
          />
          <data-fetcher
            v-model:url="clientUsersUrl"
            :min-length="3"
            :init-value="[]"
            :disabled="!typedIDIR"
            #="{ content, loading }"
          >
            <AutoCompleteInputComponent
              id="userId"
              label="Updated by IDIR"
              autocomplete="off"
              tip=""
              v-model="typedIDIR"
              :contents="content.map(stringToCodeName)"
              :validations="validations.userId"
              :loading="loading"
              @update:selected-value="selectCode('userId')($event)"
              @update:model-value="validationState.userId = false"
              @empty="validationState.userId = validationState.userId || $event"
            />
          </data-fetcher>
        </div>
        <div class="horizontal-input-grouping-2">
          <data-fetcher
            url="/api/codes/identification-types/legacy"
            :min-length="0"
            :init-value="[]"
            :init-fetch="true"
            :params="{ method: 'GET' }"
            #="{ content }"
          >
            <multiselect-input-component
              v-if="content?.length"
              id="clientIdType"
              label="ID type"
              tip=""
              initial-value=""
              :model-value="content"
              :selectedValues="modelFilters.clientIdType?.map(getNameFrom(content))"
              :validations="[]"
              @update:selected-value="modelFilters.clientIdType = codeNamesToCodes($event)"
            />
          </data-fetcher>
          <text-input-component
            id="clientIdentification"
            label="ID number"
            placeholder=""
            tip=""
            autocomplete="off"
            v-model="modelFilters.clientIdentification"
            :validations="validations.clientIdentification"
            @empty="validationState.clientIdentification = true"
            @error="validationState.clientIdentification = !$event"
          />
        </div>
        <div class="horizontal-input-grouping-2">
          <data-fetcher
            url="/api/codes/client-types/legacy"
            :min-length="0"
            :init-value="[]"
            :init-fetch="true"
            :params="{ method: 'GET' }"
            #="{ content }"
          >
            <multiselect-input-component
              v-if="content?.length"
              id="clientType"
              label="Client type"
              tip=""
              initial-value=""
              :model-value="content"
              :selectedValues="modelFilters.clientType?.map(getNameFrom(content))"
              :validations="[]"
              @update:selected-value="modelFilters.clientType = codeNamesToCodes($event)"
            />
          </data-fetcher>
          <data-fetcher
            url="/api/codes/client-statuses"
            :min-length="0"
            :init-value="[]"
            :init-fetch="true"
            :params="{ method: 'GET' }"
            #="{ content }"
          >
            <multiselect-input-component
              v-if="content?.length"
              id="clientStatus"
              label="Status"
              tip=""
              initial-value=""
              :model-value="content"
              :selectedValues="modelFilters.clientStatus?.map(getNameFrom(content))"
              :validations="[]"
              @update:selected-value="modelFilters.clientStatus = codeNamesToCodes($event)"
            />
          </data-fetcher>
        </div>
        <div class="horizontal-input-grouping-2">
          <text-input-component
            id="contactName"
            label="Contact name"
            placeholder=""
            tip=""
            autocomplete="off"
            v-model="modelFilters.contactName"
            :validations="validations.contactName"
            @empty="validationState.contactName = true"
            @error="validationState.contactName = !$event"
          />
          <text-input-component
            id="emailAddress"
            label="Email"
            placeholder=""
            tip=""
            autocomplete="off"
            v-model="modelFilters.emailAddress"
            :validations="validations.emailAddress"
            @empty="validationState.emailAddress = true"
            @error="validationState.emailAddress = !$event"
          />
        </div>
        <div class="horizontal-input-grouping-2">
          <div class="grouping-03">
            <div class="cds-text-input-label">
              <span>Update from date</span>
            </div>
            <date-input-component
              id="updatedFromDate"
              title="Update from date"
              :autocomplete="['off', 'off', 'off']"
              v-model="modelFilters.updatedFromDate"
              :enabled="true"
              :validations="validations.updatedFromDate"
              @error="validationState.updatedFromDate = !$event"
              @possibly-valid="validationState.updatedFromDate = $event"
            />
          </div>
          <div class="grouping-03">
            <div class="cds-text-input-label">
              <span>Update to date</span>
            </div>
            <date-input-component
              id="updatedToDate"
              title="Update to date"
              :autocomplete="['off', 'off', 'off']"
              v-model="modelFilters.updatedToDate"
              :enabled="true"
              :validations="validations.updatedToDate"
              @error="validationState.updatedToDate = !$event"
              @possibly-valid="validationState.updatedToDate = $event"
            />
          </div>
        </div>
      </div>
    </cds-modal-body>

    <cds-modal-footer>
      <cds-modal-footer-button 
        kind="secondary" 
        data-modal-close class="cds--modal-close-btn"
      >
        Cancel
      </cds-modal-footer-button>
      <cds-modal-footer-button
        id="search-advanced-btn"
        kind="primary"
        class="cds--modal-submit-btn"
        @click="search"
        :disabled="!isAdvancedDataValid"
      >
        Search
        <Search16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>
</template>