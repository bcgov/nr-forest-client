<script setup lang="ts">
import { reactive, watch, computed, ref } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/tooltip/index";
// Composables
import { useFetchTo } from "@/composables/useFetch";
// Types
import type { CodeNameType, BusinessSearchResult } from "@/dto/CommonTypesDto";
import type { Address } from "@/dto/ApplyClientNumberDto";
// Validators
import { getValidations } from "@/helpers/validators/StaffFormValidations";
import { submissionValidation } from "@/helpers/validators/SubmissionValidators";
import Delete16 from "@carbon/icons-vue/es/trash-can/16";
import Add16 from "@carbon/icons-vue/es/add/16";
import Information16 from "@carbon/icons-vue/es/information/16";
import { getAddressDescription, indexToLocationCode } from "@/services/ForestClientService";

// Define the input properties for this component
const props = defineProps<{
  id: number;
  modelValue: Address;
  countryList: Array<CodeNameType>;
  validations: Array<Function>;
  revalidate?: boolean;
  hideDeleteButton?: boolean;
}>();

// Events we emit during component lifecycle
const emit = defineEmits<{
  (e: "valid", value: boolean): void;
  (e: "update:model-value", value: Address | undefined): void;
  (e: "remove", id: number): void;
  (e: "removeAdditionalDelivery", id: number): void;
  (e: "updateLocationName", value: string): void;
}>();

const noValidation = (value: string) => "";

// We set it as a separated ref due to props not being updatable
const selectedValue = reactive<Address>(props.modelValue);
const validateAddressNameData =
  props.validations.length === 0 ? noValidation : props.validations[0]("Names", indexToLocationCode(props.modelValue.index));
const nameError = ref<string | undefined>("");
const showDetailsLoading = ref<boolean>(false);

const uniquenessValidation = () => {
  nameError.value = validateAddressNameData(selectedValue.locationName ?? "");
};

const additionalDeliveryVisible = computed(() => selectedValue.complementaryAddressTwo !== null);

const showAdditionalDelivery = () => {
  selectedValue.complementaryAddressTwo = "";
};

const deliveryInformationError = computed(() =>
  additionalDeliveryVisible.value && !selectedValue.complementaryAddressOne
    ? "Delivery information cannot be empty when Additional delivery information is displayed"
    : "",
);

// Watch for changes on the input
watch([selectedValue], () => {
  uniquenessValidation();
  emit("update:model-value", selectedValue);
});

watch(
  () => props.revalidate,
  () => uniquenessValidation(),
  { immediate: true },
);

const updateStateProvince = (value: CodeNameType | undefined, property: string) => {
  if (value && (property === "country" || property === "province")) {
    selectedValue[property] = { value: value.code, text: value.name };
  }
};

// Province related data
const provinceUrl = computed(
  () => `/api/codes/countries/${selectedValue.country.value}/provinces?page=0&size=250`,
);

const resetProvinceOnChange = (receivedCountry: any) => {
  if (selectedValue.country.value !== receivedCountry && receivedCountry)
    selectedValue.province = { value: "", text: "" };
};

const addressControl = ref(false);

// Validations
const validation = reactive<Record<string, boolean>>({
  locationName: false,
  complementaryAddressOne: true,
  complementaryAddressTwo: true,
  streetAddress: false,
  country: false,
  province: false,
  city: false,
  postalCode: false,
  faxNumber: true,
  notes: true,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) => accumulator && currentValue,
    true,
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

const postalCodeValidators = computed(() => {
  switch (selectedValue.country.value) {
    case "CA":
      return [
        ...getValidations(
          'location.addresses.*.postalCode($.location.addresses.*.country.value === "CA")',
        ),
        submissionValidation(`location.addresses[${props.id}].postalCode`),
      ];
    case "US":
      return [
        ...getValidations(
          'location.addresses.*.postalCode($.location.addresses.*.country.value === "US")',
        ),
        submissionValidation(`location.addresses[${props.id}].postalCode`),
      ];
    default:
      return [
        ...getValidations(
          'location.addresses.*.postalCode($.location.addresses.*.country.value !== "CA" && $.location.addresses.*.country.value !== "US")',
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

const postalCodeNumeric = computed<boolean>(() => selectedValue.country.value !== "CA");

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
  selectedValue.country.value === "US" ? "Zip code" : "Postal code",
);

const autoCompleteUrl = computed(
  () =>
    `/api/addresses?country=${
      selectedValue.country.value ?? ""
    }&maxSuggestions=10&searchTerm=${selectedValue.streetAddress ?? ""}`,
);
const autoCompleteResult = ref<BusinessSearchResult | undefined>({} as BusinessSearchResult);
const detailsData = ref<Address | null>(null);

watch([autoCompleteResult], () => {
  addressControl.value = false;
  if (autoCompleteResult.value && autoCompleteResult.value.code) {
    showDetailsLoading.value = true;
    const { error, loading: detailsLoading } = useFetchTo(
      `/api/addresses/${encodeURIComponent(autoCompleteResult.value.code)}`,
      detailsData,
      {},
    );

    watch([error], () => {
      postalCodeShowHint.value = true;
    });

    watch([detailsLoading], () => (showDetailsLoading.value = detailsLoading.value));
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
    // Why is this here? Because Address response differs from the street address value
    // addressControl is responsible for giving an empty list instead of the AC value
    setTimeout(() => (addressControl.value = false), 200);
  }
});

const updateLocationName = (event: FocusEvent) => emit("updateLocationName", event.target.value);

const getLocationDescription = (address: Address, index: number): string =>
  getAddressDescription(address, index, "Location");
</script>

<template>
  <div class="frame-01">
    <text-input-component
      :id="'name_' + id"
      label="Location name"
      required
      required-label
      placeholder=""
      tip='For example, "Headquarters", "Campbell River Region" or "Castlegar Woods Division"'
      v-model="selectedValue.locationName"
      :enabled="true"
      :validations="[
        ...getValidations('location.addresses.*.locationName'),
        submissionValidation(`location.addresses[${id}].locationName`),
      ]"
      :error-message="nameError"
      @empty="validation.locationName = !$event"
      @error="validation.locationName = !$event"
      @blur="updateLocationName"
    />

    <text-input-component
      :id="'complementaryAddressOne_' + id"
      label="Delivery information"
      placeholder=""
      tip="This is where 'care of' and similar information can be included"
      v-model="selectedValue.complementaryAddressOne"
      :enabled="true"
      :validations="[
        ...getValidations('location.addresses.*.complementaryAddressOne'),
        submissionValidation(`location.addresses[${id}].complementaryAddressOne`),
      ]"
      :error-message="deliveryInformationError"
      @empty="validation.complementaryAddressOne = !additionalDeliveryVisible || !$event"
      @error="validation.complementaryAddressOne = !$event"
    />

    <cds-button
      id="add-delivery-btn"
      kind="tertiary"
      @click.prevent="showAdditionalDelivery"
      v-if="!additionalDeliveryVisible"
    >
      <span>Add more delivery information</span>
      <Add16 slot="icon" />
    </cds-button>

    <div v-if="additionalDeliveryVisible" class="input-with-instruction">
      <div class="horizontal-input-grouping">
        <text-input-component
          :id="'complementaryAddressTwo_' + id"
          label="Additional delivery information"
          placeholder=""
          tip=""
          v-model="selectedValue.complementaryAddressTwo"
          :enabled="true"
          :validations="[
            ...getValidations('location.addresses.*.complementaryAddressTwo'),
            submissionValidation(`location.addresses[${id}].complementaryAddressTwo`),
          ]"
          @empty="validation.complementaryAddressTwo = true"
          @error="validation.complementaryAddressTwo = !$event"
        />
        <cds-button
          :id="'deleteAdditionalDeliveryInformation_' + id"
          :danger-descriptor="`Delete additional delivery information &quot;${selectedValue.complementaryAddressTwo || ''}&quot; from &quot;${getLocationDescription(
            selectedValue,
            id,
          )}&quot;`"
          kind="danger--tertiary"
          size="md"
          @click.prevent="emit('removeAdditionalDelivery', id)"
        >
          <Delete16 slot="icon" />
        </cds-button>
      </div>
      <p class="body-compact-01">You can only add a maximum of 2 lines of delivery information.</p>
    </div>

    <data-fetcher
      v-model:url="autoCompleteUrl"
      :min-length="3"
      :init-value="[]"
      :init-fetch="false"
      :disabled="!selectedValue.streetAddress"
      #="{ content, loading, error }"
    >
      <AutoCompleteInputComponent
        :id="'addr_' + id"
        label="Street address or PO box"
        autocomplete="off"
        required
        required-label
        placeholder=""
        tip=""
        v-model="selectedValue.streetAddress"
        :contents="addressControl ? [] : content"
        :validations="[
          ...getValidations('location.addresses.*.streetAddress'),
          submissionValidation(`location.addresses[${id}].streetAddress`),
        ]"
        :loading="loading"
        @update:selected-value="autoCompleteResult = $event"
        @empty="validation.streetAddress = !$event"
        @error="validation.streetAddress = !$event"
      />
      <cds-inline-loading status="active" v-if="showDetailsLoading"
        >Loading address details...</cds-inline-loading
      >
    </data-fetcher>

    <text-input-component
      :id="'city_' + id"
      label="City"
      autocomplete="off"
      required
      required-label
      placeholder=""
      v-model="selectedValue.city"
      tip=""
      :enabled="true"
      :validations="[
        ...getValidations('location.addresses.*.city'),
        submissionValidation(`location.addresses[${id}].city`),
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
      <combo-box-input-component
        :id="'province_' + id"
        :label="provinceNaming"
        autocomplete="off"
        required
        required-label
        :initial-value="content?.find((item) => item.code === selectedValue.province.value)?.name"
        :model-value="content"
        :enabled="true"
        tip=""
        :validations="[
          ...getValidations('location.addresses.*.province.text'),
          submissionValidation(`location.addresses[${id}].province`),
        ]"
        @update:selected-value="updateStateProvince($event, 'province')"
        @empty="validation.province = !$event"
      />
    </data-fetcher>

    <combo-box-input-component
      :id="'country_' + id"
      label="Country"
      autocomplete="off"
      required
      required-label
      :initial-value="countryList?.find((item) => item.code === selectedValue.country.value)?.name"
      tip=""
      :enabled="true"
      :model-value="countryList"
      :validations="[
        ...getValidations('location.addresses.*.country.text'),
        submissionValidation(`location.addresses[${id}].country`),
      ]"
      @update:selected-value="updateStateProvince($event, 'country')"
      @update:model-value="resetProvinceOnChange"
      @empty="validation.country = !$event"
    />

    <text-input-component
      :id="'postalCode_' + id"
      :label="postalCodeNaming"
      autocomplete="off"
      :numeric="postalCodeNumeric"
      required
      required-label
      placeholder=""
      :tip="postalCodePlaceholder"
      :enabled="true"
      v-model="modelValue.postalCode"
      :mask="postalCodeMask"
      :validations="postalCodeValidators"
      @error="validation.postalCode = !$event"
      @empty="validation.postalCode = !$event"
    />

    <textarea-input-component
      :id="'notes_' + id"
      label="Notes"
      enable-counter
      :max-count="4000"
      :rows="7"
      placeholder=""
      v-model="selectedValue.notes"
      :enabled="true"
      :validations="[
        ...getValidations('location.addresses.*.notes'),
        submissionValidation(`location.addresses[${id}].notes`),
      ]"
      @empty="validation.notes = true"
      @error="validation.notes = !$event"
    >
      <div slot="label-text" class="label-with-icon line-height-0">
        <div class="cds-text-input-label">
          <span>Notes</span>
        </div>
        <cds-tooltip>
          <Information16 />
          <cds-tooltip-content>
            For example, any information about the client location
          </cds-tooltip-content>
        </cds-tooltip>
      </div>
    </textarea-input-component>

    <div class="grouping-06" v-if="!hideDeleteButton && Number(id) > 0">
      <cds-button
        :id="'deleteAddress_' + id"
        :danger-descriptor="`Delete location &quot;${getLocationDescription(
          selectedValue,
          id,
        )}&quot;`"
        kind="danger--tertiary"
        @click.prevent="emit('remove', id)"
      >
        <span>Delete location</span>
        <Delete16 slot="icon" />
      </cds-button>
    </div>
  </div>
</template>
