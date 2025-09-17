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
  formatLocation,
  includesAnyOf,
  toTitleCase,
} from "@/services/ForestClientService";
import Edit16 from "@carbon/icons-vue/es/edit/16";
import TrashCan16 from "@carbon/icons-vue/es/trash-can/16";

import ClientRelationshipForm from "@/pages/client-details/ClientRelationshipForm.vue";
import {
  CLIENT_RELATIONSHIPS_EDIT_COLUMN_COUNT,
  type OperateRelatedClient,
  type SaveableComponent,
} from "./shared";

const props = defineProps<{
  row: IndexedRelatedClient;
  clientData: ClientDetails;
  locationIndex: string;
  userRoles: UserRole[];
  isReloading: boolean;
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

const setSaving = (value: boolean) => {
  formRef.value.setSaving(value);
};

const thisSaveableComponent: SaveableComponent = { setSaving, lockEditing };
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
        <div class="gap-0_5-rem">
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
        :validations="[]"
        keep-scroll-bottom-position
        @canceled="cancel"
        @save="save"
      />
    </td>
  </cds-table-row>
</template>
