<script setup lang="ts">
import { watch, ref, computed, reactive, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
// Composables
import { useEventBus } from "@vueuse/core";
import { useFocus } from "@/composables/useFocus";
import { useFetchTo } from "@/composables/useFetch";
// Type
import type { FormDataDto, Address } from "@/dto/ApplyClientNumberDto";
import { indexedEmptyAddress } from "@/dto/ApplyClientNumberDto";
import type { ModalNotification } from "@/dto/CommonTypesDto";
import { isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";
import { getAddressDescription } from "@/services/ForestClientService";

// @ts-ignore
import Add16 from "@carbon/icons-vue/es/add/16";

//Defining the props and emitter to receive the data and emit an update
const props = withDefaults(
  defineProps<{ data: FormDataDto; active: boolean; maxLocations?: number }>(),
  { maxLocations: 25 }
);

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

//Defining the event bus to send notifications up
const bus = useEventBus<ModalNotification>("modal-notification");

const { setFocusedComponent, setScrollPoint } = useFocus();

//Set the prop as a ref, and then emit when it changes
const formData = reactive<FormDataDto>(props.data);
const revalidate = ref(false);
watch([formData], () => emit("update:data", formData));

const updateAddress = (value: Address | undefined, index: number) => {
  if (index < formData.location.addresses.length) {
    if (value) formData.location.addresses[index] = value;
    else {
      const addressesCopy: Address[] = [...formData.location.addresses];
      const removedAddressArray = addressesCopy.splice(index, 1);
      formData.location.addresses = addressesCopy;

      // Remove the deleted address from the contacts that are associated to it.
      // (the condition is just a sanity check)
      if (removedAddressArray.length > 0) {
        const removedAddress = removedAddressArray[0];
        formData.location.contacts.forEach((contact) => {
          const indexWithinContact = contact.locationNames.findIndex(
            (locationName) => locationName.text === removedAddress.locationName
          );
          if (indexWithinContact !== -1) {
            contact.locationNames.splice(indexWithinContact, 1);
          }
        });
      }
    }
    revalidate.value = !revalidate.value;
  }
};

//Country related data
const countryList = ref([]);

const fetch = () => {
  if (props.active)
    useFetchTo("/api/codes/countries?page=0&size=250", countryList);
};

watch(() => props.active, fetch);
fetch();

let lastAddressId = -1; // The first addressId to be generated minus 1.
const getNewAddressId = () => ++lastAddressId;

// Associate each address to a unique id, permanent for the lifecycle of this component.
const addressesIdMap = new Map<Address, number>(
  formData.location.addresses.map((address) => {
    const addressId = getNewAddressId(); // Always get a new ID
    address.index = addressId;
    return [address, addressId];
  })
);

//New address being added
const otherAddresses = computed(() => formData.location.addresses.slice(1));
const addAddress = () => {
  const newLength = formData.location.addresses.push(
    indexedEmptyAddress(getNewAddressId())
  );
  const address = formData.location.addresses[newLength - 1];
  addressesIdMap.set(address, address.index);
  setScrollPoint(`address-${address.index}-heading`);
  setFocusedComponent(`address-${address.index}-heading`);
  return newLength;
};

//Validation
const validation = reactive<Record<string, boolean>>({
  0: false,
});

const checkValid = () => {
  const result = Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );
  return result;
};
watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

const updateValidState = (index: number, valid: boolean) => {
  const addressId = addressesIdMap.get(formData.location.addresses[index]);
  if (validation[addressId] !== valid) {
    validation[addressId] = valid;
  }
};

const uniqueValues = isUniqueDescriptive();

const removeAddress = (index: number) => () => {
  const address = formData.location.addresses[index];
  const addressId = addressesIdMap.get(address);
  addressesIdMap.delete(address);

  updateAddress(undefined, index);
  delete validation[addressId];
  uniqueValues.remove("Names", addressId + "");
  bus.emit({
    active: false,
    message: "",
    kind: "",
    toastTitle: "",
    handler: () => {},
  });
};

const getLocationDescription = (address: Address, index: number): string =>
  getAddressDescription(address, index, "Location");

const handleRemove = (index: number) => {
  const selectedAddress = getLocationDescription(
    formData.location.addresses[index],
    index
  );
  bus.emit({
    name: selectedAddress,
    toastTitle: "Success",
    kind: "location",
    message: `“${selectedAddress}” additional location was deleted`,
    handler: removeAddress(index),
    active: true,
  });
};

const removeAdditionalDelivery = (index: number) => () => {
  formData.location.addresses[index].complementaryAddressTwo = null;
  bus.emit({
    active: false,
    message: "",
    kind: "",
    toastTitle: "",
    handler: () => {},
  });
};

const handleRemoveAdditionalDelivery = (index: number) => {
  const selectedDeliveryInformation =
    formData.location.addresses[index].complementaryAddressTwo;
  bus.emit({
    name: selectedDeliveryInformation,
    toastTitle: "Success",
    kind: "delivery information",
    message: `“${selectedDeliveryInformation}” additional delivery information was deleted`,
    handler: removeAdditionalDelivery(index),
    active: true,
  });
};

onMounted(() => setFocusedComponent("focus-1", 0));

const locationNames = ref<string[]>([]);

const updateLocationName = (locationName: string, index: number) => {
  locationNames.value[index] = locationName;
};
</script>

<template>
  <h3 data-focus="address-0-heading" tabindex="-1">
    <div data-scroll="address-0-heading" class="header-offset"></div>
    Primary location
  </h3>
  <cds-inline-notification
    v-shadow="2"
    low-contrast="true"
    open="true"
    kind="info"
    hide-close-button="true"
    title="">
    <p class="cds--inline-notification-content">
      <strong>Primary location: </strong>
      This is the address where the client receives mail.
    </p>
  </cds-inline-notification>
  <div class="errors-container hide-when-less-than-two-children">
    <!--
    The parent div is necessary to avoid the div.header-offset below from interfering in the flex flow.
    -->
    <div data-scroll="location.addresses[0]" class="header-offset"></div>
    <fuzzy-match-notification-grouping-component id="location.addresses[0]" />
  </div>
  <staff-location-group-component
    :id="0"
    v-model="formData.location.addresses[0]"
    :countryList="countryList"
    :validations="[uniqueValues.add]"
    :revalidate="revalidate"
    @update:model-value="updateAddress($event, 0)"
    @valid="updateValidState(0, $event)"
    @remove-additional-delivery="handleRemoveAdditionalDelivery(0)"
  />

  <hr />
  <h3>Additional locations</h3>
  <div class="frame-01" v-if="otherAddresses.length > 0" aria-live="off">
    <cds-accordion v-for="(address, index) in otherAddresses" :key="addressesIdMap.get(address)">
      <div :data-scroll="`address-${index + 1}-heading`" class="header-offset"></div>
      <cds-accordion-item
        open
        :title="locationNames[index + 1] || 'Additional location'"
        size="lg"
        class="grouping-13"
        :data-focus="`address-${index + 1}-heading`"
      >
        <div class="errors-container hide-when-less-than-two-children">
          <!--
          The parent div is necessary to avoid the div.header-offset below from interfering in the flex flow.
          -->
          <div :data-scroll="`location.addresses[${index + 1}]`" class="header-offset"></div>
          <fuzzy-match-notification-grouping-component :id="`location.addresses[${index + 1}]`" />
        </div>
        <staff-location-group-component
          :id="addressesIdMap.get(address)"
          v-bind:model-value="address"
          :countryList="countryList"
          :validations="[uniqueValues.add]"
          :revalidate="revalidate"
          @update:model-value="updateAddress($event, index + 1)"
          @valid="updateValidState(index + 1, $event)"
          @remove="handleRemove(index + 1)"
          @remove-additional-delivery="handleRemoveAdditionalDelivery(index + 1)"
          @update-location-name="(locationName) => updateLocationName(locationName, index + 1)"
        />
      </cds-accordion-item>
    </cds-accordion>
  </div>
  <div class="grouping-02">
    <p class="body-02 light-theme-text-text-primary">
      {{
        formData.location.addresses.length < props.maxLocations
          ? "If the business operates in more than one location you can include additional addresses."
          : `You can only add a maximum of ${props.maxLocations} locations.`
      }}
    </p>
  </div>
  <cds-button
    kind="tertiary"
    @click.prevent="addAddress"
    v-if="formData.location.addresses.length < props.maxLocations"
  >
    <span>Add another location</span>
    <Add16 slot="icon" />
  </cds-button>
  <hr />
</template>
