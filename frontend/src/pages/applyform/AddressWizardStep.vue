<template>
  <h4>Address</h4>
  <br />
  <h5>Mailing address</h5>
  <Note note="This is the primary address where you'll receive mail." />
  <br /><br />
  <Note
    note="If you'd like another location, for example a seed orchard or if your street"
  />
  <Note
    note="address is different from your mailing address, please select the ”+ Add"
  />
  <Note note="another address” button below." />
  <br /><br /><br />

  <address-group-component
    :id="0"
    v-model="formData.location.addresses[0]"
    :countryList="countryList"
    :validations="[]"
    @update:model-value="updateAddress($event, 0)"
    @valid="updateValidState(0, $event)"
  />

  <br /><br />

  <div v-show="otherAddresses.length > 0">
    <h5>Additional address</h5>
    <br />
    <Note note="Provide a name to identify your additional address" />
    <br /><br /><br />
  </div>

  <address-group-component
    v-for="(address, index) in otherAddresses"
    :key="index + 1"
    :id="index + 1"
    v-bind:model-value="address"
    :countryList="countryList"
    :validations="[]"
    @update:model-value="updateAddress($event, index + 1)"
    @valid="updateValidState(index + 1, $event)"
  />

  <div v-show="otherAddresses.length > 0"><br /><br /><br /></div>

  <bx-btn kind="tertiary" icon-layout="regular" @click="addAddress"
    >Add another address +</bx-btn
  >
</template>

<script setup lang="ts">
import { watch, ref, computed, reactive } from "vue";

import {
  type FormDataDto,
  type Address,
  emptyAddress,
} from "@/dto/ApplyClientNumberDto";
import { useFetchTo } from "@/services/ForestClientService";

import Note from "@/common/NoteComponent.vue";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>();

const emit = defineEmits<{
  (e: "update:data", value: FormDataDto): void;
  (e: "valid", value: boolean): void;
}>();

//Set the prop as a ref, and then emit when it changes
const formData = reactive<FormDataDto>(props.data);
watch([formData], () => emit("update:data", formData));

const updateAddress = (value: Address | undefined, index: number) => {
  if (value && index < formData.location.addresses.length)
    formData.location.addresses[index] = value;
};

//Country related data
const countryList = ref([]);

const fetch = () => {
  if (props.active)
    useFetchTo("/api/clients/activeCountryCodes?page=0&size=250", countryList);
};

watch(() => props.active, fetch);
fetch();

//New address being added
const otherAddresses = computed(() => formData.location.addresses.slice(1));
const addAddress = () =>
  formData.location.addresses.push(JSON.parse(JSON.stringify(emptyAddress)));

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
</script>
