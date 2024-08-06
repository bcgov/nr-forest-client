<script setup lang="ts">
import { watch, computed, ref, reactive, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/inline-loading/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/tooltip/index";
// Importing composables
import { useEventBus } from "@vueuse/core";
import { useFetchTo } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
// Importing types
import type { FirstNationDetailsDto, ForestClientDetailsDto, FormDataDto } from "@/dto/ApplyClientNumberDto";
import type {
  BusinessSearchResult,
  CodeNameType,
  IdentificationCodeNameType,
} from "@/dto/CommonTypesDto";
// Importing validators
import {
  getValidations,
  validate,
} from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
import {
  clientIdentificationMaskParams,
  getClientIdentificationValidations,
} from "@/helpers/validators/StaffFormValidations";
// @ts-ignore
import Information16 from "@carbon/icons-vue/es/information/16";

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

// Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch(
  () => formData.value,
  () => emit("update:data", formData.value)
);

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  businessName: !!formData.value.businessInformation.businessName,
});

watch(
  () => formData.value.businessInformation.firstName,
  (value) => {
    // copy the firstName into the first contact
    //formData.value.location.contacts[0].firstName = value;
  }
);

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
  () => `/api/opendata/${formData.value.businessInformation.businessName ?? ""}`,
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


const parseSelectedNation = (selectedNation: FirstNationDetailsDto, values: ForestClientDetailsDto[]) => {
  console.log("parseSelectedNation called with:", selectedNation, values);
  if (selectedNation) {
    validation.businessName = true;
    formData.value.businessInformation.goodStandingInd = (selectedNation.goodStanding ?? true) ? "Y" : "N";
    //formData.value.location.addresses = selectedNation.addresses;
  }
  return selectedNation;
};

const updateModelValue = ($event) => {
  validation.businessName = !!$event && $event === autoCompleteResult.value?.name;
}
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
        autocomplete="clientName firstNation"
        required
        required-label
        placeholder=""
        tip="Start typing to search for your nation or band name"
        v-model="formData.businessInformation.businessName"
        :contents="firstNationControl ? [] : content.map((v) => (
          {
            code: v.id,
            name: v.name,
            status:'ACTIVE',
            legalType:'R'
          }
        ))"
        :validations="[
          ...getValidations('businessInformation.businessName'),
          submissionValidation(`businessInformation.businessName`),
        ]"
        enabled
        :loading="loading"
        @update:selected-value="autoCompleteResult = parseSelectedNation($event, content)"
        @update:model-value="updateModelValue"
      />
      <cds-inline-loading status="active" v-if="loading">Loading first nation details...</cds-inline-loading>
    </data-fetcher>

  </div>
</template>
