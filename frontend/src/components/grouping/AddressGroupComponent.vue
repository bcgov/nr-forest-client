<script setup lang="ts">
import { reactive, watch, computed, ref, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
// Composables
import { useEventBus } from "@vueuse/core";
import { useFetchTo } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
// Types
import type { CodeNameType, BusinessSearchResult } from "@/dto/CommonTypesDto";
import type { Address } from "@/dto/ApplyClientNumberDto";
// Validators
import { getValidations } from "@/helpers/validators/GlobalValidators";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
// @ts-ignore
import Delete16 from "@carbon/icons-vue/es/trash-can/16";

//Define the input properties for this component
const props = defineProps<{
  id: number;
  modelValue: Address;
  countryList: Array<CodeNameType>;
  validations: Array<Function>;
  revalidate?: boolean;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "valid", value: boolean): void;
  (e: "update:model-value", value: Address | undefined): void;
  (e: "remove", value: number): void;
}>();

const generalErrorBus = useEventBus<string>("general-error-notification");
const { setFocusedComponent } = useFocus();

const noValidation = (value: string) => "";

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Address>(props.modelValue);
const validateAddressData =
  props.validations.length === 0
    ? noValidation
    : props.validations[0]("Address", props.id + "");
const validateAddressNameData =
  props.validations.length === 0
    ? noValidation
    : props.validations[0]("Names", props.id + "");
const addressError = ref<string | undefined>("");
const nameError = ref<string | undefined>("");
const showDetailsLoading = ref<boolean>(false);

const uniquenessValidation = () => {
  addressError.value = validateAddressData(
    `${selectedValue.streetAddress} ${selectedValue.country.value} ${selectedValue.province.value} ${selectedValue.city} ${selectedValue.city} ${selectedValue.postalCode}`
  );
  nameError.value = validateAddressNameData(selectedValue.locationName);
};

//Watch for changes on the input
watch([selectedValue], () => {
  uniquenessValidation();
  emit("update:model-value", selectedValue);
});

watch(
  () => props.revalidate,
  () => uniquenessValidation(),
  { immediate: true }
);

const updateStateProvince = (
  value: CodeNameType | undefined,
  property: string
) => {
  if (value && (property === "country" || property === "province")) {
    selectedValue[property] = { value: value.code, text: value.name };
  }
};

//Province related data
const provinceUrl = computed(
  () =>
    `/api/clients/activeCountryCodes/${selectedValue.country.value}?page=0&size=250`
);

const resetProvinceOnChange = (receivedCountry: any) => {
  if (selectedValue.country.value !== receivedCountry && receivedCountry)
    selectedValue.province = { value: "", text: "" };
};

const addressControl = ref(false);

//Validations
const validation = reactive<Record<string, boolean>>({
  locationName: props.id == 0,
  streetAddress: false,
  country: false,
  province: false,
  city: false,
  postalCode: false,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

const postalCodeValidators = computed(() => {
  switch (selectedValue.country.value) {
    case "CA":
      return [
        ...getValidations(
          'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")'
        ),
        submissionValidation(`location.addresses[${props.id}].postalCode`),
      ];
    case "US":
      return [
        ...getValidations(
          'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")'
        ),
        submissionValidation(`location.addresses[${props.id}].postalCode`),
      ];
    default:
      return [
        ...getValidations(
          'location.addresses.*.postalCode($.location.addresses.*.country.value !== "CA" && $.location.addresses.*.country.value !== "US")'
        ),
        submissionValidation(`location.addresses[${props.id}].postalCode`),
      ];
  }
});

const postalCodeMask = computed(() => {
  switch (selectedValue.country.value) {
    case "CA":
      return "A#A#A#";
    case "US":
      return ["#####", "#####-####"];
    default:
      return "##########";
  }
});

const postalCodeShowHint = ref(false);

const postalCodePlaceholder = computed(() => {
  if (postalCodeShowHint.value) {
    switch (selectedValue.country.value) {
      case "CA":
        return "Use A1A1A1 format";
      case "US":
        return "Use 00000 or 00000-0000 format";
      default:
        return "Use 00000 format";
    }
  } else {
    return "";
  }
});

const provinceNaming = computed(() => {
  switch (selectedValue.country.value) {
    case "CA":
      return "Province or territory";
    case "US":
      return "State";
    default:
      return "Province/territory or state";
  }
});

const postalCodeNaming = computed(() =>
  selectedValue.country.value === "US" ? "Zip code" : "Postal code"
);

const autoCompleteUrl = computed(
  () =>
    `/api/clients/addresses?country=${
      selectedValue.country.value ?? ""
    }&maxSuggestions=10&searchTerm=${selectedValue.streetAddress ?? ""}`,
);
const autoCompleteResult = ref<BusinessSearchResult | undefined>(
  {} as BusinessSearchResult
);
const detailsData = ref<Address | null>(null);

watch([autoCompleteResult], () => {
  addressControl.value = false;
  if (autoCompleteResult.value && autoCompleteResult.value.code) {
    showDetailsLoading.value = true;
    const { error, loading: detailsLoading } = useFetchTo(
      `/api/clients/addresses/${encodeURIComponent(
        autoCompleteResult.value.code
      )}`,
      detailsData,
      {}
    );

    watch([error], () => {
      // @ts-ignore
      generalErrorBus.emit(error.response?.data.message);
      postalCodeShowHint.value = true;
    });

    watch(
      [detailsLoading],
      () => (showDetailsLoading.value = detailsLoading.value)
    );
  }
});

watch([detailsData], () => {
  if (detailsData.value) {
    selectedValue.streetAddress = detailsData.value.streetAddress.trim();
    selectedValue.city = detailsData.value.city;
    selectedValue.province = detailsData.value.province;
    selectedValue.postalCode = detailsData.value.postalCode.replace(/\s/g, "");
    if (!selectedValue.postalCode) postalCodeShowHint.value = true;
    else postalCodeShowHint.value = false;
    addressControl.value = true;
    //Why is this here? Because Address response differs from the street address value
    //addressControl is responsible for giving an empty list instead of the AC value
    setTimeout(() => (addressControl.value = false), 200);
  }
});

onMounted(() => {
  if (props.id == 0) setFocusedComponent(`addr_${props.id}`, 800);
  else setFocusedComponent(`name_${props.id}`, 200);
});
</script>

<template>
  <div class="frame-01">
  <text-input-component
    :id="'name_' + id"
    label="Location or address name"
    placeholder=""
    tip="For example, 'Campbell River Region' or 'Castlegar Woods Division'"
    v-model="selectedValue.locationName"
    :enabled="true"
    :validations="[
      ...getValidations('location.addresses.*.locationName'),
      submissionValidation(`location.addresses[${id}].locationName`)
    ]"
    :error-message="nameError"
    @empty="validation.locationName = !$event"
    @error="validation.locationName = !$event"
    v-if="id !== 0"
  />

  <dropdown-input-component
    :id="'country_' + id"
    label="Country"
    :initial-value="selectedValue.country.text"
    tip=""
    :enabled="true"
    :model-value="countryList"
    :validations="[...getValidations('location.addresses.*.country.text'),submissionValidation(`location.addresses[${id}].country`)]"
    :error-message="addressError"
    @update:selected-value="updateStateProvince($event, 'country')"
    @update:model-value="resetProvinceOnChange"
    @empty="validation.country = !$event"
  />

  <data-fetcher
    v-model:url="autoCompleteUrl"
    :min-length="3"
    :init-value="[]"
    :init-fetch="false"
    #="{ content, loading, error }"
  >
    <AutoCompleteInputComponent
      :id="'addr_' + id"
      label="Street address or PO box"
      placeholder=""
      tip="Start typing to search for your street address or PO box"
      v-model="selectedValue.streetAddress"
      :contents="addressControl ? [] : content"
      :validations="[
        ...getValidations('location.addresses.*.streetAddress'),
        submissionValidation(`location.addresses[${id}].streetAddress`)
      ]"
      :loading="loading"
      @update:selected-value="autoCompleteResult = $event"
      :error-message="addressError"
      @empty="validation.streetAddress = !$event"
      @error="validation.streetAddress = !$event"
    />
    <cds-inline-loading status="active" v-if="showDetailsLoading">Loading address details...</cds-inline-loading>
  </data-fetcher>

  <text-input-component
    :id="'city_' + id"
    label="City"
    placeholder=""
    v-model="selectedValue.city"
    tip=""
    :enabled="true"
    :error-message="addressError"
    :validations="[
      ...getValidations('location.addresses.*.city'),
      submissionValidation(`location.addresses[${id}].city`)
    ]"
    @empty="validation.city = !$event"
    @error="validation.city = !$event"
  />

  <data-fetcher
    v-model:url="provinceUrl"
    :min-length="0"
    :init-value="[]"
    :init-fetch="true"
    :params="{ method: 'GET' }"
    #="{ content }"
  >
    <dropdown-input-component
      :id="'province_' + id"
      :label="provinceNaming"
      :initial-value="selectedValue.province.text"
      :model-value="content"
      :enabled="true"
      tip=""
      :validations="[...getValidations('location.addresses.*.province.text'),submissionValidation(`location.addresses[${id}].province`)]"
      :error-message="addressError"
      @update:selected-value="updateStateProvince($event, 'province')"
      @empty="validation.province = !$event"
    />
  </data-fetcher>

  <text-input-component
    :id="'postalCode_' + id"
    :label="postalCodeNaming"
    placeholder=""
    :tip="postalCodePlaceholder"
    :enabled="true"
    :error-message="addressError"
    v-model="modelValue.postalCode"
    :mask="postalCodeMask"
    :validations="postalCodeValidators"
    @error="validation.postalCode = !$event"
    @empty="validation.postalCode = !$event"
  />
<div class="grouping-06">
  <cds-button
    v-if="id > 0"
    kind="danger--tertiary"
    @click.prevent="emit('remove', id)"
  >
    <span>Delete address</span>
    <Delete16 slot="icon" />
  </cds-button>
</div>
  </div>
</template>
