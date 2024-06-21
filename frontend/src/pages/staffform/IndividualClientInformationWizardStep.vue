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
import type { CodeNameType, IdentificationType } from "@/dto/CommonTypesDto";
// Importing validators
import { getValidations, validate } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
import {
  idNumberMaskParams,
  getIdNumberValidations,
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
  () => emit("update:data", formData.value),
);

const identificationTypeList: IdentificationType[] = [
  { code: "BRTH", name: "Canadian birth certificate" },
  { code: "CDDL", name: "Canadian driver's licence" },
  { code: "PASS", name: "Canadian passport" },
  { code: "CITZ", name: "Canadian citizenship card" },
  { code: "FNID", name: "First Nation status ID" },
  { code: "USDL", name: "US driver's licence" },
  { code: "OTHR", name: "Other Identification" },
];

const identificationType = computed(() => {
  const value = formData.value.businessInformation.identificationType;
  return value ? identificationTypeList.find((item) => item.code === value) : undefined;
});

const identificationProvince = computed(() => {
  const value = formData.value.businessInformation.identificationProvince;
  return value ? provinceList.value.find((item) => item.code === value) : undefined;
});

const updateIdType = (value: IdentificationType | undefined) => {
  formData.value.businessInformation.identificationType = value?.code;
};

const provinceUrl = computed(() => {
  const countryCode = formData.value.businessInformation.identificationCountry;
  if (countryCode) {
    return `/api/countries/${countryCode}/provinces?page=0&size=250`;
  }
  return "";
});

const provinceList = ref<CodeNameType[]>([]);

const { fetch: fetchProvinceList } = useFetchTo(provinceUrl, provinceList, {
  skip: !provinceUrl.value,
});

watch(identificationType, (value) => {
  formData.value.businessInformation.identificationProvince = null;

  // is driver's licence
  if (["CDDL", "USDL"].includes(value?.code)) {
    // TODO: this should be removed/updated when FSADT1-1383 is done
    formData.value.businessInformation.identificationCountry =
      identificationType.value?.code === "USDL" ? "US" : "CA";

    fetchProvinceList();
  }
});

watch(provinceList, () => {
  if (
    identificationType.value.code === "CDDL" &&
    (!identificationProvince.value || !identificationProvince.value.code)
  ) {
    // default value for Issuing province when ID type is Canadian driver's licence
    formData.value.businessInformation.identificationProvince = "BC";
  }
});

const issuingProvinceNaming = computed(() => {
  const countryCode = formData.value.businessInformation.identificationCountry;
  return countryCode === "US" ? "Issuing state" : "Issuing province";
});

const updateIssuingProvince = (value: CodeNameType | undefined) => {
  formData.value.businessInformation.identificationProvince = value?.code;
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

const shouldDisplayProvince = computed(() =>
  ["CDDL", "USDL"].includes(identificationType.value?.code),
);

watch(shouldDisplayProvince, (value) => {
  if (!value) {
    validation.identificationProvince = true;
  }
});

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  firstName: false,
  middleName:
    !formData.value.businessInformation.middleName || // since middleName is not required
    validate(["businessInformation.middleName"], formData.value),
  lastName: false,
  birthdate: false,
  identificationType: false,
  identificationProvince: !shouldDisplayProvince.value,
  clientIdentification: false,
});

const watcherTypeAndProvinceFirstRun = ref(true);

watch(
  [
    () => formData.value.businessInformation.identificationType as IdentificationType["code"],
    () => formData.value.businessInformation.identificationProvince,
  ],
  ([identificationTypeCode, identificationProvinceCode]) => {
    idNumberAdditionalValidations.value = [];

    const oldIdNumberMask = idNumberMask.value;
    idNumberMask.value = undefined;

    if (identificationTypeCode) {
      if (identificationTypeCode === "CDDL" || identificationTypeCode === "USDL") {
        if (identificationProvinceCode) {
          // Driver's licences
          if (identificationTypeCode === "CDDL" && identificationProvinceCode === "BC") {
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
        idNumberAdditionalValidations.value = getIdNumberValidations(
          `businessInformation.idNumber-${identificationTypeCode}`,
        );
        idNumberMask.value = getIdNumberMask(identificationTypeCode);
      }

      /*
      We need to clear the ID number when the idType gets updated, except when the input mask is
      the same.
      The following condition also prevents from doing it when the idType did not actually changed,
      which could happen when rendering the component with information already filled up.
      */
      if (
        !watcherTypeAndProvinceFirstRun.value &&
        (idNumberMask.value || oldIdNumberMask) &&
        idNumberMask.value !== oldIdNumberMask &&
        formData.value.businessInformation.clientIdentification
      ) {
        formData.value.businessInformation.clientIdentification = "";
      }
    }

    watcherTypeAndProvinceFirstRun.value = false;
  },
  { immediate: true },
);

watch(
  () => formData.value.businessInformation.firstName,
  (value) => {
    // copy the firstName into the first contact
    formData.value.location.contacts[0].firstName = value;
  },
);

watch(
  () => formData.value.businessInformation.lastName,
  (value) => {
    // copy the lastName into the first contact
    formData.value.location.contacts[0].lastName = value;
  },
);

const fullName = computed(() => {
  const parts: string[] = [];
  parts.push(formData.value.businessInformation.firstName);
  if (formData.value.businessInformation.middleName) {
    parts.push(formData.value.businessInformation.middleName);
  }
  parts.push(formData.value.businessInformation.lastName);
  return parts.join(" ");
});

// set the business name as the individual's full name
watch(fullName, (fullNameValue) => {
  formData.value.businessInformation.businessName = fullNameValue;
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
      v-model="formData.businessInformation.firstName"
      :validations="[
        ...getValidations('businessInformation.firstName'),
        submissionValidation(`businessInformation.firstName`),
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
      @empty="validation.middleName = true"
      @error="validation.middleName = !$event"
    />

    <text-input-component
      id="lastName"
      label="Last name"
      placeholder=""
      autocomplete="off"
      v-model="formData.businessInformation.lastName"
      :validations="[
        ...getValidations('businessInformation.lastName'),
        submissionValidation(`businessInformation.lastName`),
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
        :initial-value="identificationType?.name"
        required
        required-label
        :model-value="identificationTypeList"
        :enabled="true"
        tip=""
        :validations="[...getValidations('idType.text'), submissionValidation('idType.text')]"
        @update:selected-value="updateIdType($event as IdentificationType)"
        @empty="validation.identificationType = !$event"
      />

      <dropdown-input-component
        v-if="shouldDisplayProvince"
        id="issuingProvince"
        :label="issuingProvinceNaming"
        required
        required-label
        :initial-value="identificationProvince?.name"
        :model-value="provinceList"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('issuingProvince.text'),
          submissionValidation('issuingProvince.text'),
        ]"
        @update:selected-value="updateIssuingProvince($event)"
        @empty="validation.identificationProvince = !$event"
      />

      <text-input-component
        id="idNumber"
        label="ID number"
        placeholder=""
        autocomplete="off"
        v-model="formData.businessInformation.clientIdentification"
        :mask="idNumberMask"
        :validations="[
          ...getValidations('businessInformation.idNumber'),
          ...idNumberAdditionalValidations,
          submissionValidation('businessInformation.idNumber'),
        ]"
        enabled
        required
        required-label
        @empty="validation.clientIdentification = !$event"
        @error="validation.clientIdentification = !$event"
      />
    </div>
  </div>
</template>
