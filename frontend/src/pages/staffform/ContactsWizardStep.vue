<script setup lang="ts">
import { watch, ref, computed, reactive, onMounted } from "vue";
// Carbon
import "@carbon/web-components/es/components/button/index";
// Composables
import { useEventBus } from "@vueuse/core";
import { useFocus } from "@/composables/useFocus";
import { useFetchTo } from "@/composables/useFetch";
// Type
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import { indexedEmptyContact } from "@/dto/ApplyClientNumberDto";
import type { CodeNameType, ModalNotification } from "@/dto/CommonTypesDto";
import { isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";
import { getContactDescription } from "@/services/ForestClientService";
import StaffContactGroupComponent from "@/components/grouping/StaffContactGroupComponent.vue";

// @ts-ignore
import Add16 from "@carbon/icons-vue/es/add/16";

//Defining the props and emitter to receive the data and emit an update
const props = withDefaults(
  defineProps<{ data: FormDataDto; active: boolean; maxContacts?: number }>(),
  { maxContacts: 25 },
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

const updateContact = (value: Contact | undefined, index: number) => {
  if (index < formData.location.contacts.length) {
    if (value) {
      formData.location.contacts[index] = value;
    } else {
      const contactsCopy: Contact[] = [...formData.location.contacts];
      contactsCopy.splice(index, 1);
      formData.location.contacts = contactsCopy;
    }
    revalidate.value = !revalidate.value;
  }
};

//Country related data
const countryList = ref([]);

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

let lastContactId = -1; // The first contactId to be generated minus 1.
const getNewContactId = () => ++lastContactId;

// Associate each contacts to a unique id, permanent for the lifecycle of this component.
const contactsIdMap = new Map<Contact, number>(
  formData.location.contacts.map((contact) => {
    const contactId = getNewContactId();
    if(contactId !== contact.index) contact.index = contactId;
    contact.index = contactId;
    return [contact, contactId];
  }),
);

//New contact being added
const otherContacts = computed(() => formData.location.contacts.slice(1));
const addContact = () => {
  const newLength = formData.location.contacts.push(indexedEmptyContact(getNewContactId()));
  const contact = formData.location.contacts[newLength - 1];
  contactsIdMap.set(contact, contact.index);  
  setScrollPoint(`contact-${contact.index}-heading`);
  setFocusedComponent(`contact-${contact.index}-heading`);
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
  const contactId = contactsIdMap.get(formData.location.contacts[index]);
  if (validation[contactId] !== valid) {
    validation[contactId] = valid;
  }
};

const uniqueValues = isUniqueDescriptive();

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

onMounted(() => setFocusedComponent("focus-1", 0));

const contactName = (contact: Contact) =>{
  if(!contact) return 'Additional contact'
  const name = `${contact.firstName} ${contact.lastName}`
  if(!name.trim()) return 'Additional contact'
  return name
}

</script>

<template>
  <h3 data-focus="contact-0-heading" tabindex="-1">
    <div data-scroll="contact-0-heading" class="header-offset"></div>
    Primary contact
  </h3>
  <cds-inline-notification
    v-shadow="2"
    low-contrast="true"
    open="true"
    kind="info"
    hide-close-button="true"
    title="">
    <p class="cds--inline-notification-content">
      <strong>Primary contact: </strong>
      Applicant must be a contact for this client.
    </p>
  </cds-inline-notification>
  <staff-contact-group-component
    :id="0"
    v-model="formData.location.contacts[0]"
    :role-list="roleList"
    :address-list="addresses"
    :validations="[uniqueValues.add]"
    :revalidate="revalidate"
    :read-only-name="formData.businessInformation.clientType === 'I'"
    @update:model-value="updateContact($event, 0)"
    @valid="updateValidState(0, $event)"
  />

  <hr />
  <h3>Additional contacts</h3>
  <div class="frame-01" v-if="otherContacts.length > 0" aria-live="off">
    <cds-accordion v-for="(contact, index) in otherContacts" :key="contactsIdMap.get(contact)">
      <div :data-scroll="`contact-${index + 1}-heading`" class="header-offset"></div>
      <cds-accordion-item
        open
        :title="contactName(contact)"
        size="lg"
        class="grouping-13"
        :data-focus="`contact-${index + 1}-heading`"
      >
        <staff-contact-group-component
          :id="contactsIdMap.get(contact)"
          v-bind:model-value="contact"
          :role-list="roleList"
          :address-list="addresses"
          :validations="[uniqueValues.add]"
          :revalidate="revalidate"
          :read-only-name="false"
          @update:model-value="updateContact($event, index + 1)"
          @valid="updateValidState(index + 1, $event)"
          @remove="handleRemove(index + 1)"          
        />
      </cds-accordion-item>
    </cds-accordion>
  </div>
  <div class="grouping-02">
    <p class="body-02 light-theme-text-text-primary">
      {{
        formData.location.contacts.length < props.maxContacts
          ? "Contacts can be added to the applicant's account"
          : `You can only add a maximum of ${props.maxContacts} contacts.`
      }}
    </p>
  </div>
  <cds-button
    kind="tertiary"
    @click.prevent="addContact"
    v-if="formData.location.contacts.length < props.maxContacts"
  >
    <span>Add another contact</span>
    <Add16 slot="icon" />
  </cds-button>
  <hr />
</template>
