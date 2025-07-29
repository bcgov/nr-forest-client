<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import * as jsonpatch from "fast-json-patch";
import { OtherRelatedClientEntry, type ActionWords, type ClientLocation, type RelatedClientEntry, type SaveEvent, type UserRole } from "@/dto/CommonTypesDto";
import {
  compareAny,
  formatAddress,
  formatLocation,
  formatPhoneNumber,
  getFormattedHtml,
  includesAnyOf,
  keepScrollBottomPosition as keepScrollBottomPositionFn,
  preserveUnchangedData,
  removeNullText,
  toTitleCase,
} from "@/services/ForestClientService";

import Edit16 from "@carbon/icons-vue/es/edit/16";
import Save16 from "@carbon/icons-vue/es/save/16";
import Close16 from "@carbon/icons-vue/es/close/16";
import Undefined16 from "@carbon/icons-vue/es/undefined/16";
import Renew16 from "@carbon/icons-vue/es/renew/16";
import Check16 from "@carbon/icons-vue/es/checkmark/16";

import { useFetchTo } from "@/composables/useFetch";

const props = defineProps<{
  data: RelatedClientEntry[];
  location: ClientLocation;
  userRoles: UserRole[];
  validations: Array<Function>;
  isReloading: boolean;
  keepScrollBottomPosition?: boolean;
  createMode?: boolean;
}>();

const emit = defineEmits<{
  (e: "save", payload: SaveEvent<RelatedClientEntry>): void;
  (e: "canceled"): void;
  (e: "updateLocationName", value: string): void;
}>();

const indexString = props.location.clientLocnCode;

const index = indexString !== null ? Number(indexString) : null;

const sortedData = computed<OtherRelatedClientEntry[]>(() => {
  const normalizedData = props.data.map((entry) => {
    // const {
    //   client: _c,
    //   relatedClient: _r,
    //   ...result
    // } = {
    //   ...entry,
    //   otherClient: entry.isMainParticipant ? entry.relatedClient : entry.client,
    // };
    const result = {
      ...entry,
      otherClient: entry.isMainParticipant ? entry.relatedClient : entry.client,
    };
    delete result.client;
    delete result.relatedClient;
    return result as OtherRelatedClientEntry;
  });

  const result = normalizedData.toSorted(
    (a, b) =>
      compareAny(a.relationship.name, b.relationship.name) ||
      compareAny(a.isMainParticipant, b.isMainParticipant) ||
      compareAny(a.otherClient.client.name, b.otherClient.client.name) ||
      compareAny(a.otherClient.location.name, b.otherClient.location.name),
  );
  return result;
});

const businessPhone = computed(() => formatPhoneNumber(props.data.businessPhone));
const cellPhone = computed(() => formatPhoneNumber(props.data.cellPhone));
const homePhone = computed(() => formatPhoneNumber(props.data.homePhone));
const faxNumber = computed(() => formatPhoneNumber(props.data.faxNumber));

let originalData: RelatedClientEntry;
const formData = ref<RelatedClientEntry>();

// Country related data
const countryList = ref([]);
useFetchTo("/api/codes/countries?page=0&size=250", countryList);

const revalidate = ref(false);

const isEditing = ref(!!props.createMode);
const hasAnyChange = ref(false);

const resetFormData = () => {
  const stringifiedData = JSON.stringify(props.data);

  originalData = JSON.parse(stringifiedData);
  formData.value = JSON.parse(stringifiedData);

  hasAnyChange.value = false;
};

resetFormData();

watch(
  formData,
  () => {
    if (isEditing.value) {
      const updatedData = preserveUnchangedData(formData.value, originalData);
      hasAnyChange.value = JSON.stringify(updatedData) !== JSON.stringify(originalData);
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

defineExpose({
  lockEditing,
  setSaving,
});

const save = (
  rawUpdatedRelationship: RelatedClientEntry,
  action: ActionWords = {
    infinitive: "update",
    pastParticiple: "updated",
  },
) => {
  const updatedRelationship = preserveUnchangedData(rawUpdatedRelationship, originalData);
  const patch = props.createMode ? null : jsonpatch.compare(originalData, updatedRelationship);

  const operationType = props.createMode ? "insert" : "update";

  emit("save", {
    patch,
    updatedData: updatedRelationship,
    action,
    operationType,
  });
};

const saveForm = () => {
  const relationship = formData.value;
  const action = props.createMode
    ? {
        infinitive: "create",
        pastParticiple: "created",
      }
    : {
        infinitive: "update",
        pastParticiple: "updated",
      };
  save(relationship, action);
};

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);

const valid = ref(false);
</script>

<template>
  <div class="grouping-12" :class="{ invisible: props.isReloading }">
    <template v-if="!isEditing">
      <read-only-component
        label="Mailing address"
        :id="`location-relationships-${indexString}-mailingAddress`"
      >
        <a>
          <span class="body-compact-01 colorless">{{ formatAddress(props.location) }}</span>
        </a>
      </read-only-component>
      <cds-table id="relatioships-table" use-zebra-styles>
        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell class="col-padding" />
            <cds-table-header-cell class="col-123">Relationship type</cds-table-header-cell>
            <cds-table-header-cell class="col-123">Related client location</cds-table-header-cell>
            <cds-table-header-cell class="col-123">Percentage owned</cds-table-header-cell>
            <cds-table-header-cell class="col-123">Signing authority</cds-table-header-cell>
            <cds-table-header-cell v-if="canEdit && false" class="col-123">Actions</cds-table-header-cell>
          </cds-table-header-row>
        </cds-table-head>
        <cds-table-body>
          <cds-table-row v-for="row in sortedData" :key="row">
            <cds-table-cell />
            <cds-table-cell>
              <span>{{ row.relationship.name }}</span>
              &nbsp;
              <cds-tag type="purple" title="" v-if="row.isMainParticipant">
                <span>Primary</span>
              </cds-tag>
            </cds-table-cell>
            <cds-table-cell>
              <span>
                <a
                  :href="`/clients/details/${row.otherClient.client.code}`"
                  target="_blank"
                  rel="noopener"
                >
                  {{ row.otherClient.client.code }}
                </a>
                , {{ row.otherClient.client.name }}
                <a
                  :href="`/clients/details/${row.otherClient.client.code}`"
                  target="_blank"
                  rel="noopener"
                >
                  {{ formatLocation(row.otherClient.location.code, row.otherClient.location.name) }}
                </a>
              </span>
            </cds-table-cell>
            <cds-table-cell>
              <span>{{ `${row.percentageOwnership}%` || "-" }}</span>
            </cds-table-cell>
            <cds-table-cell>
              <span>{{ row.hasSigningAuthority || "-" }}</span>
            </cds-table-cell>
            <cds-table-cell v-if="canEdit && false">
              <!-- TODO: Actions column contents -->
            </cds-table-cell>
          </cds-table-row>
        </cds-table-body>
      </cds-table>
    </template>
    <div v-if="canEdit && !isEditing">
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
      <staff-location-group-component
        :id="index"
        v-bind:model-value="formData"
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
