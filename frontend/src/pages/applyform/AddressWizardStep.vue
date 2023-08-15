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

<hr v-if="otherAddresses.length > 0"/>

<div class="frame-01">
  <div v-if="otherAddresses.length > 0" class="grouping-01">
    <span class="heading-03">Additional address</span>
    <span class="body-01">Provide a name to identify your additional address</span>
  </div>

  <div v-for="(address, index) in otherAddresses">
    <hr v-if="index > 0"/>
    <address-group-component
      :key="index + 1"
      :id="index + 1"
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
<bx-btn
    kind="tertiary"
    iconLayout=""
    class="bx--btn"
    @click.prevent="addAddress"
    size="field"
  >
    <span>Add another address</span>
    <Add16 slot="icon" />
  </bx-btn>
</template>

<script setup lang="ts">
import { watch, ref, computed, reactive} from 'vue'
import { useEventBus } from '@vueuse/core'
import Add16 from '@carbon/icons-vue/es/add/16'
import {
  type FormDataDto,
  type Address,
  emptyAddress
} from '@/dto/ApplyClientNumberDto'
import { useFetchTo } from '@/composables/useFetch'
import type { ModalNotification } from '@/dto/CommonTypesDto'

import Note from '@/components/NoteComponent.vue'
import { isUniqueDescriptive } from '@/helpers/validators/GlobalValidators'

//Defining the props and emitter to receive the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>()

const emit = defineEmits<{
  (e: 'update:data', value: FormDataDto): void;
  (e: 'valid', value: boolean): void;
}>()

//Defining the event bus to send notifications up
const bus = useEventBus<ModalNotification>('modal-notification');

//Set the prop as a ref, and then emit when it changes
const formData = reactive<FormDataDto>(props.data);
const revalidate = ref(false);
watch([formData], () => emit('update:data', formData));

const updateAddress = (value: Address | undefined, index: number) => {
  revalidate.value = !revalidate.value;
  if (value && index < formData.location.addresses.length)
    formData.location.addresses[index] = value;
  emit('update:data', formData);
};

//Country related data
const countryList = ref([]);

const fetch = () => {
  if (props.active)
    useFetchTo('/api/clients/activeCountryCodes?page=0&size=250', countryList);
};

watch(() => props.active, fetch);
fetch();

//New address being added
const otherAddresses = computed(() => formData.location.addresses.slice(1));
const addAddress = () => formData.location.addresses.push(emptyAddress());



//Validation
const validation = reactive<Record<string, boolean>>({
  0: false
});

const checkValid = () =>{
  
  const result = Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );
  return result;
}
watch([validation], () => emit('valid', checkValid()));
emit('valid', false);

const updateValidState = (index: number, valid: boolean) => {
  validation[index] = valid;
};

const uniqueValues = isUniqueDescriptive();

const removeAddress = (index: number) => () => {
  const addressesCopy: Address[] = [...formData.location.addresses];
  addressesCopy.splice(index, 1);
  formData.location.addresses = addressesCopy;
 
  delete validation[index];
  uniqueValues.remove('Address',index+'')
  uniqueValues.remove('Names',index+'')
  revalidate.value = !revalidate.value;
  emit('valid', checkValid())

  bus.emit({ active: false, message: '', kind: '', handler: () => {} });

 
};

const handleRemove = (index: number) => {
  const selectedAddress = formData.location.addresses[index].locationName.length !== 0
    ? formData.location.addresses[index].locationName
    : 'Address #' + index;
  bus.emit({
    message: selectedAddress,
    kind: 'address',
    handler: removeAddress(index),
    active: true
  });
};
</script>
