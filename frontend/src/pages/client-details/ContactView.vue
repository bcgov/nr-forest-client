<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import Edit16 from "@carbon/icons-vue/es/edit/16";
import Save16 from "@carbon/icons-vue/es/save/16";
import Close16 from "@carbon/icons-vue/es/close/16";
import Trash16 from "@carbon/icons-vue/es/trash-can/16";
import Check16 from "@carbon/icons-vue/es/checkmark/16";
import type {
  ActionWords,
  ClientContact,
  ClientLocation,
  CodeNameType,
  SaveEvent,
  UserRole,
} from "@/dto/CommonTypesDto";
import {
  contactToCreateFormat,
  contactToEditFormat,
  formatPhoneNumber,
  includesAnyOf,
  keepScrollBottomPosition as keepScrollBottomPositionFn,
  preserveUnchangedData,
} from "@/services/ForestClientService";
import type { Contact } from "@/dto/ApplyClientNumberDto";
import { useFetchTo } from "@/composables/useFetch";
import type { SaveableComponent } from "./shared";

const props = defineProps<{
  data: ClientContact;
  index: number;
  associatedLocationsString: string;
  allLocations: ClientLocation[];
  userRoles: UserRole[];
  validations: Array<Function>;
  isReloading: boolean;
  keepScrollBottomPosition?: boolean;
  createMode?: boolean;
}>();

const emit = defineEmits<{
  (e: "save", payload: SaveEvent<ClientContact>): void;
  (e: "delete", contact: ClientContact): void;
  (e: "canceled"): void;
  (e: "updateContactName", value: string): void;
}>();

const businessPhone = computed(() => formatPhoneNumber(props.data.businessPhone));
const secondaryPhone = computed(() => formatPhoneNumber(props.data.secondaryPhone));
const faxNumber = computed(() => formatPhoneNumber(props.data.faxNumber));

let originalData: ClientContact;
let originalContactData: Contact;
const formContactData = ref<Contact>();

const roleList = ref([]);
useFetchTo("/api/codes/contact-types?page=0&size=250", roleList);

const addressList = computed(() =>
  props.allLocations.map(
    (location) =>
      ({
        code: location.clientLocnCode,
        name: location.clientLocnName,
      }) as CodeNameType,
  ),
);

const revalidate = ref(false);

const isEditing = ref(!!props.createMode);
const hasAnyChange = ref(false);

let previousValue: Contact;

const resetFormData = () => {
  originalData = JSON.parse(JSON.stringify(props.data));

  // As required by the StaffContactGroupComponent
  const createFormData = contactToCreateFormat(props.data, props.allLocations);

  const stringifiedData = JSON.stringify(createFormData);

  originalContactData = JSON.parse(stringifiedData);
  formContactData.value = JSON.parse(stringifiedData);
  previousValue = JSON.parse(JSON.stringify(formContactData.value));

  hasAnyChange.value = false;
};

resetFormData();

watch(
  formContactData,
  () => {
    if (isEditing.value) {
      const updatedData = preserveUnchangedData(formContactData.value, originalContactData);
      hasAnyChange.value = JSON.stringify(updatedData) !== JSON.stringify(originalContactData);

      if (formContactData.value.fullName !== previousValue.fullName) {
        emit("updateContactName", formContactData.value.fullName);
      }

      previousValue = JSON.parse(JSON.stringify(formContactData.value));
    }
  },
  { deep: true },
);

const saving = ref(false);

const edit = () => {
  resetFormData();
  isEditing.value = true;
  saving.value = false;
};

const cancel = () => {
  lockEditing();
  emit("canceled");
};

const lockEditing = () => {
  isEditing.value = false;
  if (props.keepScrollBottomPosition) {
    keepScrollBottomPositionFn(nextTick());
  }
};

const setSaving = (value: boolean) => {
  saving.value = value;
};

defineExpose<SaveableComponent>({
  lockEditing,
  setSaving,
});

const save = (
  rawUpdatedContact: ClientContact,
  action: ActionWords = {
    infinitive: "update",
    pastParticiple: "updated",
  },
) => {
  const updatedContact = preserveUnchangedData(rawUpdatedContact, originalData);
  const patch = props.createMode ? null : jsonpatch.compare(originalData, updatedContact);

  const operationType = props.createMode ? "insert" : "update";

  emit("save", {
    patch,
    updatedData: updatedContact,
    action,
    operationType,
  });
};

const saveForm = () => {
  const contact = contactToEditFormat(formContactData.value, props.data);
  const action = props.createMode
    ? {
        infinitive: "create",
        pastParticiple: "created",
      }
    : {
        infinitive: "update",
        pastParticiple: "updated",
      };
  save(contact, action);
};

const displayDeleteModal = ref(false);

const handleDelete = () => {
  displayDeleteModal.value = true;
};

const deleteContact = () => {
  emit("delete", props.data);
  displayDeleteModal.value = false;
};

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);

const valid = ref(false);
</script>

<template>
  <div
    :id="`contact-${index}-general-section`"
    class="grouping-12"
    :class="{ invisible: props.isReloading }"
  >
    <div v-if="!isEditing" class="flex-column-1_5rem">
      <read-only-component label="Contact type" :id="`contact-${index}-contactType`">
        <span class="body-compact-01">{{ data.contactTypeDesc }}</span>
      </read-only-component>
      <read-only-component
        label="Associated locations"
        :id="`contact-${index}-associatedLocations`"
      >
        <span class="body-compact-01">{{ associatedLocationsString }}</span>
      </read-only-component>
      <read-only-component
        label="Email address"
        :id="`contact-${index}-emailAddress`"
        v-if="data.emailAddress"
      >
        <a :href="`mailto:${data.emailAddress}`">
          <span class="body-compact-01 colorless">{{ data.emailAddress }}</span>
        </a>
      </read-only-component>
      <div
        :id="`contact-${index}-phone-section`"
        class="grouping-10 no-padding"
        v-if="data.businessPhone || data.secondaryPhone || data.faxNumber"
      >
        <read-only-component
          label="Primary phone number"
          :id="`contact-${index}-primaryPhoneNumber`"
          v-if="data.businessPhone"
        >
          <a :href="`tel:${businessPhone}`">
            <span class="body-compact-01 colorless">{{ businessPhone }}</span>
          </a>
        </read-only-component>
        <read-only-component
          label="Secondary phone number"
          :id="`contact-${index}-secondaryPhoneNumber`"
          v-if="data.secondaryPhone"
        >
          <a :href="`tel:${secondaryPhone}`">
            <span class="body-compact-01 colorless">{{ secondaryPhone }}</span>
          </a>
        </read-only-component>
        <read-only-component label="Fax" :id="`contact-${index}-fax`" v-if="data.faxNumber">
          <a :href="`tel:${faxNumber}`">
            <span class="body-compact-01 colorless">{{ faxNumber }}</span>
          </a>
        </read-only-component>
      </div>
    </div>
    <div v-if="canEdit && !isEditing" class="screen-only">
      <cds-button :id="`contact-${index}-EditBtn`" kind="tertiary" size="md" @click="edit">
        <span class="width-unset">Edit contact</span>
        <Edit16 slot="icon" />
      </cds-button>
    </div>
    <div class="tab-form" v-if="isEditing">
      <staff-contact-group-component
        :id="index"
        v-bind:model-value="formContactData"
        :role-list="roleList"
        :address-list="addressList"
        :validations="props.validations"
        :revalidate="revalidate"
        single-input-for-name
        hideDeleteButton
        show-location-code
        @valid="valid = $event"
        @update:model-value="revalidate = !revalidate"
      />
      <div class="form-group-buttons form-group-buttons--stretched">
        <cds-button
          :id="`contact-${index}-SaveBtn`"
          kind="primary"
          size="md"
          @click="saveForm"
          :disabled="saving || !hasAnyChange || !valid"
        >
          <template v-if="props.createMode">
            <span class="width-unset">Save contact</span>
            <Check16 slot="icon" />
          </template>
          <template v-else>
            <span class="width-unset">Save changes</span>
            <Save16 slot="icon" />
          </template>
        </cds-button>
        <cds-button
          :id="`contact-${index}-CancelBtn`"
          kind="tertiary"
          size="md"
          @click="cancel"
          :disabled="saving"
        >
          <span class="width-unset">Cancel</span>
          <Close16 slot="icon" />
        </cds-button>
        <cds-button
          v-if="!props.createMode"
          :id="`contact-${index}-DeleteBtn`"
          kind="danger--tertiary"
          size="md"
          @click="handleDelete"
          :disabled="saving"
        >
          <span class="width-unset">Delete contact</span>
          <Trash16 slot="icon" />
        </cds-button>
      </div>
    </div>
  </div>
  <cds-modal
    id="modal-delete"
    aria-labelledby="modal-delete-heading"
    size="sm"
    :open="displayDeleteModal"
    @cds-modal-closed="displayDeleteModal = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="modal-delete-heading"
        >Are you sure you want to delete "{{ props.data.contactName }}" contact?
      </cds-modal-heading>
    </cds-modal-header>

    <cds-modal-body id="modal-delete-body"></cds-modal-body>

    <cds-modal-footer>
      <cds-modal-footer-button
        kind="secondary"
        data-modal-close
        class="cds--modal-close-btn"
        :disabled="saving"
      >
        Cancel
      </cds-modal-footer-button>

      <cds-modal-footer-button
        kind="danger"
        class="cds--modal-submit-btn"
        v-on:click="deleteContact"
        :danger-descriptor="`Delete &quot;${props.data.contactName}&quot;`"
        :disabled="saving"
      >
        Delete contact
        <Trash16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>
</template>

<style scoped>
.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
