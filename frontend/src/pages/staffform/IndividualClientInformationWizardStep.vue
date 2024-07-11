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
  () => emit("update:data", formData.value),
);

const identificationTypeList = ref([]);
useFetchTo("/api/codes/identification-types", identificationTypeList);

const identificationType = computed(() => {
  const value = formData.value.businessInformation.identificationType;
  return value ? identificationTypeList.value.find((item) => item.code === value) : undefined;
});

const identificationProvince = computed(() => {
  const value = formData.value.businessInformation.identificationProvince;
  return value ? provinceList.value.find((item) => item.code === value) : undefined;
});

const updateIdentificationType = (value: IdentificationType | undefined) => {
  formData.value.businessInformation.identificationType = value?.code;
};

const provinceUrl = computed(() => {
  const countryCode = formData.value.businessInformation.identificationCountry;
  if (countryCode) {
    return `/api/codes/countries/${countryCode}/provinces?page=0&size=250`;
  }
  return "";
});

const provinceList = ref<CodeNameType[]>([]);

const { fetch: fetchProvinceList } = useFetchTo(provinceUrl, provinceList, {
  skip: !provinceUrl.value,
});

watch(identificationType, (value) => {
  formData.value.businessInformation.identificationProvince = null;
  formData.value.businessInformation.identificationCountry = identificationType.value?.countryCode;
  fetchProvinceList();
});

watch(provinceList, () => {
  if (
    identificationType.value.countryCode === "CA" &&
    (!identificationProvince.value || !identificationProvince.value.code)
  ) {
    // Default value for Issuing province when ID type is Canadian driver's licence
    formData.value.businessInformation.identificationProvince = "BC";
  }
});

const identificationProvinceLabel = computed(() => {
  const countryCode = formData.value.businessInformation.identificationCountry;
  return countryCode === "US" ? "Issuing state" : "Issuing province";
});

const updateIdentificationProvince = (value: CodeNameType | undefined) => {
  formData.value.businessInformation.identificationProvince = value?.code;
};

const clientIdentificationAdditionalValidations = ref<((value: string) => string)[]>([]);

const getClientIdentificationMask = (type: keyof typeof clientIdentificationMaskParams) => {
  if (clientIdentificationMaskParams[type]) {
    const token = clientIdentificationMaskParams[type].onlyNumbers ? "#" : "N";
    return token.repeat(clientIdentificationMaskParams[type].maxSize);
  }
  return undefined;
};

const clientIdentificationMask = ref<string>();

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
    clientIdentificationAdditionalValidations.value = [];

    const oldClientIdentificationMask = clientIdentificationMask.value;
    clientIdentificationMask.value = undefined;

    if (identificationTypeCode) {
      if (identificationTypeCode === "CDDL" || identificationTypeCode === "USDL") {
        if (identificationProvinceCode) {
          // Driver's licences
          if (identificationTypeCode === "CDDL" && identificationProvinceCode === "BC") {
            // BC driver's licences
            clientIdentificationAdditionalValidations.value = getClientIdentificationValidations(
              "businessInformation.clientIdentification-BCDL",
            );
            clientIdentificationMask.value = getClientIdentificationMask("BCDL");
          } else {
            // Every other driver's licences, including both Canadian or US.
            clientIdentificationAdditionalValidations.value = getClientIdentificationValidations(
              "businessInformation.clientIdentification-nonBCDL",
            );
            clientIdentificationMask.value = getClientIdentificationMask("nonBCDL");
          }
        }
      } else {
        // Every other ID type
        clientIdentificationAdditionalValidations.value = getClientIdentificationValidations(
          `businessInformation.clientIdentification-${identificationTypeCode}`,
        );
        clientIdentificationMask.value = getClientIdentificationMask(identificationTypeCode);
      }

      /*
      We need to clear the clientIdentification when the type/province gets updated, except when
      the input mask is the same.
      The following condition also prevents from doing it when the type/province did not actually
      changed, which could happen when rendering the component with information already filled up.
      */
      if (
        !watcherTypeAndProvinceFirstRun.value &&
        (clientIdentificationMask.value || oldClientIdentificationMask) &&
        clientIdentificationMask.value !== oldClientIdentificationMask &&
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
      <div class="label-with-icon parent-label">
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
        id="identificationType"
        label="ID type"
        :initial-value="identificationType?.name"
        required
        required-label
        :model-value="identificationTypeList"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('identificationType.text'),
          submissionValidation('identificationType.text'),
        ]"
        @update:selected-value="updateIdentificationType($event as IdentificationType)"
        @empty="validation.identificationType = !$event"
      />

      <dropdown-input-component
        v-if="shouldDisplayProvince"
        id="identificationProvince"
        :label="identificationProvinceLabel"
        required
        required-label
        :initial-value="identificationProvince?.name"
        :model-value="provinceList"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('identificationProvince.text'),
          submissionValidation('identificationProvince.text'),
        ]"
        @update:selected-value="updateIdentificationProvince($event)"
        @empty="validation.identificationProvince = !$event"
      />

      <text-input-component
        id="clientIdentification"
        label="ID number"
        placeholder=""
        autocomplete="off"
        v-model="formData.businessInformation.clientIdentification"
        :mask="clientIdentificationMask"
        :validations="[
          ...getValidations('businessInformation.clientIdentification'),
          ...clientIdentificationAdditionalValidations,
          submissionValidation('businessInformation.clientIdentification'),
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
