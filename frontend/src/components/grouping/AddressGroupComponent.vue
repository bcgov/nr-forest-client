<script setup lang="ts">
import { reactive, watch, computed, ref } from "vue";
import delete16 from "@carbon/icons-vue/es/trash-can/16";
import type { CodeNameType } from "@/core/CommonTypes";
import type { Address } from "@/dto/ApplyClientNumberDto";
import { isNotEmpty,isCanadianPostalCode,isUsZipCode,isOnlyNumbers,isMaxSize,isMinSize, isNoSpecialCharacters } from "@/helpers/validators/GlobalValidators";

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

//We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Address>(props.modelValue);
const validateAddressData = props.validations[0]('Address',props.id+'');
const validateAddressNameData = props.validations[0]('Names',props.id+'');
const addressError = ref<string | undefined>("");
const nameError = ref<string | undefined>("");

//Watch for changes on the input
watch([selectedValue], () => {
  addressError.value = validateAddressData(`${selectedValue.streetAddress} ${selectedValue.country.value} ${selectedValue.province.value} ${selectedValue.city} ${selectedValue.city} ${selectedValue.postalCode}`);
  nameError.value = validateAddressNameData(selectedValue.locationName);
  emit("update:model-value", selectedValue);
});

watch(() => props.revalidate,() =>{
  addressError.value = validateAddressData(`${selectedValue.streetAddress} ${selectedValue.country.value} ${selectedValue.province.value} ${selectedValue.city} ${selectedValue.city} ${selectedValue.postalCode}`);
  nameError.value = validateAddressNameData(selectedValue.locationName);
});

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
    return [isOnlyNumbers,isMinSize(5), isMaxSize(10)];
}
});

const provinceNaming = computed(() =>{
  switch(selectedValue.country.value){
    case "CA":
      return "Province or territory"
    case "US":
      return "State"
    default:
      return "Province/terrytory or state"
  }
});

const postalCodeNaming = computed(() =>(
  selectedValue.country.value === "US" ? "Zip code" : "Postal code"));

</script>

<template>
  <text-input-component
    id="name"
    label="Location or address name"
    placeholder="Kamloops office"
    tip="For example, 'Campbell River Region' or 'Castlegar Woods Division'"
    v-model="selectedValue.locationName"
    :enabled="true"
    :validations="[isNotEmpty,isMinSize(3),isMaxSize(50),isNoSpecialCharacters]"
    :error-message="nameError"
    @empty="validation.locationName = !$event"
    v-if="props.id !== 0"
  />

  <dropdown-input-component
    id="country"
    label="Country"
    :initial-value="selectedValue.country.value"
    :model-value="countryList"
    :validations="[]"
    :error-message="addressError"
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
    :error-message="addressError"
    :validations="[isNotEmpty,isMinSize(5),isMaxSize(50)]"
    @empty="validation.streetAddress = !$event"
  />

  <text-input-component
    id="city"
    label="City"
    placeholder="City"
    v-model="selectedValue.city"
    :enabled="true"
    :error-message="addressError"
    :validations="[isNotEmpty,isMinSize(3),isMaxSize(50),isNoSpecialCharacter]"
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
      :label="provinceNaming"
      :initial-value="selectedValue.province.value"
      :model-value="content"
      :validations="[]"
      :error-message="addressError"
      @update:selected-value="updateStateProvince($event, 'province')"
      @empty="validation.province = !$event"
    />
  </data-fetcher>

  <text-input-component
    id="postalCode"
    :label="postalCodeNaming"
    placeholder="A1A1A1"
    :enabled="true"
    :error-message="addressError"
    v-model="modelValue.postalCode"
    :validations="postalCodeValidators"
    @empty="validation.postalCode = !$event"
  />

  <bx-btn    
    v-show="id > 0"
    kind="danger-ghost"
    iconLayout=""
    class="bx--btn"    
    @click.prevent="emit('remove',id)"
    size="field"
  >
  <span>Delete address</span>
  <delete16 slot="icon"/>
  </bx-btn>
</template>
