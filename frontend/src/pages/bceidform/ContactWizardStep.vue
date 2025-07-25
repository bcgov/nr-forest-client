<script setup lang="ts">
import { watch, ref, computed, reactive, onMounted, getCurrentInstance } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
// Composables
import { useEventBus } from "@vueuse/core";
import { useFetchTo } from "@/composables/useFetch";
import { useFocus } from "@/composables/useFocus";
// Type Imports
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import { emptyContact, defaultLocation } from "@/dto/ApplyClientNumberDto";
import type { CodeNameType, ModalNotification } from "@/dto/CommonTypesDto";
// Validators
import { isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";
// @ts-ignore
import Add16 from "@carbon/icons-vue/es/add/16";
import { getContactDescription } from "@/services/ForestClientService";

//Defining the props and emiter to reveice the data and emit an update
const props = defineProps<{ data: FormDataDto; active: boolean }>();

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

const updateContact = (value: Contact | undefined, index: number) => {
  if (index < formData.location.contacts.length) {
    if (value) formData.location.contacts[index] = value;
    else {
      const contactCopy: Contact[] = [...formData.location.contacts];
      contactCopy.splice(index, 1);
      formData.location.contacts = contactCopy;
    }
  }
  revalidate.value = !revalidate.value;
};

//Role related data
const roleList = ref([]);
const fetch = () => {
  if (props.active)
    useFetchTo("/api/codes/contact-types?page=0&size=250", roleList);
};

watch(() => props.active, fetch);
fetch();

//Addresses Related data
const addresses = computed<CodeNameType[]>(() =>
  formData.location.addresses.map((address, index) => {
    return { code: index + "", name: address.locationName } as CodeNameType;
  })
);

const uniqueValues = isUniqueDescriptive();

let lastContactId = -1; // The first contactId to be generated minus 1.
const getNewContactId = () => ++lastContactId;

// Associate each contact to a unique id, permanent for the lifecycle of this component.
const contactsIdMap = new Map<Contact, number>(
  formData.location.contacts.map((contact) => [contact, getNewContactId()]),
);

const instance = getCurrentInstance();
const features = instance.appContext.config.globalProperties.$features;

//New contact being added
const otherContacts = computed(() => formData.location.contacts.slice(1));
const addContact = (autoFocus = true) => {
  const newContact = JSON.parse(JSON.stringify(emptyContact));
  if (!features.BCEID_MULTI_ADDRESS) {
    newContact.locationNames.push(defaultLocation);
  }
  const newLength = formData.location.contacts.push(newContact);

  /*
  For some reason we need to get the contact from the array to make it work.
  It doesn't work with the value from newContact as key.
  */
  const contact = formData.location.contacts[newLength - 1];
  contactsIdMap.set(contact, getNewContactId());
  if (autoFocus) {
    const focusIndex = newLength - 1;
    setScrollPoint(`contact-${focusIndex}-heading`);
    setFocusedComponent(`contact-${focusIndex}-heading`);
  }
  return newLength;
};

const removeContact = (index: number) => () => {
  const contact = formData.location.contacts[index];
  const contactId = contactsIdMap.get(contact);
  contactsIdMap.delete(contact);

  updateContact(undefined, index);
  delete validation[contactId];
  uniqueValues.remove("Name", contactId + "");
  bus.emit({
    active: false,
    message: "",
    kind: "",
    toastTitle: "",
    handler: () => {},
  });
};

//Validation
const validation = reactive<Record<string, boolean>>({
  0: false,
});

const updateValidState = (index: number, valid: boolean) => {
  const contactId = contactsIdMap.get(formData.location.contacts[index]);
  if (validation[contactId] !== valid) {
    validation[contactId] = valid;
  }
};

const checkValid = () =>
  Object.values(validation).reduce(
    (accumulator: boolean, currentValue: boolean) =>
      accumulator && currentValue,
    true
  );

watch([validation], () => emit("valid", checkValid()));
emit("valid", false);

const handleRemove = (index: number) => {
  const selectedContact = getContactDescription(formData.location.contacts[index], index);
  bus.emit({
    name: selectedContact,
    toastTitle: "Success",
    kind: "contact",
    message: `“${selectedContact}” additional contact was deleted`,
    handler: removeContact(index),
    active: true,
  });
};

onMounted(() => setFocusedComponent("focus-2", 0));

defineExpose({
  addContact,
});
</script>

<template>
  <contact-group-component
    :id="0"
    v-model="formData.location.contacts[0]"
    required-label
    :roleList="roleList"
    :addressList="addresses"
    :validations="[uniqueValues.add]"
    :revalidate="revalidate"
    :enabled="false"
    @update:model-value="updateContact($event, 0)"
    @valid="updateValidState(0, $event)"
  />

  <div class="frame-01" v-if="otherContacts.length > 0" aria-live="off">

    <div v-for="(contact, index) in otherContacts">
      <hr />
      <div class="grouping-09" :data-scroll="`additional-contact-${index + 1}`">
        <h3 :data-focus="`contact-${index + 1}-heading`" tabindex="-1">
          <div :data-scroll="`contact-${index + 1}-heading`" class="header-offset"></div>
          Additional contact
        </h3>
      </div>
      <contact-group-component
        :key="contactsIdMap.get(contact)"
        :id="contactsIdMap.get(contact)"
        v-bind:modelValue="contact"
        required-label
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

  <p 
    class="body-compact-01"
    v-if="formData.location.contacts.length < 5">
    You can add contacts to the account. For example, a person you want to give or receive information on your behalf.
  </p>

  <cds-button
    kind="tertiary"
    @click.prevent="addContact"
    v-if="formData.location.contacts.length < 5"
  >
    <span>Add another contact</span>
    <Add16 slot="icon" />
  </cds-button>

  <p 
    class="body-compact-01"
    v-if="formData.location.contacts.length >= 5"
    id="maxAdditionalContsReachedLblId">
    You can only add a maximum of 5 contacts.
  </p>
  
</template>
