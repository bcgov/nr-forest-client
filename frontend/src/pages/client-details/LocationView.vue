<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import type {
  ActionWords,
  ClientLocation,
  ModalNotification,
  SaveEvent,
  UserRole,
} from "@/dto/CommonTypesDto";
import type { Address } from "@/dto/ApplyClientNumberDto";
import {
  formatPhoneNumber,
  getFormattedHtml,
  includesAnyOf,
  keepScrollBottomPosition as keepScrollBottomPositionFn,
  locationToCreateFormat,
  locationToEditFormat,
  preserveUnchangedData,
  removeNullText,
} from "@/services/ForestClientService";

import Edit16 from "@carbon/icons-vue/es/edit/16";
import Save16 from "@carbon/icons-vue/es/save/16";
import Close16 from "@carbon/icons-vue/es/close/16";
import Undefined16 from "@carbon/icons-vue/es/undefined/16";
import Renew16 from "@carbon/icons-vue/es/renew/16";
import Check16 from "@carbon/icons-vue/es/checkmark/16";

import { useFetchTo } from "@/composables/useFetch";
import { useEventBus } from "@vueuse/core";
import type { SaveableComponent } from "./shared";

const props = defineProps<{
  data: ClientLocation;
  userRoles: UserRole[];
  validations: Array<Function>;
  isReloading: boolean;
  keepScrollBottomPosition?: boolean;
  createMode?: boolean;
}>();

const emit = defineEmits<{
  (e: "save", payload: SaveEvent<ClientLocation>): void;
  (e: "canceled"): void;
  (e: "updateLocationName", value: string): void;
}>();

const indexString = props.data.clientLocnCode;

const index = indexString !== null ? Number(indexString) : null;

const businessPhone = computed(() => formatPhoneNumber(props.data.businessPhone));
const cellPhone = computed(() => formatPhoneNumber(props.data.cellPhone));
const homePhone = computed(() => formatPhoneNumber(props.data.homePhone));
const faxNumber = computed(() => formatPhoneNumber(props.data.faxNumber));

let originalData: ClientLocation;
let originalAddressData: Address;
const formAddressData = ref<Address>();

// Country related data
const countryList = ref([]);
useFetchTo("/api/codes/countries?page=0&size=250", countryList);

const revalidate = ref(false);

const isEditing = ref(!!props.createMode);
const hasAnyChange = ref(false);

const resetFormData = () => {
  originalData = JSON.parse(JSON.stringify(props.data));

  // As required by the StaffLocationGroupComponent
  const createFormData = locationToCreateFormat(props.data);

  const stringifiedData = JSON.stringify(createFormData);

  originalAddressData = JSON.parse(stringifiedData);
  formAddressData.value = JSON.parse(stringifiedData);

  hasAnyChange.value = false;
};

resetFormData();

watch(
  formAddressData,
  () => {
    if (isEditing.value) {
      const updatedData = preserveUnchangedData(formAddressData.value, originalAddressData);
      hasAnyChange.value = JSON.stringify(updatedData) !== JSON.stringify(originalAddressData);
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
  rawUpdatedLocation: ClientLocation,
  action: ActionWords = {
    infinitive: "update",
    pastParticiple: "updated",
  },
) => {
  const updatedLocation = preserveUnchangedData(rawUpdatedLocation, originalData);
  const patch = props.createMode ? null : jsonpatch.compare(originalData, updatedLocation);

  const operationType = props.createMode ? "insert" : "update";

  emit("save", {
    patch,
    updatedData: updatedLocation,
    action,
    operationType,
  });
};

const saveForm = () => {
  const location = locationToEditFormat(formAddressData.value, props.data);
  const action = props.createMode
    ? {
        infinitive: "create",
        pastParticiple: "created",
      }
    : {
        infinitive: "update",
        pastParticiple: "updated",
      };
  save(location, action);
};

const displayDeactivateModal = ref(false);

const handleDeactivate = () => {
  displayDeactivateModal.value = true;
};

const deactivate = () => {
  const newData: ClientLocation = {
    ...originalData,
    locnExpiredInd: "Y",
  };
  save(newData, {
    infinitive: "deactivate",
    pastParticiple: "deactivated",
  });
  displayDeactivateModal.value = false;
};

const displayReactivateModal = ref(false);

const handleReactivate = () => {
  displayReactivateModal.value = true;
};

const reactivate = () => {
  const newData: ClientLocation = {
    ...originalData,
    locnExpiredInd: "N",
  };
  save(newData, {
    infinitive: "reactivate",
    pastParticiple: "reactivated",
  });
  displayReactivateModal.value = false;
};

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);

const valid = ref(false);

// Defining the event bus to send notifications up
const bus = useEventBus<ModalNotification>("modal-notification");

const removeAdditionalDelivery = () => {
  formAddressData.value.complementaryAddressTwo = null;
  bus.emit({
    active: false,
    message: "",
    kind: "",
    toastTitle: "",
    handler: () => {},
  });
};

const handleRemoveAdditionalDelivery = () => {
  const selectedDeliveryInformation = formAddressData.value.complementaryAddressTwo;
  bus.emit({
    name: selectedDeliveryInformation,
    toastTitle: "Success",
    kind: "delivery information",
    message: `“${selectedDeliveryInformation}” additional delivery information was deleted`,
    handler: removeAdditionalDelivery,
    active: true,
  });
};
</script>

<template>
  <div class="grouping-12" :class="{ invisible: props.isReloading }">
    <div v-if="!isEditing" class="flex-column-1_5rem">
      <div :id="`location-${indexString}-address-section`" class="grouping-23">
        <read-only-component label="Address" :id="`location-${indexString}-address`">
          <div class="grouping-23 no-margin">
            <span
              :id="`location-${indexString}-addressOne`"
              class="body-compact-01"
              v-if="removeNullText(data.addressOne)"
            >
              {{ data.addressOne }}
            </span>
            <span
              :id="`location-${indexString}-addressTwo`"
              class="body-compact-01"
              v-if="removeNullText(data.addressTwo)"
            >
              {{ data.addressTwo }}
            </span>
            <span
              :id="`location-${indexString}-addressThree`"
              class="body-compact-01"
              v-if="removeNullText(data.addressThree)"
            >
              {{ data.addressThree }}
            </span>
            <span :id="`location-${indexString}-city-province`" class="body-compact-01">
              {{ data.city }}, {{ data.provinceDesc }}
            </span>
            <span :id="`location-${indexString}-country`" class="body-compact-01">
              {{ data.countryDesc }}
            </span>
            <span :id="`location-${indexString}-postalCode`" class="body-compact-01">
              {{ data.postalCode }}
            </span>
          </div>
        </read-only-component>
      </div>
      <div
        :id="`location-${indexString}-email-section`"
        class="grouping-23"
        v-if="data.emailAddress"
      >
        <read-only-component label="Email address" :id="`location-${indexString}-emailAddress`">
          <a :href="`mailto:${data.emailAddress}`">
            <span class="body-compact-01 colorless">{{ data.emailAddress }}</span>
          </a>
        </read-only-component>
      </div>
      <div
        :id="`location-${indexString}-phone-section`"
        class="grouping-10 no-padding margin-left-1_75rem"
        v-if="data.businessPhone || data.cellPhone || data.homePhone || data.faxNumber"
      >
        <read-only-component
          label="Primary phone number"
          :id="`location-${indexString}-primaryPhoneNumber`"
          v-if="data.businessPhone"
        >
          <a :href="`tel:${businessPhone}`">
            <span class="body-compact-01 colorless">{{ businessPhone }}</span>
          </a>
        </read-only-component>
        <read-only-component
          label="Secondary phone number"
          :id="`location-${indexString}-secondaryPhoneNumber`"
          v-if="data.cellPhone"
        >
          <a :href="`tel:${cellPhone}`">
            <span class="body-compact-01 colorless">{{ cellPhone }}</span>
          </a>
        </read-only-component>
        <read-only-component
          label="Tertiary phone number"
          :id="`location-${indexString}-tertiaryPhoneNumber`"
          v-if="data.homePhone"
        >
          <a :href="`tel:${homePhone}`">
            <span class="body-compact-01 colorless">{{ homePhone }}</span>
          </a>
        </read-only-component>
        <read-only-component label="Fax" :id="`location-${indexString}-fax`" v-if="data.faxNumber">
          <a :href="`tel:${faxNumber}`">
            <span class="body-compact-01 colorless">{{ faxNumber }}</span>
          </a>
        </read-only-component>
      </div>
      <div
        :id="`location-${indexString}-notes-section`"
        class="grouping-23"
        v-if="data.cliLocnComment"
      >
        <read-only-component label="Notes" :id="`location-${indexString}-notes`">
          <span
            class="body-compact-01"
            v-dompurify-html="getFormattedHtml(data.cliLocnComment)"
          ></span>
        </read-only-component>
      </div>
    </div>
    <div v-if="canEdit && !isEditing" class="screen-only">
      <cds-button
        v-if="props.data.locnExpiredInd === 'N'"
        :id="`location-${indexString}-EditBtn`"
        kind="tertiary"
        size="md"
        @click="edit"
      >
        <span class="width-unset">Edit location</span>
        <Edit16 slot="icon" />
      </cds-button>
      <cds-button
        v-if="props.data.locnExpiredInd === 'Y'"
        :id="`location-${indexString}-ReactivateBtn`"
        kind="tertiary"
        size="md"
        @click="handleReactivate"
        :disabled="saving"
      >
        <span class="width-unset">Reactivate location</span>
        <Renew16 slot="icon" />
      </cds-button>
    </div>
    <div class="tab-form" v-if="isEditing">
      <staff-details-location-group-component
        :id="index"
        v-bind:model-value="formAddressData"
        :countryList="countryList"
        :validations="props.validations"
        :revalidate="revalidate"
        includeTertiaryPhoneNumber
        hideDeleteButton
        @remove-additional-delivery="handleRemoveAdditionalDelivery"
        @valid="valid = $event"
        @update:model-value="revalidate = !revalidate"
        @update-location-name="emit('updateLocationName', $event)"
      />
      <div class="form-group-buttons form-group-buttons--stretched">
        <cds-button
          :id="`location-${indexString}-SaveBtn`"
          kind="primary"
          size="md"
          @click="saveForm"
          :disabled="saving || !hasAnyChange || !valid"
        >
          <template v-if="props.createMode">
            <span class="width-unset">Save location</span>
            <Check16 slot="icon" />
          </template>
          <template v-else>
            <span class="width-unset">Save changes</span>
            <Save16 slot="icon" />
          </template>
        </cds-button>
        <cds-button
          :id="`location-${indexString}-CancelBtn`"
          kind="tertiary"
          size="md"
          @click="cancel"
          :disabled="saving"
        >
          <span class="width-unset">Cancel</span>
          <Close16 slot="icon" />
        </cds-button>
        <cds-button
          v-if="index > 0 && !props.createMode"
          :id="`location-${indexString}-DeactivateBtn`"
          kind="danger--tertiary"
          size="md"
          @click="handleDeactivate"
          :disabled="saving"
        >
          <span class="width-unset">Deactivate location</span>
          <Undefined16 slot="icon" />
        </cds-button>
      </div>
    </div>
  </div>
  <cds-modal
    id="modal-deactivate"
    aria-labelledby="modal-deactivate-heading"
    size="sm"
    :open="displayDeactivateModal"
    @cds-modal-closed="displayDeactivateModal = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="modal-deactivate-heading"
        >Are you sure you want to deactivate "{{ props.data.clientLocnName }}" location?
      </cds-modal-heading>
    </cds-modal-header>

    <cds-modal-body id="modal-deactivate-body">
      <p>
        This location will still display but it will not be editable or usable for business
        purposes.
      </p>
    </cds-modal-body>

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
        v-on:click="deactivate"
        :danger-descriptor="`Deactivate &quot;${props.data.clientLocnName}&quot;`"
        :disabled="saving"
      >
        Deactivate location
        <Undefined16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>
  <cds-modal
    id="modal-reactivate"
    aria-labelledby="modal-reactivate-heading"
    size="sm"
    :open="displayReactivateModal"
    @cds-modal-closed="displayReactivateModal = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading id="modal-reactivate-heading"
        >Are you sure you want to reactivate "{{ props.data.clientLocnName }}" location?
      </cds-modal-heading>
    </cds-modal-header>

    <cds-modal-body id="modal-reactivate-body">
      <p>This location will be usable and editable again.</p>
    </cds-modal-body>

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
        kind="primary"
        class="cds--modal-submit-btn"
        v-on:click="reactivate"
        :disabled="saving"
      >
        Reactivate location
        <Renew16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>
</template>

<style scoped>
.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
