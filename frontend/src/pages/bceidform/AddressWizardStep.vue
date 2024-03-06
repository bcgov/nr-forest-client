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
import { emptyAddress } from "@/dto/ApplyClientNumberDto";
import type { ModalNotification } from "@/dto/CommonTypesDto";
import { isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";

// @ts-ignore
import Add16 from "@carbon/icons-vue/es/add/16";

//Defining the props and emitter to receive the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

//Defining the event bus to send notifications up
const bus = useEventBus<ModalNotification>("modal-notification");

const { safeSetFocusedComponent } = useFocus();

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
    useFetchTo("/api/countries?page=0&size=250", countryList);
};

watch(() => props.active, fetch);
fetch();

let lastAddressId = -1; // The first addressId to be generated minus 1.
const getNewAddressId = () => ++lastAddressId;

// Associate each address to a unique id, permanent for the lifecycle of this component.
const addressesIdMap = new Map<Address, number>(
  formData.location.addresses.map((address) => [address, getNewAddressId()]),
);

//New address being added
const otherAddresses = computed(() => formData.location.addresses.slice(1));
const addAddress = () => {
  const newLength = formData.location.addresses.push(emptyAddress());
  const address = formData.location.addresses[newLength - 1];
  addressesIdMap.set(address, getNewAddressId());
  const focusIndex = newLength - 1;
  safeSetFocusedComponent(`name_${focusIndex}`);
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
  uniqueValues.remove("Address", addressId + "");
  uniqueValues.remove("Names", addressId + "");
  bus.emit({
    active: false,
    message: "",
    kind: "",
    toastTitle: "",
    handler: () => {},
  });
};

const handleRemove = (index: number) => {
  const selectedAddress =
    formData.location.addresses[index].locationName.length !== 0
      ? formData.location.addresses[index].locationName
      : "Address #" + index;
  bus.emit({
    name: selectedAddress,
    toastTitle: "Success",
    kind: "address",
    message: `“${selectedAddress}” additional address was deleted`,
    handler: removeAddress(index),
    active: true,
  });
};

onMounted(() => safeSetFocusedComponent("addr_0", 800));
</script>

<template>
  <address-group-component
    :id="0"
    v-model="formData.location.addresses[0]"
    :countryList="countryList"
    :validations="[uniqueValues.add]"
    :revalidate="revalidate"
    @update:model-value="updateAddress($event, 0)"
    @valid="updateValidState(0, $event)"
  />

  <template v-if="$features.BCEID_MULTI_ADDRESS">
    <div class="frame-01" v-if="otherAddresses.length > 0">
      <div v-for="(address, index) in otherAddresses">
        <hr />
        <div class="grouping-09">
          <h3>Additional address</h3>
        </div>
        <address-group-component
          :key="addressesIdMap.get(address)"
          :id="addressesIdMap.get(address)"
          v-bind:model-value="address"
          :countryList="countryList"
          :validations="[uniqueValues.add]"
          :revalidate="revalidate"
          @update:model-value="updateAddress($event, index + 1)"
          @valid="updateValidState(index + 1, $event)"
          @remove="handleRemove(index + 1)"
        />
      </div>
    </div>
    <div class="grouping-02">
      <p class="body-02 light-theme-text-text-primary">
        If the business operates in more than one location you can include additional addresses.
      </p>
    </div>
    <cds-button
      kind="tertiary"
      @click.prevent="addAddress"
      v-if="formData.location.addresses.length < 5"
    >
      <span>Add another address</span>
      <Add16 slot="icon" />
    </cds-button>
  </template>
</template>
