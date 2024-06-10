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
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
import type { CodeNameType } from "@/dto/CommonTypesDto";
// Importing validators
import { getValidations } from "@/helpers/validators/GlobalValidators";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
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
  () => emit("update:data", formData.value),
);

// TODO: get this list from the back-end
const idTypeList: CodeNameType[] = [
  { code: "CBC", name: "Canadian birth certificate" },
  { code: "CDL", name: "Canadian driver's licence" },
  { code: "CM", name: "Canadian military ID" },
  { code: "CP", name: "Canadian passport" },
  { code: "CCC", name: "Canadian citizenship card" },
  { code: "FNS", name: "First Nation status ID" },
  { code: "USDL", name: "US driver's licence" },
];
const idType = ref<CodeNameType>();
const issuingProvince = ref<CodeNameType>();

const updateIdType = (value: CodeNameType | undefined) => {
  idType.value = value;
};

const provinceUrl = computed(() => {
  if (idType.value) {
    const countryCode = idType.value.code === "USDL" ? "US" : "CA";
    return `/api/countries/${countryCode}/provinces?page=0&size=250`;
  }
  return "";
});

const provinceList = ref<CodeNameType[]>([]);

const { fetch: fetchProvinceList } = useFetchTo(provinceUrl, provinceList, {
  skip: !provinceUrl.value,
});

watch(idType, (idTypeValue) => {
  formData.value.businessInformation.idType = idTypeValue?.code;

  // is driver's licence
  if (["CDL", "USDL"].includes(idType.value.code)) {
    fetchProvinceList();
  }
});

watch(provinceList, (provinceListValue) => {
  issuingProvince.value = null;
  if (idType.value.code === "CDL") {
    // default value for Issuing province when ID type is Canadian driver's licence
    issuingProvince.value = provinceListValue.find((province) => province.code === "BC");
  }
});

const issuingProvinceNaming = computed(() => {
  const countryCode = idType.value?.code === "USDL" ? "US" : "CA";
  return countryCode === "US" ? "Issuing state" : "Issuing province";
});

const updateIssuingProvince = (value: CodeNameType | undefined) => {
  issuingProvince.value = value;
};

watch(issuingProvince, (issuingProvinceValue) => {
  formData.value.businessInformation.issuingProvince = issuingProvinceValue?.code;
});

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  firstName: false,
  middleName: true,
  lastName: false,
  business: false,
  birthdate: false,
  idType: false,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) => accumulator && currentValue,
    true,
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
      id="firstName"
      label="First name"
      placeholder=""
      autocomplete="off"
      v-model="formData.location.contacts[0].firstName"
      :validations="[
        ...getValidations('location.contacts.*.firstName'),
        submissionValidation(`location.contacts[0].firstName`),
      ]"
      enabled
      required
      required-label
      @empty="validation.firstName = !$event"
      @error="validation.firstName = !$event"
    />

    <text-input-component
      id="middleName"
      label="Middle name"
      placeholder=""
      autocomplete="off"
      v-model="formData.businessInformation.middleName"
      :validations="[
        ...getValidations('businessInformation.middleName'),
        submissionValidation(`businessInformation.middleName`),
      ]"
      enabled
      @empty="validation.middleName = !$event"
      @error="validation.middleName = !$event"
    />

    <text-input-component
      id="lastName"
      label="Last name"
      placeholder=""
      autocomplete="off"
      v-model="formData.location.contacts[0].lastName"
      :validations="[
        ...getValidations('location.contacts.*.lastName'),
        submissionValidation(`location.contacts[0].lastName`),
      ]"
      enabled
      required
      required-label
      @empty="validation.lastName = !$event"
      @error="validation.lastName = !$event"
    />

    <div>
      <div class="label-with-icon">
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
        id="birthdate"
        title="Date of birth"
        :autocomplete="['bday-year', 'bday-month', 'bday-day']"
        v-model="formData.businessInformation.birthdate"
        :enabled="true"
        :validations="[...getValidations('businessInformation.birthdate')]"
        :year-validations="[...getValidations('businessInformation.birthdate.year')]"
        @error="validation.birthdate = !$event"
        @possibly-valid="validation.birthdate = $event"
        required
      />
    </div>

    <div class="horizontal-input-grouping">
      <dropdown-input-component
        id="idType"
        label="ID type"
        :initial-value="idType?.name"
        required
        required-label
        :model-value="idTypeList"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('businessInformation.idType'),
          submissionValidation('businessInformation.idType'),
        ]"
        @update:selected-value="updateIdType($event)"
        @empty="validation.district = !$event"
      />

      <dropdown-input-component
        v-if="['CDL', 'USDL'].includes(idType?.code)"
        id="issuingProvince"
        :label="issuingProvinceNaming"
        required
        required-label
        :initial-value="issuingProvince?.name"
        :model-value="provinceList"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('businessInformation.issuingProvince'),
          submissionValidation('businessInformation.issuingProvince'),
        ]"
        @update:selected-value="updateIssuingProvince($event)"
        @empty="validation.province = !$event"
      />

      <text-input-component
        id="idNumber"
        label="ID number"
        placeholder=""
        autocomplete="off"
        v-model="formData.businessInformation.idNumber"
        :validations="[
          ...getValidations('businessInformation.idNumber'),
          submissionValidation('businessInformation.idNumber'),
        ]"
        enabled
        required
        required-label
        @empty="validation.idNumber = !$event"
        @error="validation.idNumber = !$event"
      />
    </div>
  </div>
</template>
