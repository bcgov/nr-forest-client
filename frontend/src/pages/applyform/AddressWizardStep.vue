<script setup lang="ts">
import { watch, ref, computed, reactive, onMounted} from 'vue'
import { useEventBus } from '@vueuse/core'
import Add16 from '@carbon/icons-vue/es/add/16'
import {
  type FormDataDto,
  type Address,
  emptyAddress
} from '@/dto/ApplyClientNumberDto'
import { useFetchTo } from '@/composables/useFetch'
import type { ModalNotification } from '@/dto/CommonTypesDto'
import { isUniqueDescriptive } from '@/helpers/validators/GlobalValidators'
import { useFocus } from '@/composables/useFocus'

//Defining the props and emitter to receive the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>()

const emit = defineEmits<{
  (e: 'update:data', value: FormDataDto): void;
  (e: 'valid', value: boolean): void;
}>()

//Defining the event bus to send notifications up
const bus = useEventBus<ModalNotification>('modal-notification');

const { setFocusedComponent } = useFocus();

//Set the prop as a ref, and then emit when it changes
const formData = reactive<FormDataDto>(props.data);
const revalidate = ref(false);
watch([formData], () => emit('update:data', formData));

const updateAddress = (value: Address | undefined, index: number) => {
  if(index < formData.location.addresses.length){
    if (value)
      formData.location.addresses[index] = value;
    else{
      const addressesCopy: Address[] = [...formData.location.addresses];
      addressesCopy.splice(index, 1);
      formData.location.addresses = addressesCopy;
    }
    revalidate.value = !revalidate.value;
  }
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
   if (validation[index] !== valid) {
     validation[index] = valid;
   }
};

const uniqueValues = isUniqueDescriptive();

const removeAddress = (index: number) => () => {
  updateAddress(undefined, index)
  delete validation[index];
  uniqueValues.remove('Address',index+'')
  uniqueValues.remove('Names',index+'')
  bus.emit({ active: false, message: '', kind: '', toastTitle: '', handler: () => {} });
  setFocusedComponent(`addr_addr_${index-1}`,200);
};

const handleRemove = (index: number) => {
  const selectedAddress = formData.location.addresses[index].locationName.length !== 0
    ? formData.location.addresses[index].locationName
    : 'Address #' + index;
  bus.emit({
    message: selectedAddress,
    kind: 'address',
    toastTitle: 'The additional address was deleted',
    handler: removeAddress(index),
    active: true
  });
};

onMounted(() => setFocusedComponent('addr_0',800))
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

<div class="frame-01" v-if="otherAddresses.length > 0">
  
  <div v-for="(address, index) in otherAddresses">
    <hr />
    <div class="grouping-09">
      <span class="heading-03">Additional address</span>
    </div>
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
<p class="body-01 heading-compact-01-dark">
If you’d like to include another address, for example a seed orchard or if your street address is different from your mailing address, select the “Add another address” button below.
</p>
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
