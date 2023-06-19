<script setup lang="ts">
import { reactive, watch, computed } from "vue";
import type { CodeNameType } from "@/core/CommonTypes";
import type { Address } from "@/dto/ApplyClientNumberDto";
import { isNotEmpty,isCanadianPostalCode,isUsZipCode,isOnlyNumbers,isMaxSize,isMinSize } from "@/helpers/validators/GlobalValidators";

//Define the input properties for this component
const props = defineProps<{
  id: number;
  modelValue: Address;
  countryList: Array<CodeNameType>;
  validations: Array<Function>;
}>();

//Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "valid", value: boolean): void;
  (e: "update:modelValue", value: Address | undefined): void;
}>();

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Address>(props.modelValue);

//Watch for changes on the input
watch([selectedValue], () => emit("update:modelValue", selectedValue));

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

const resetProvinceOnChange = (receivedCountry: any) =>
  selectedValue.country.value !== receivedCountry &&
  (selectedValue.province = { value: "", text: "" });

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
    return [isCanadianPostalCode];
  case "US":
    return [isUsZipCode];
  default:
    return [isOnlyNumbers, isMaxSize(10)];
}
});

</script>

<template>
  <text-input-component
    id="name"
    label="Location or address name"
    placeholder="Kamloops office"
    tip="For example, 'Campbell River Region' or 'Castlegar Woods Division'"
    v-model="selectedValue.locationName"
    :enabled="true"
    :validations="[isNotEmpty,isMinSize(3),isMaxSize(50)]"
    @empty="validation.locationName = !$event"
    v-if="props.id !== 0"
  />

  <dropdown-input-component
    id="country"
    label="Country"
    :initial-value="selectedValue.country.value"
    :model-value="countryList"
    :validations="[]"
    @update:selected-value="updateStateProvince($event, 'country')"
    @update:model-value="resetProvinceOnChange"
    @empty="validation.country = !$event"
  />

  <text-input-component
    id="addr"
    label="Street address or PO box"
    placeholder="Start typing to search for your address or PO box"
    v-model="selectedValue.streetAddress"
    :enabled="true"
    :validations="[isNotEmpty,isMinSize(3),isMaxSize(50)]"
    @empty="validation.streetAddress = !$event"
  />

  <text-input-component
    id="city"
    label="City"
    placeholder="City"
    v-model="selectedValue.city"
    :enabled="true"
    :validations="[isNotEmpty,isMinSize(3),isMaxSize(50)]"
    @empty="validation.city = !$event"
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
      id="province"
      label="Province or territory"
      :initial-value="selectedValue.province.value"
      :model-value="content"
      :validations="[]"
      @update:selected-value="updateStateProvince($event, 'province')"
      @empty="validation.province = !$event"
    />
  </data-fetcher>

  <text-input-component
    id="postalCode"
    label="Postal code"
    placeholder="A1A1A1"
    :enabled="true"
    v-model="modelValue.postalCode"
    :validations="postalCodeValidators"
    @empty="validation.postalCode = !$event"
  />
</template>
