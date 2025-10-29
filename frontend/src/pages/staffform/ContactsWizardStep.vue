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
import type { FormDataDto, Contact } from "@/dto/ApplyClientNumberDto";
import {
  defaultContactType,
  defaultLocation,
  indexedEmptyContact,
} from "@/dto/ApplyClientNumberDto";
import type { CodeNameType, ModalNotification } from "@/dto/CommonTypesDto";
import { isUniqueDescriptive } from "@/helpers/validators/GlobalValidators";
import { getContactDescription } from "@/services/ForestClientService";
import StaffContactGroupComponent from "@/components/grouping/StaffContactGroupComponent.vue";

// @ts-ignore
import Add16 from "@carbon/icons-vue/es/add/16";

//Defining the props and emitter to receive the data and emit an update
const props = withDefaults(
  defineProps<{ data: FormDataDto; active: boolean; maxContacts?: number }>(),
  { maxContacts: 25 }
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
      Object.assign(formData.location.contacts[index], value);
    } else {
      const contactsCopy: Contact[] = [...formData.location.contacts];
      contactsCopy.splice(index, 1);
      formData.location.contacts = contactsCopy;
    }
    revalidate.value = !revalidate.value;
  }
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

const fuzzyNotification = reactive<Record<string, boolean>>({});

const open = reactive<Record<string, boolean>>(
  formData.location.contacts.reduce((accumulator: Record<string, boolean>, current: Contact) => {
    const contactId = current.index;
    accumulator[contactId] = true;
    return accumulator;
  }, {}),
);

const setFuzzyNotification = (index: number, show: boolean) => {
  const contact = formData.location.contacts[index];
  const contactId = contact.index;
  fuzzyNotification[contactId] = show;
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
    formData.location.contacts.forEach((contact) => {
      const id = contact.index;
      open[id] = fuzzyNotification[id];
    });
  }
});

let lastContactId = formData.location.contacts.length - 1; // The first contactId to be generated minus 1.
const getNewContactId = () => ++lastContactId;

//New contact being added
const otherContacts = computed(() => formData.location.contacts.slice(1));

const addContact = () => {
  const newLength = formData.location.contacts.push(
    indexedEmptyContact(getNewContactId())
  );
  const contact = formData.location.contacts[newLength - 1];
  contact.locationNames = [
    // The default location `value` with the updated name
    {
      value: defaultLocation.value,
      text: formData.location.addresses[0].locationName,
    },
  ];
  contact.contactType = defaultContactType;

  setScrollPoint(`contact-${contact.index}-heading`);
  setFocusedComponent(`contact-${contact.index}-heading`);
  open[contact.index] = true;
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
  const contact = formData.location.contacts[index];
  const contactId = contact.index;
  if (validation[contactId] !== valid) {
    validation[contactId] = valid;
  }
};

const uniqueValues = isUniqueDescriptive();

const removeContact = (index: number) => () => {
  const contact = formData.location.contacts[index];
  const contactId = contact.index;

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
  const selectedContact = getContactDescription(
    formData.location.contacts[index],
    index + 1, // 1-based index to display in the message
  );
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

const cdsAccordionItemBeingtoggled = (event: any, contact: Contact) => {
  const id = contact.index;
  open[id] = event.detail.open;
  /*
  This somehow prevents some UI issues when there are manual expand/collapse by the user followed
  by automatic expand/collapse or vice-versa.
  */
  if (event.detail.open) {
    event.preventDefault();
  }
};

const contactName = (contact: Contact) => {
  if (!contact) return "Additional contact";
  const name = `${contact.firstName} ${contact.lastName}`;
  if (!name.trim()) return "Additional contact";
  return name;
};
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
  <div class="errors-container hide-when-less-than-two-children">
    <!--
    The parent div is necessary to avoid the div.header-offset below from interfering in the flex flow.
    -->
    <div data-scroll="location-contacts-0" class="header-offset"></div>
    <fuzzy-match-notification-grouping-component
      id="location-contacts-0"
      :ref="(el: any) => setFuzzyNotificationByElement(el, 0)"
    />
  </div>
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
    <cds-accordion v-for="(contact, index) in otherContacts" :key="contact.index">
      <div
        :data-scroll="`contact-${index + 1}-heading`"
        class="header-offset"
      ></div>
      <cds-accordion-item
        :open="open[contact.index]"
        :title="contactName(contact)"
        :data-text="`${contactName(contact) || ''}`"
        size="lg"
        class="grouping-13"
        :data-focus="`contact-${index + 1}-heading`"
        @cds-accordion-item-beingtoggled="
          (event: any) => cdsAccordionItemBeingtoggled(event, contact)
        "
      >
        <div class="errors-container hide-when-less-than-two-children">
          <!--
          The parent div is necessary to avoid the div.header-offset below from interfering in the flex flow.
          -->
          <div :data-scroll="`location-contacts-${index + 1}`" class="header-offset"></div>
          <fuzzy-match-notification-grouping-component
            :id="`location-contacts-${index + 1}`"
            :ref="(el: any) => setFuzzyNotificationByElement(el, index + 1)"
          />
        </div>
        <staff-contact-group-component
          :id="contact.index"
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
