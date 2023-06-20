<template>
  <address-group-component
    :id="0"
    v-model="formData.location.addresses[0]"
    :countryList="countryList"
    :validations="[uniqueValues]"
    :revalidate="revalidate"
    @update:model-value="updateAddress($event, 0)"
    @valid="updateValidState(0, $event)"
  />

  <div v-show="otherAddresses.length > 0">
    <h5>Additional address</h5>
    <br />
    <Note note="Provide a name to identify your additional address" />
  </div>

  <address-group-component
    v-for="(address, index) in otherAddresses"
    :key="index + 1"
    :id="index + 1"
    v-bind:model-value="address"
    :countryList="countryList"
    :validations="[uniqueValues]"
    :revalidate="revalidate"
    @update:model-value="updateAddress($event, index + 1)"
    @valid="updateValidState(index + 1, $event)"
    @remove="handleRemove(index + 1)"
  />

  <bx-btn
    kind="tertiary"
    iconLayout=""
    class="bx--btn"
    @click.prevent="addAddress"
    size="field"
  >
    <span>Add another address</span>
    <add16 slot="icon" />
  </bx-btn>
</template>

<script setup lang="ts">
import { watch, ref, computed, reactive, inject } from "vue";
import add16 from "@carbon/icons-vue/es/add/16";
import {
  type FormDataDto,
  type Address,
  emptyAddress,
} from "@/dto/ApplyClientNumberDto";
import { useFetchTo } from "@/services/ForestClientService";

import Note from "@/common/NoteComponent.vue";
import { isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

//Set the prop as a ref, and then emit when it changes
const formData = reactive<FormDataDto>(props.data);
const revalidate = ref(false);
watch([formData], () => emit("update:data", formData));

const updateAddress = (value: Address | undefined, index: number) => {
  revalidate.value = !revalidate.value;
  if (value && index < formData.location.addresses.length)
    formData.location.addresses[index] = value;
  emit("update:data", formData);
};

//Country related data
const countryList = ref([]);

const fetch = () => {
  if (props.active)
    useFetchTo("/api/clients/activeCountryCodes?page=0&size=250", countryList);
};

watch(() => props.active, fetch);
fetch();

const modalContentEvent = inject("modalContent");

//New address being added
const otherAddresses = computed(() => formData.location.addresses.slice(1));
const addAddress = () =>
  formData.location.addresses.push(JSON.parse(JSON.stringify(emptyAddress)));

const removeAddress = (index: number) => () => {
  formData.location.addresses = formData.location.addresses.splice(index, 1);
  modalContentEvent({ active: false });
};

//Validation
const validation = reactive<Record<string, boolean>>({
  0: false,
});

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

const updateValidState = (index: number, valid: boolean) => {
  validation[index] = valid;
};

const uniqueValues = isUniqueDescriptive();

const handleRemove = (index: number) => {
  const selectedAddress = formData.location.addresses[index];
  modalContentEvent({
    name: selectedAddress.locationName || "this",
    kind: "address",
    handler: removeAddress(index),
    active: true,
  });
};
</script>
