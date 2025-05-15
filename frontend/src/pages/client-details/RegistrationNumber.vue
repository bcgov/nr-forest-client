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
  runLocalValidations();
};

const displayedError = ref<string>();

const updateRegistrationNumberFirstError = (firstError: string) => {
  displayedError.value =
    firstError ||
    registryTypeError.value ||
    localTypeError.value ||
    registryNumberError.value ||
    localNumberError.value;
};

const localTypeError = ref("");

const localNumberError = ref("");

const hasChanged = () => {
  const typeChanged =
    modelValue.client.registryCompanyTypeCode !== originalValue.client.registryCompanyTypeCode;

  const numberChanged = modelValue.client.corpRegnNmbr !== originalValue.client.corpRegnNmbr;

  return typeChanged || numberChanged;
};

const validateType = (): string => {
  if (!hasChanged()) {
    return "";
  }

  if (!modelValue.client.registryCompanyTypeCode && modelValue.client.corpRegnNmbr) {
    return "You must provide a type if the number is filled in";
  }

  return "";
};

const validateNumber = (): string => {
  if (!hasChanged()) {
    return "";
  }

  if (modelValue.client.registryCompanyTypeCode && !modelValue.client.corpRegnNmbr) {
    return "You must provide a number if a type is selected";
  }

  return "";
};

const runLocalValidations = () => {
  localTypeError.value = validateType();
  localNumberError.value = validateNumber();
};

const registryTypeError = ref("");

const registryTypeValidation = ref(true);

const setRegistryTypeError = (error: string) => {
  registryTypeError.value = error;
  runLocalValidations();
  updateRegistrationNumberFirstError(error);
  registryTypeValidation.value = !error && !localTypeError.value;
};

const setRegistryTypeEmpty = (_empty: boolean) => {
  registryTypeValidation.value = true;
};

const registryNumberError = ref("");

const registryNumberValidation = ref(true);

const setRegistryNumberError = (error: string) => {
  registryNumberError.value = error;
  runLocalValidations();
  updateRegistrationNumberFirstError(error);
  registryNumberValidation.value = !error && !localNumberError.value;
};

const setRegistryNumberEmpty = (_empty: boolean) => {
  runLocalValidations();
  registryNumberValidation.value = !registryNumberError.value && !localNumberError.value;
};

watch([registryTypeValidation, registryNumberValidation], () => {
  const valid = registryTypeValidation.value && registryNumberValidation.value;
  emit("valid", valid);
});

emit("valid", true);
</script>
<template>
  <div class="label-with-icon line-height-0 parent-label">
    <div class="cds-text-input-label">
      <span>Registration number</span>
    </div>
  </div>
  <div class="horizontal-input-grouping combined-inputs">
    <combo-box-input-component
      id="input-registryType"
      class="grouping-03--width-32rem"
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
      :error-message="localTypeError"
      @update:selected-value="updateRegistryType($event)"
      @error="setRegistryTypeError($event)"
      @empty="setRegistryTypeEmpty($event)"
    />
    <text-input-component
      id="input-registryNumber"
      class="grouping-02--width-8rem"
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
      :error-message="localNumberError"
      @empty="setRegistryNumberEmpty($event)"
      @error="setRegistryNumberError($event)"
    />
  </div>
  <div class="cds--form-requirement field-error">{{ displayedError }}</div>
</template>
