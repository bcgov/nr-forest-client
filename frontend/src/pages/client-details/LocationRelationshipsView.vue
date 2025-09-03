<script setup lang="ts">
import { computed } from "vue";
import {
  type ClientLocation,
  type IndexedRelatedClient,
  type RelatedClientEntry,
  type UserRole,
} from "@/dto/CommonTypesDto";
import {
  booleanToYesNo,
  compareAny,
  formatLocation,
  includesAnyOf,
  toTitleCase,
} from "@/services/ForestClientService";
import Edit16 from "@carbon/icons-vue/es/edit/16";
import TrashCan16 from "@carbon/icons-vue/es/trash-can/16";

const props = defineProps<{
  data: RelatedClientEntry[];
  location: ClientLocation;
  userRoles: UserRole[];
  isReloading: boolean;
  createMode?: boolean;
}>();

const locationIndex = props.location?.clientLocnCode || null;

const sortedData = computed<RelatedClientEntry[]>(() => {
  const indexedData = props.data.map((entry, index) => {
    const result: IndexedRelatedClient = {
      ...entry,
      index,
      originalLocation: { ...entry.client.location },
    };
    return result;
  });

  const result = indexedData.toSorted(
    (a, b) =>
      compareAny(a.relationship?.name, b.relationship?.name) ||
      -compareAny(a.isMainParticipant, b.isMainParticipant) ||
      compareAny(a.relatedClient.client.name, b.relatedClient.client.name) ||
      compareAny(a.relatedClient.location.name, b.relatedClient.location.name),
  );
  return result;
});

const canEdit = computed(() =>
  includesAnyOf(props.userRoles, ["CLIENT_ADMIN", "CLIENT_SUSPEND", "CLIENT_EDITOR"]),
);
</script>

<template>
  <div class="grouping-12" :class="{ invisible: props.isReloading }">
    <template v-if="!props.createMode">
      <cds-table id="relatioships-table" :class="{ 'view-only': !canEdit }" use-zebra-styles>
        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell class="col-padding-10-px" />
            <cds-table-header-cell class="col-310-px">Primary client</cds-table-header-cell>
            <cds-table-header-cell class="col-210-px">Relationship type</cds-table-header-cell>
            <cds-table-header-cell class="col-310-px">Related client</cds-table-header-cell>
            <cds-table-header-cell class="col-120-px">Percentage owned</cds-table-header-cell>
            <cds-table-header-cell class="col-120-px">Signing authority</cds-table-header-cell>
            <cds-table-header-cell v-if="canEdit" class="col-104-px-fixed">
              Actions
            </cds-table-header-cell>
          </cds-table-header-row>
        </cds-table-head>
        <cds-table-body>
          <cds-table-row v-for="(row, rowIndex) in sortedData" :key="row">
            <cds-table-cell class="no-padding" />
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
                    :id="`location-${locationIndex}-row-${rowIndex}-EditBtn`"
                    kind="ghost"
                    :disabled="!row.isMainParticipant"
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
                    :id="`location-${locationIndex}-row-${rowIndex}-DeleteBtn`"
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
          </cds-table-row>
        </cds-table-body>
      </cds-table>
    </template>
  </div>
</template>
