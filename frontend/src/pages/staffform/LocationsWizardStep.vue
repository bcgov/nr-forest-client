<script setup lang="ts">
import { watch, ref, computed, reactive, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/accordion/index";
// Components
import FuzzyMatchNotificationGroupingComponent from "@/components/grouping/FuzzyMatchNotificationGroupingComponent.vue";
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
    if (value) Object.assign(formData.location.addresses[index], value);
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

let lastAddressId = formData.location.addresses.length - 1; // The first addressId to be generated minus 1.
const getNewAddressId = () => ++lastAddressId;

//New address being added
const otherAddresses = computed(() => formData.location.addresses.slice(1));

const addAddress = () => {
  const newLength = formData.location.addresses.push(
    indexedEmptyAddress(getNewAddressId())
  );
  const address = formData.location.addresses[newLength - 1];
  setScrollPoint(`address-${address.index}-heading`);
  setFocusedComponent(`address-${address.index}-heading`);
  open[address.index] = true;
  return newLength;
};

//Validation
const validation = reactive<Record<string, boolean>>({
  0: false,
});

const fuzzyNotification = reactive<Record<string, boolean>>({});

const open = reactive<Record<string, boolean>>(
  formData.location.addresses.reduce((accumulator: Record<string, boolean>, current: Address) => {
    const addressId = current.index;
    accumulator[addressId] = true;
    return accumulator;
  }, {}),
);

const locationNames = reactive<Record<string, string>>(
  formData.location.addresses.reduce((accumulator: Record<string, string>, current: Address) => {
    const addressId = current.index;
    accumulator[addressId] = current.locationName;
    return accumulator;
  }, {}),
);

const setFuzzyNotification = (index: number, show: boolean) => {
  const address = formData.location.addresses[index];
  const addressId = address.index;
  fuzzyNotification[addressId] = show;
};

const setFuzzyNotificationByElement = (
  el: InstanceType<typeof FuzzyMatchNotificationGroupingComponent>,
  index: number,
) => {
  if (el) {
    setFuzzyNotification(index, el.fuzzyMatchedError.show);
  }
};

watch(fuzzyNotification, () => {
  const hasFuzzyNotification = Object.values(fuzzyNotification).some((value) => value);
  if (hasFuzzyNotification) {
    formData.location.addresses.forEach((address) => {
      const id = address.index;
      open[id] = fuzzyNotification[id];
    });
  }
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
  const address = formData.location.addresses[index];
  const addressId = address.index;
  if (validation[addressId] !== valid) {
    validation[addressId] = valid;
  }
};

const uniqueValues = isUniqueDescriptive();

const removeAddress = (index: number) => () => {
  const address = formData.location.addresses[index];
  const addressId = address.index;

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
    index + 1, // 1-based index to display in the message
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

const updateLocationName = (locationName: string, index: number) => {
  const address = formData.location.addresses[index];
  const addressId = address.index;
  locationNames[addressId] = locationName;

  // Update the location name on the contacts that are associated with it.
  formData.location.contacts.forEach((contact) => {
    const indexWithinContact = contact.locationNames.findIndex(
      (locationName) => locationName.value === String(addressId),
    );
    if (indexWithinContact !== -1) {
      contact.locationNames[indexWithinContact].text = locationName;
    }
  });
};

const cdsAccordionItemBeingtoggled = (event: any, address: Address) => {
  const id = address.index;
  open[id] = event.detail.open;

  /*
  This somehow prevents some UI issues when there are manual expand/collapse by the user followed
  by automatic expand/collapse or vice-versa.
  */
  if (event.detail.open) {
    event.preventDefault();
  }
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
    <div data-scroll="location-addresses-0" class="header-offset"></div>
    <fuzzy-match-notification-grouping-component
      id="location-addresses-0"
      :ref="(el: any) => setFuzzyNotificationByElement(el, 0)"
    />
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
    @update-location-name="(locationName) => updateLocationName(locationName, 0)"
  />

  <hr />
  <h3>Additional locations</h3>
  <div class="frame-01" v-if="otherAddresses.length > 0" aria-live="off">
    <cds-accordion v-for="(address, index) in otherAddresses" :key="address.index">
      <div
        :data-scroll="`address-${index + 1}-heading`"
        class="header-offset"
      ></div>
      <cds-accordion-item
        :open="open[address.index]"
        :title="locationNames[address.index] || 'Additional location'"
        :data-text="`${address.locationName || 'Additional location'}`"
        size="lg"
        class="grouping-13"
        :data-focus="`address-${index + 1}-heading`"
        @cds-accordion-item-beingtoggled="
          (event: any) => cdsAccordionItemBeingtoggled(event, address)
        "
      >
        <div class="errors-container hide-when-less-than-two-children">
          <!--
          The parent div is necessary to avoid the div.header-offset below from interfering in the flex flow.
          -->
          <div :data-scroll="`location-addresses-${index + 1}`" class="header-offset"></div>
          <fuzzy-match-notification-grouping-component
            :id="`location-addresses-${index + 1}`"
            :ref="(el: any) => setFuzzyNotificationByElement(el, index + 1)"
          />
        </div>
        <staff-location-group-component
          :id="address.index"
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
