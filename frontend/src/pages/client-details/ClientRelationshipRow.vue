<script setup lang="ts">
import { computed, inject, ref } from "vue";
import type {
  ClientDetails,
  IndexedRelatedClient,
  SaveEvent,
  UserRole,
} from "@/dto/CommonTypesDto";
import {
  booleanToYesNo,
  createRemovePatch,
  formatLocation,
  formatRelatedClient,
  includesAnyOf,
  toTitleCase,
} from "@/services/ForestClientService";
import Edit16 from "@carbon/icons-vue/es/edit/16";
import TrashCan16 from "@carbon/icons-vue/es/trash-can/16";

import ClientRelationshipForm from "@/pages/client-details/ClientRelationshipForm.vue";
import {
  CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT,
  type OperateRelatedClient,
  type OperationOptions,
  type SaveableComponent,
} from "./shared";

const props = defineProps<{
  row: IndexedRelatedClient;
  clientData: ClientDetails;
  locationIndex: string;
  validations: Array<Function>;
  userRoles: UserRole[];
}>();

const operateRelatedClient = inject<OperateRelatedClient>("operateRelatedClient");

const save = (payload: SaveEvent<IndexedRelatedClient>) => {
  operateRelatedClient(payload, {
    saveableComponent: thisSaveableComponent,
  });
};

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);

const formRef = ref<InstanceType<typeof ClientRelationshipForm> | null>(null);

const isEditing = ref(false);

const edit = () => {
  isEditing.value = true;
};

const cancel = () => {
  isEditing.value = false;
};

const lockEditing = () => {
  isEditing.value = false;
};

const localSaving = ref(false);

const setSaving = (value: boolean) => {
  if (formRef.value) {
    formRef.value.setSaving(value);
  }
  localSaving.value = value;
};

const thisSaveableComponent: SaveableComponent = { setSaving, lockEditing };

const displayDeleteModal = ref(false);

const handleDelete = () => {
  displayDeleteModal.value = true;
};

const deleteRelatedClient = (
  relatedClient: IndexedRelatedClient,
  rawOptions?: OperationOptions,
) => {
  const { index: relatedClientIndex, originalLocation } = relatedClient;
  const patch = createRemovePatch(`/relatedClients/${originalLocation.code}/${relatedClientIndex}`);
  operateRelatedClient(
    {
      action: {
        infinitive: "delete",
        pastParticiple: "deleted",
      },
      patch,
      updatedData: relatedClient,
      operationType: "delete",
    },
    {
      ...rawOptions,
      preserveRawPatch: true,
    },
  );
};

const confirmDeleteRelatedClient = () => {
  deleteRelatedClient(props.row, {
    saveableComponent: thisSaveableComponent,
  });
  displayDeleteModal.value = false;
};
</script>

<template>
  <cds-table-row :class="{ edit: isEditing }">
    <cds-table-cell class="no-padding" />
    <template v-if="!isEditing">
      <cds-table-cell
        v-for="selectedClient in row.isMainParticipant ? [row.client] : [row.relatedClient]"
        :key="selectedClient.client.code"
      >
        <span>
          <template v-if="!row.isMainParticipant">
            <a
              :href="`/clients/details/${selectedClient.client.code}`"
              target="_blank"
              rel="noopener"
              >{{ selectedClient.client.code }}</a
            >,
          </template>
          {{ toTitleCase(selectedClient.client.name) }}
          <template v-if="!row.isMainParticipant">
            <br />
            <a
              :href="`/clients/details/${selectedClient.client.code}`"
              target="_blank"
              rel="noopener"
            >
              {{ formatLocation(selectedClient.location.code, selectedClient.location.name) }}
            </a>
          </template>
        </span>
      </cds-table-cell>
      <cds-table-cell>
        <div>
          <span>{{ row.relationship.name }}</span>
        </div>
      </cds-table-cell>
      <cds-table-cell
        v-for="selectedClient in row.isMainParticipant ? [row.relatedClient] : [row.client]"
        :key="selectedClient.client.code"
      >
        <span>
          <template v-if="row.isMainParticipant">
            <a
              :href="`/clients/details/${selectedClient.client.code}`"
              target="_blank"
              rel="noopener"
              >{{ selectedClient.client.code }}</a
            >,
          </template>
          {{ toTitleCase(selectedClient.client.name) }}
          <template v-if="row.isMainParticipant">
            <br />
            <a
              :href="`/clients/details/${selectedClient.client.code}`"
              target="_blank"
              rel="noopener"
            >
              {{ formatLocation(selectedClient.location.code, selectedClient.location.name) }}
            </a>
          </template>
        </span>
      </cds-table-cell>
      <cds-table-cell>
        <span>{{ row.percentageOwnership ? `${row.percentageOwnership}%` : "-" }}</span>
      </cds-table-cell>
      <cds-table-cell>
        <span>{{ booleanToYesNo(row.hasSigningAuthority) || "-" }}</span>
      </cds-table-cell>
      <cds-table-cell v-if="canEdit" class="no-padding">
        <div class="gap-0_5-rem">
          <cds-tooltip align="top-right">
            <cds-button
              :id="`location-${locationIndex}-row-${row.index}-EditBtn`"
              kind="ghost"
              :disabled="!row.isMainParticipant"
              @click="edit"
            >
              <Edit16 slot="icon" />
            </cds-button>
            <cds-tooltip-content v-show="!row.isMainParticipant" autoalign>
              Go to “{{ row.relatedClient.client.code }},
              {{ toTitleCase(row.relatedClient.client.name) }}” client’s page to edit this
              relationship
            </cds-tooltip-content>
          </cds-tooltip>
          <cds-tooltip align="top-right">
            <cds-button
              :id="`location-${locationIndex}-row-${row.index}-DeleteBtn`"
              kind="ghost"
              class="svg-danger"
              @click="handleDelete"
              :disabled="!row.isMainParticipant"
            >
              <TrashCan16 slot="icon" />
            </cds-button>
            <cds-tooltip-content v-show="!row.isMainParticipant">
              Go to “{{ row.relatedClient.client.code }},
              {{ toTitleCase(row.relatedClient.client.name) }}” client’s page to delete this
              relationship
            </cds-tooltip-content>
          </cds-tooltip>
        </div>
      </cds-table-cell>
    </template>
    <td v-else class="table-form-container" :colspan="CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT">
      <client-relationship-form
        ref="formRef"
        :location-index="locationIndex"
        :index="String(row.index)"
        :data="row"
        :clientData="clientData"
        :validations="props.validations"
        keep-scroll-bottom-position
        @canceled="cancel"
        @save="save"
        @delete="handleDelete"
      />
    </td>
  </cds-table-row>
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
        >Are you sure you want to delete this client relationship with "{{
          formatRelatedClient(
            props.row.relatedClient.client.code,
            props.row.relatedClient.client.name,
          )
        }}"?
      </cds-modal-heading>
    </cds-modal-header>

    <cds-modal-body id="modal-delete-body"></cds-modal-body>

    <cds-modal-footer>
      <cds-modal-footer-button
        kind="secondary"
        data-modal-close
        class="cds--modal-close-btn"
        :disabled="localSaving"
      >
        Cancel
      </cds-modal-footer-button>

      <cds-modal-footer-button
        kind="danger"
        class="cds--modal-submit-btn"
        v-on:click="confirmDeleteRelatedClient"
        :danger-descriptor="`Delete client relationship with &quot;{{
          formatRelatedClient(
            props.row.relatedClient.client.code,
            props.row.relatedClient.client.name,
          )
        }}&quot;`"
        :disabled="localSaving"
      >
        Delete client relationship
        <Trash16 slot="icon" />
      </cds-modal-footer-button>
    </cds-modal-footer>
  </cds-modal>
</template>
