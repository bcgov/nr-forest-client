<script setup lang="ts">
import { watch, ref, reactive, onMounted } from "vue";
import "@carbon/web-components/es/components/inline-loading/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/tooltip/index";
import { useFocus } from "@/composables/useFocus";
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
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
  (newVal) => {
    emit('update:data', newVal);
  },
  { deep: true }
);

watch(
  () => props.data,
  (newData) => {
    formData.value = { ...newData };
  },
  { deep: true }
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

</script>

<template>
  <div class="frame-01">
    <text-input-component
      id="businessName"
      label="Business name"
      placeholder=""
      autocomplete="off"
      v-model="formData.businessInformation.businessName"
      :validations="[
        ...getValidations('businessInformation.businessName'),
        submissionValidation(`businessInformation.businessName`),
      ]"
      enabled
      required
      required-label
      @empty="validation.businessName = !$event"
      @error="validation.businessName = !$event"
    />

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
