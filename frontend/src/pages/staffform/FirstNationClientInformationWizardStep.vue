<script setup lang="ts">
import { watch, computed, ref, reactive, onMounted } from "vue";
import "@carbon/web-components/es/components/inline-loading/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/tooltip/index";
import { useFocus } from "@/composables/useFocus";
import type {
  FirstNationDetailsDto,
  ForestClientDetailsDto,
  FormDataDto,
} from "@/dto/ApplyClientNumberDto";
import type { BusinessSearchResult } from "@/dto/CommonTypesDto";
import { formatAddresses } from "@/dto/ApplyClientNumberDto";
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";

// Defining the props and emiter to receive the data and emit an update
const props = defineProps<{
  data: FormDataDto;
  active: boolean;
  autoFocus?: boolean;
}>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

// Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch(
  () => formData.value,
  () => emit("update:data", formData.value)
);

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  businessName: !!formData.value.businessInformation.businessName,
  workSafeBcNumber: true,
  clientAcronym: true
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

const autoCompleteUrl = computed(
  () => `/api/opendata/${formData.value.businessInformation.businessName ?? ""}`
);

const autoCompleteResult = ref<BusinessSearchResult | undefined>(
  {} as BusinessSearchResult
);

const firstNationControl = ref<boolean>();
const showDetailsLoading = ref<boolean>(false);

watch([autoCompleteResult], () => {
  if (autoCompleteResult.value && autoCompleteResult.value.code) {
    showDetailsLoading.value = true;
  }
  firstNationControl.value = false;
});

const updateModelValue = ($event) => {
  validation.businessName =
    !!$event && $event === autoCompleteResult.value?.name;
};

const parseSelectedNation = (
  selectedNation: FirstNationDetailsDto
) => {
  if (selectedNation) {
    validation.businessName = true;
    formData.value.location.addresses = formatAddresses(
      selectedNation.addresses
    );
    formData.value.businessInformation.registrationNumber = `DINA${selectedNation.id}`;
    formData.value.businessInformation.clientType = selectedNation.clientType;
  }
  return selectedNation;
};

const mapFirstNationInfo = (firstNations: ForestClientDetailsDto[] = []) => {
  return firstNations.map((v) => ({
    ...v,
    code: v.id,
  }));
};
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
        id="clientName"
        label="Client name"
        autocomplete="off"
        required
        required-label
        placeholder=""
        tip="Start typing to search for the nation or band"
        v-model="formData.businessInformation.businessName"
        :contents="firstNationControl ? [] : mapFirstNationInfo(content)"
        :validations="[
          ...getValidations('businessInformation.businessName'),
          submissionValidation(`businessInformation.businessName`),
        ]"
        enabled
        :loading="loading"
        @update:selected-value="autoCompleteResult = parseSelectedNation($event)"
        @update:model-value="updateModelValue"
      />
      <cds-inline-loading status="active" v-if="loading">Loading first nation details...</cds-inline-loading>
    </data-fetcher>

    <text-input-component
      id="workSafeBcNumber"
      label="WorkSafeBC Number"
      placeholder=""
      autocomplete="off"
      mask="######"
      v-model="formData.businessInformation.workSafeBcNumber"
      :validations="[
        ...getValidations('businessInformation.workSafeBcNumber'),
        submissionValidation(`businessInformation.workSafeBcNumber`),
      ]"
      enabled
      @empty="validation.workSafeBcNumber = true"
      @error="validation.workSafeBcNumber = !$event"
    />

    <text-input-component
      id="clientAcronym"
      label="Acronym"
      placeholder=""
      autocomplete="off"
      mask="NNNNNNNN"
      v-model="formData.businessInformation.clientAcronym"
      :validations="[
        ...getValidations('businessInformation.clientAcronym'),
        submissionValidation(`businessInformation.clientAcronym`),
      ]"
      enabled
      @empty="validation.clientAcronym = true"
      @error="validation.clientAcronym = !$event"
    />
  </div>
</template>
