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
import type { CodeNameType, IdType } from "@/dto/CommonTypesDto";
import type { Ref } from "vue";
// Importing validators
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
import {
  idNumberMaskParams,
  getIdNumberValidations,
} from "@/helpers/validators/StaffFormValidations";
// @ts-ignore
import Information16 from "@carbon/icons-vue/es/information/16";

export interface IndividualVirtualFields {
  idType: Ref<IdType>;
  issuingProvince: Ref<CodeNameType>;
}

// Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{
  data: FormDataDto;
  active: boolean;
  autoFocus?: boolean;
  virtualFields: IndividualVirtualFields;
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

const idTypeList: IdType[] = [
  { code: "BRTH", name: "Canadian birth certificate" },
  { code: "CDL", name: "Canadian driver's licence" },
  { code: "PASS", name: "Canadian passport" },
  { code: "CITZ", name: "Canadian citizenship card" },
  { code: "FNID", name: "First Nation status ID" },
  { code: "USDL", name: "US driver's licence" },
  { code: "OTHR", name: "Other Identification" },
];

const idType = props.virtualFields.idType;
const issuingProvince = props.virtualFields.issuingProvince;

const updateIdType = (value: IdType | undefined) => {
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
  issuingProvince.value = null;

  // is driver's licence
  if (["CDL", "USDL"].includes(idTypeValue?.code)) {
    fetchProvinceList();
  }
});

watch(provinceList, (provinceListValue) => {
  if (idType.value.code === "CDL" && (!issuingProvince.value || !issuingProvince.value.code)) {
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

const idNumberAdditionalValidations = ref<((value: string) => string)[]>([]);

const getIdNumberMask = (type: keyof typeof idNumberMaskParams) => {
  if (idNumberMaskParams[type]) {
    const token = idNumberMaskParams[type].onlyNumbers ? "#" : "N";
    return token.repeat(idNumberMaskParams[type].maxSize);
  }
  return undefined;
};

const idNumberMask = ref<string>();

watch([idType, issuingProvince], ([idTypeValue, issuingProvinceValue]) => {
  formData.value.businessInformation.idType = null;
  idNumberAdditionalValidations.value = [];

  const oldIdNumberMask = idNumberMask.value;
  idNumberMask.value = undefined;

  if (idTypeValue) {
    if (idTypeValue.code === "CDL" || idTypeValue.code === "USDL") {
      if (issuingProvinceValue?.code) {
        // Driver's licences
        if (idTypeValue?.code === "CDL") {
          formData.value.businessInformation.idType = issuingProvinceValue.code + "DL";
        }

        if (idTypeValue?.code === "USDL") {
          formData.value.businessInformation.idType = "US" + issuingProvinceValue.code;
        }

        if (idTypeValue?.code === "CDL" && issuingProvinceValue.code === "BC") {
          // BC driver's licences
          idNumberAdditionalValidations.value = getIdNumberValidations(
            "businessInformation.idNumber-BCDL",
          );
          idNumberMask.value = getIdNumberMask("BCDL");
        } else {
          // Every other driver's licences, including both Canadian or US.
          idNumberAdditionalValidations.value = getIdNumberValidations(
            "businessInformation.idNumber-nonBCDL",
          );
          idNumberMask.value = getIdNumberMask("nonBCDL");
        }
      }
    } else {
      // Every other ID type
      formData.value.businessInformation.idType = idTypeValue.code;

      idNumberAdditionalValidations.value = getIdNumberValidations(
        `businessInformation.idNumber-${idTypeValue.code}`,
      );
      idNumberMask.value = getIdNumberMask(idTypeValue.code);
    }

    if (
      (idNumberMask.value || oldIdNumberMask) &&
      idNumberMask.value !== oldIdNumberMask &&
      formData.value.businessInformation.idNumber
    ) {
      /*
      Clear the ID number to prevent bad data hidden by the update on the mask and not revalidated.
      */
      formData.value.businessInformation.idNumber = "";
    }
  }

  validation.idType = !!formData.value.businessInformation.idType;
});

const fullName = computed(() => {
  const parts: string[] = [];
  parts.push(formData.value.location.contacts[0].firstName);
  if (formData.value.businessInformation.middleName) {
    parts.push(formData.value.businessInformation.middleName);
  }
  parts.push(formData.value.location.contacts[0].lastName);
  return parts.join(" ");
});

// set the business name as the individual's full name
watch(fullName, (fullNameValue) => {
  formData.value.businessInformation.businessName = fullNameValue;
});

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  firstName: false,
  middleName: true,
  lastName: false,
  birthdate: false,
  idType: false,
  idNumber: false,
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
        :validations="[...getValidations('idType.text'), submissionValidation('idType.text')]"
        @update:selected-value="updateIdType($event as IdType)"
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
          ...getValidations('issuingProvince.text'),
          submissionValidation('issuingProvince.text'),
        ]"
        @update:selected-value="updateIssuingProvince($event)"
      />

      <text-input-component
        id="idNumber"
        label="ID number"
        placeholder=""
        autocomplete="off"
        v-model="formData.businessInformation.idNumber"
        :mask="idNumberMask"
        :validations="[
          ...getValidations('businessInformation.idNumber'),
          ...idNumberAdditionalValidations,
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
