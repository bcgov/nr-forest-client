<script setup lang="ts">
import { computed, ref, watch } from "vue";

import { useFetchTo } from "@/composables/useFetch";
import type { ClientDetails, CodeNameType } from "@/dto/CommonTypesDto";
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";

const props = defineProps<{
  modelValue: ClientDetails;
  originalValue: ClientDetails;
}>();

const emit = defineEmits<{
  (e: "valid", value: boolean): void;
}>();

const { modelValue, originalValue } = props;

const registryTypesUrl = computed(
  () => `/api/codes/registry-types/${modelValue.client.clientTypeCode}`,
);
const registryTypesList = ref([]);
const fetchRegistryTypes = useFetchTo(registryTypesUrl, registryTypesList, { skip: true });
watch(
  registryTypesUrl,
  () => {
    fetchRegistryTypes.fetch();
  },
  { immediate: true },
);
const formattedRegistryTypesList = computed(() =>
  registryTypesList.value.map((registryType) => ({
    ...registryType,
    name: `${registryType.code} - ${registryType.name}`,
  })),
);

const updateRegistryType = (value: CodeNameType) => {
  modelValue.client.registryCompanyTypeCode = value.code;
};

/**
 * This is the condensed error message that will be displayed.
 */
const registrationNumberError = ref<string>();

const registryJoinedFieldsError = ref<string>();

const updateRegistrationNumberFirstError = (firstError: string) => {
  registrationNumberError.value =
    firstError ||
    registryTypeError.value ||
    registryNumberError.value ||
    registryJoinedFieldsError.value;
};

const validateRegistrationNumberJoinedFields = () => {
  registryJoinedFieldsError.value = undefined;

  const typeChanged =
    modelValue.client.registryCompanyTypeCode !== originalValue.client.registryCompanyTypeCode;

  const numberChanged = modelValue.client.corpRegnNmbr !== originalValue.client.corpRegnNmbr;

  if (!typeChanged && !numberChanged) {
    return;
  }

  if (modelValue.client.registryCompanyTypeCode && !modelValue.client.corpRegnNmbr) {
    registryJoinedFieldsError.value =
      "When Type is non-empty you must also provide the Number part for the registration number";
  }

  if (!modelValue.client.registryCompanyTypeCode && modelValue.client.corpRegnNmbr) {
    registryJoinedFieldsError.value =
      "When the Number part is non-empty you must also provide the Type for the registration number";
  }
};

const registryTypeError = ref<string>();

const registryTypeValidation = ref(true);

const setRegistryTypeError = (error: string) => {
  registryTypeError.value = error;
  updateRegistrationNumberFirstError(error);
  registryTypeValidation.value = !error;
};

const setRegistryTypeEmpty = (_empty: boolean) => {
  registryTypeValidation.value = true;
  validateRegistrationNumberJoinedFields();
};

const registryNumberError = ref<string>();

const registryNumberValidation = ref(true);

const setRegistryNumberError = (error: string) => {
  registryNumberError.value = error;
  updateRegistrationNumberFirstError(error);
  registryNumberValidation.value = !error;
};

const setRegistryNumberEmpty = (_empty: boolean) => {
  registryNumberValidation.value = true;
  validateRegistrationNumberJoinedFields();
};

watch([registryTypeValidation, registryNumberValidation, registryJoinedFieldsError], () => {
  const valid =
    registryTypeValidation.value &&
    registryNumberValidation.value &&
    !registryJoinedFieldsError.value;
  emit("valid", valid);
  console.log({ valid });
});

emit("valid", true);
</script>
<template>
  <div class="label-with-icon line-height-0 parent-label">
    <div class="cds-text-input-label">
      <span class="cds-text-input-required-label">* </span>
      <span>Registration number</span>
    </div>
  </div>
  <div class="horizontal-input-grouping combined-inputs">
    <combo-box-input-component
      id="input-registryType"
      class="grouping-03--width-20_5rem"
      label="Type"
      :initial-value="
        formattedRegistryTypesList?.find(
          (item) => item.code === modelValue.client.registryCompanyTypeCode,
        )?.name
      "
      required
      :model-value="formattedRegistryTypesList"
      :enabled="true"
      tip=""
      :validations="[
        ...getValidations('client.registryCompanyTypeCode'),
        submissionValidation('client.registryCompanyTypeCode'),
      ]"
      :error-message="registryJoinedFieldsError"
      @update:selected-value="updateRegistryType($event)"
      @error="setRegistryTypeError($event)"
      @empty="setRegistryTypeEmpty($event)"
    />
    <text-input-component
      id="input-registryNumber"
      class="grouping-02--width-9rem"
      label="Number"
      mask="#########"
      placeholder=""
      autocomplete="off"
      v-model="modelValue.client.corpRegnNmbr"
      :validations="[
        ...getValidations('client.corpRegnNmbr'),
        submissionValidation('client.corpRegnNmbr'),
      ]"
      enabled
      :error-message="registryJoinedFieldsError"
      @empty="setRegistryNumberEmpty($event)"
      @error="setRegistryNumberError($event)"
    />
  </div>
  <div class="cds--form-requirement field-error">{{ registrationNumberError }}</div>
</template>
