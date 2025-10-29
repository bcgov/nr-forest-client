<script setup lang="ts">
import { watch, computed, ref, reactive, onMounted, nextTick } from "vue";
// Carbon
import "@carbon/web-components/es/components/inline-loading/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/tooltip/index";
// Importing composables
import { useFetchTo } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
import { useEventBus } from "@vueuse/core";
// Importing types
import type { FormDataDto } from "@/dto/ApplyClientNumberDto";
import type { CodeNameType, IdentificationCodeNameType, UserRole } from "@/dto/CommonTypesDto";
// Importing validators
import {
  getValidations,
  clientIdentificationMaskParams,
} from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
// @ts-ignore
import Information16 from "@carbon/icons-vue/es/information/16";

// Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{
  data: FormDataDto;
  active: boolean;
  autoFocus?: boolean;
  userRoles: UserRole[];
}>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

const isAdmin = computed(() => props.userRoles.includes("CLIENT_ADMIN"));

const isBirthdateRequired = computed(() => !isAdmin.value);

/**
 * This is the *local* validation key for the birthdate field.
 * It's *local* only, in the sense of being the key used only by the front-end code.
 */
const birthdateLocalValidationKey = computed(() =>
  isAdmin.value ? "businessInformation.birthdate-optional" : "businessInformation.birthdate",
);

// Set the prop as a ref, and then emit when it changes
const formData = ref<FormDataDto>(props.data);
watch(
  () => formData.value,
  () => emit("update:data", formData.value)
);

const revalidateBus = useEventBus<string[] | undefined>("revalidate-bus");

const identificationTypeList = ref([]);
useFetchTo("/api/codes/identification-types", identificationTypeList);

const identificationProvince = computed(() => {
  const value = formData.value.businessInformation.identificationProvince;
  return value
    ? provinceList.value.find((item) => item.code === value)
    : undefined;
});

const updateIdentificationType = (
  value: IdentificationCodeNameType | undefined
) => {
  formData.value.businessInformation.identificationType = {
    value: value.code,
    text: value.name,
    countryCode: value.countryCode,
  };
  formData.value.businessInformation.identificationCountry =
    formData.value.businessInformation.identificationType.countryCode;
  formData.value.businessInformation.identificationProvince = "";

  if (formData.value.businessInformation.identificationType.countryCode)
    fetchProvinceList();
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

watch(provinceList, () => {
  if (
    formData.value.businessInformation.identificationType.countryCode === "CA" &&
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

const getClientIdentificationMask = (
  type: keyof typeof clientIdentificationMaskParams
) => {
  if (clientIdentificationMaskParams[type]) {
    const token = clientIdentificationMaskParams[type].onlyNumbers ? "#" : "N";
    return token.repeat(clientIdentificationMaskParams[type].maxSize);
  }
  return undefined;
};

const clientIdentificationMask = ref<string>();

const shouldDisplayProvince = computed(() =>
  ["CDDL", "USDL"].includes(
    formData.value.businessInformation.identificationType?.value
  )
);

const isOtherIdentificationType = computed(() =>
  formData.value.businessInformation.identificationType?.value === "OTHR"
);

watch(shouldDisplayProvince, (value) => {
  if (!value) {
    validation.identificationProvince = true;
  }
});

// -- Validation of the component --
const validation = reactive<Record<string, boolean>>({
  firstName: false,
  middleName: true,
  lastName: false,
  birthdate: !isBirthdateRequired.value,
  identificationType: false,
  identificationProvince: !shouldDisplayProvince.value,
  clientIdentification: false,
});

const clientIdentificationValidationExtras = ref<string>('');

watch(
  [
    () => formData.value.businessInformation.identificationType,
    () => formData.value.businessInformation.identificationProvince,
  ],
  ([identificationType, identificationProvinceCode]) => {
    clientIdentificationValidationExtras.value = "";
    clientIdentificationMask.value = undefined;
    validation.clientIdentification = false;

    if (formData.value.businessInformation.clientIdentification) {
      nextTick(() => revalidateBus.emit(["clientIdentification"]));
    }

    if (identificationType) {
      if (identificationType.value === "OTHR") {
        validation.clientIdentification = true;
      }

      if (
        identificationType.value === "CDDL" ||
        identificationType.value === "USDL"
      ) {
        if (identificationProvinceCode) {
          // Driver's licences
          if (
            identificationType.value === "CDDL" &&
            identificationProvinceCode === "BC"
          ) {
            // BC driver's licences
            clientIdentificationValidationExtras.value =
              "businessInformation.clientIdentification-BCDL";
            clientIdentificationMask.value =
              getClientIdentificationMask("BCDL");
          } else {
            // Every other driver's licences, including both Canadian or US.
            clientIdentificationValidationExtras.value =
              "businessInformation.clientIdentification-nonBCDL";
            clientIdentificationMask.value =
              getClientIdentificationMask("nonBCDL");
          }
        }
      } 
      else {
        // Every other ID type
        clientIdentificationValidationExtras.value =
          `businessInformation.clientIdentification-${identificationType.value}`;
        clientIdentificationMask.value = getClientIdentificationMask(
          identificationType.value
        );
      }
    }
  },
  { immediate: true }
);

watch(
  () => formData.value.businessInformation.firstName,
  (value) => {
    // copy the firstName into the first contact
    formData.value.location.contacts[0].firstName = value;
  }
);

watch(
  () => formData.value.businessInformation.lastName,
  (value) => {
    // copy the lastName into the first contact
    formData.value.location.contacts[0].lastName = value;
  }
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

let clientTypeOfId = ref('');
let clientIdNumber = ref('');

if (formData.value.businessInformation.clientIdentification &&
    formData.value.businessInformation.identificationType.value == 'OTHR'
) {
    const [clientType, clientId] = formData.value.businessInformation.clientIdentification.split(':');
    clientTypeOfId.value = clientType.trim();
    clientIdNumber.value = clientId.trim();
  }

const combinedValue = computed(() => ({
  clientTypeOfId: clientTypeOfId.value,
  clientIdNumber: clientIdNumber.value
}));

// Watch the combined computed property
watch(combinedValue, (newValue) => {
  if (formData.value.businessInformation.identificationType.value == 'OTHR') {
    formData.value.businessInformation.clientIdentification = `${newValue.clientTypeOfId}:${newValue.clientIdNumber}`;
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
      <div class="label-with-icon line-height-0 parent-label">
        <div class="cds-text-input-label">
          <span class="cds-text-input-required-label" v-if="isBirthdateRequired">* </span>
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
        :autocomplete="['off', 'off', 'off']"
        v-model="formData.businessInformation.birthdate"
        :enabled="true"
        :validations="[
          ...getValidations(birthdateLocalValidationKey),
          submissionValidation('businessInformation.birthdate'),
        ]"
        :year-validations="[...getValidations('businessInformation.birthdate.year')]"
        @error="validation.birthdate = !$event"
        @possibly-valid="validation.birthdate = $event"
        :required="isBirthdateRequired"
      />
    </div>

    <div class="horizontal-input-grouping">
      <combo-box-input-component
        id="identificationType"
        label="ID type"
        :initial-value="formData.businessInformation.identificationType?.text"
        required
        required-label
        :model-value="identificationTypeList"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('identificationType.text'),
          submissionValidation('businessInformation.identificationType'),
        ]"
        @update:selected-value="updateIdentificationType($event)"
        @empty="validation.identificationType = !$event"
      />

      <combo-box-input-component
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
        v-if="!isOtherIdentificationType"
        id="clientIdentification"
        label="ID number"
        placeholder=""
        autocomplete="off"
        v-model="formData.businessInformation.clientIdentification"
        :mask="clientIdentificationMask"
        :validations="[
          ...getValidations('businessInformation.clientIdentification'),
          ...getValidations(clientIdentificationValidationExtras),
          submissionValidation('businessInformation.clientIdentification'),
        ]"
        required
        required-label
        @empty="validation.clientIdentification = !$event"
        @error="validation.clientIdentification = !$event"
      />

      <text-input-component
        v-if="isOtherIdentificationType"
        id="clientTypeOfId"
        label="Type of ID"
        placeholder=""
        autocomplete="off"
        v-model="clientTypeOfId"
        :validations="[
          ...getValidations('businessInformation.clientTypeOfId'),
          submissionValidation('businessInformation.clientTypeOfId'),
        ]"
        required
        required-label
        @empty="validation.clientTypeOfId = !$event"
        @error="validation.clientTypeOfId = !$event"
      />

      <text-input-component
        v-if="isOtherIdentificationType"
        id="clientIdNumber"
        label="ID number"
        placeholder=""
        autocomplete="off"
        v-model="clientIdNumber"
        :validations="[
          ...getValidations('businessInformation.clientIdNumber'),
          submissionValidation('businessInformation.clientIdNumber'),
        ]"
        required
        required-label
        @empty="validation.clientIdNumber = !$event"
        @error="validation.clientIdNumber = !$event"
      />

    </div>
  </div>
</template>
