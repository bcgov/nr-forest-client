<script setup lang="ts">
import { watch, ref, computed, reactive, onMounted } from 'vue'
import { useEventBus } from '@vueuse/core'
import Add16 from '@carbon/icons-vue/es/add/16'

import {
  type FormDataDto,
  type Contact,
  emptyContact
} from '@/dto/ApplyClientNumberDto'
import { useFetchTo } from '@/composables/useFetch'
import useFocus from '@/composables/useFocus'
import type { CodeNameType, ModalNotification } from '@/dto/CommonTypesDto'
import { isUniqueDescriptive } from '@/helpers/validators/GlobalValidators'


//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>()

const emit = defineEmits<{
  (e: 'update:data', value: FormDataDto): void
  (e: 'valid', value: boolean): void
}>()

//Defining the event bus to send notifications up
const bus = useEventBus<ModalNotification>('modal-notification')

const { setFocusedComponent } = useFocus();

//Set the prop as a ref, and then emit when it changes
const formData = reactive<FormDataDto>(props.data)
const revalidate = ref(false)
watch([formData], () => emit('update:data', formData))

const updateContact = (value: Contact | undefined, index: number) => {
  if (index < formData.location.contacts.length){
    if(value)
      formData.location.contacts[index] = value
    else{
      const contactCopy: Contact[] = [...formData.location.contacts];
      contactCopy.splice(index, 1);
      formData.location.contacts = contactCopy;
    }
  }
  revalidate.value = !revalidate.value
}

//Role related data
const roleList = ref([])
const fetch = () => {
  if (props.active)
    useFetchTo('/api/clients/activeContactTypeCodes?page=0&size=250', roleList)
}

watch(() => props.active, fetch)
fetch()

//Addresses Related data
const addresses = computed<CodeNameType[]>(() =>
  formData.location.addresses.map((address, index) => {
    return { code: index + '', name: address.locationName } as CodeNameType
  })
)

const uniqueValues = isUniqueDescriptive()

//New contact being added
const otherContacts = computed(() => formData.location.contacts.slice(1))
const addContact = () =>
  formData.location.contacts.push(JSON.parse(JSON.stringify(emptyContact)))

const removeContact = (index: number) => () => {
    updateContact(undefined, index)
    delete validation[index]
    uniqueValues.remove('Name',index+'')
    bus.emit({ active: false, message: '', kind: '', handler: () => {} })
}

//Validation
const validation = reactive<Record<string, boolean>>({
  0: false
})

const updateValidState = (index: number, valid: boolean) => {
  if (validation[index] !== valid) {
     validation[index] = valid;
   }
}

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  )

watch([validation], () => emit('valid', checkValid()))
emit('valid', false)

const handleRemove = (index: number) => {
  const selectedContact = formData.location.contacts[index]
  bus.emit({
    message: selectedContact.firstName || 'this',
    kind: 'Contact deleted',
    handler: removeContact(index),
    active: true
  })
}

onMounted(() => setFocusedComponent('address_0'))
</script>

<template>
  <contact-group-component
    :id="0"
    v-model="formData.location.contacts[0]"
    :roleList="roleList"
    :addressList="addresses"
    :validations="[uniqueValues.add]"
    :revalidate="revalidate"
    :enabled="false"
    @update:model-value="updateContact($event, 0)"
    @valid="updateValidState(0, $event)"
  />

  <hr v-if="otherContacts.length > 0"/>

  <div class="frame-01" v-if="otherContacts.length > 0">
    <div class="grouping-01">
      <span class="heading-03">Additional contact</span>
    </div>
    <div  v-for="(contact, index) in otherContacts">
      <hr v-if="index > 0"/>
    <contact-group-component
    
      :key="index + 1"
      :id="index + 1"
      v-bind:modelValue="contact"
      :roleList="roleList"
      :addressList="addresses"
      :validations="[uniqueValues.add]"
      :enabled="true"
      :revalidate="revalidate"
      @update:model-value="updateContact($event, index + 1)"
      @valid="updateValidState(index + 1, $event)"
      @remove="handleRemove(index + 1)"
    />
    </div>
  </div>

  <span class="body-01">
              To add another contact to the account, select "Add another
              contact" button below.</span>

  <bx-btn
    kind="tertiary"
    iconLayout=""
    class="bx--btn"
    @click.prevent="addContact"
    size="field"
  >
    <span>Add another contact</span>
    <Add16 slot="icon" />
  </bx-btn>
</template>


