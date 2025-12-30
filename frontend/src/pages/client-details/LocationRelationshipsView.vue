<script setup lang="ts">
import { computed } from "vue";
import {
  type ClientDetails,
  type ClientLocation,
  type IndexedRelatedClient,
  type RelatedClientEntry,
  type UserRole,
} from "@/dto/CommonTypesDto";
import { compareAny, includesAnyOf } from "@/services/ForestClientService";

// Page components
import ClientRelationshipRow from "@/pages/client-details/ClientRelationshipRow.vue";
import { useMediaQuery } from "@vueuse/core";

const props = defineProps<{
  data: RelatedClientEntry[];
  clientData: ClientDetails;
  location: ClientLocation;
  validations: Array<Function>;
  userRoles: UserRole[];
  isReloading: boolean;
  isPrinting: boolean;
}>();

const locationIndex = props.location?.clientLocnCode || null;

const sortedData = computed<IndexedRelatedClient[]>(() => {
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

const showActionsColumn = computed(() => canEdit.value && !props.isPrinting);
</script>

<template>
  <div class="grouping-12" :class="{ invisible: props.isReloading }">
    <cds-table
      id="relatioships-table"
      :class="{ 'view-only': !showActionsColumn }"
      use-zebra-styles
    >
      <cds-table-head>
        <cds-table-header-row>
          <cds-table-header-cell class="col-padding-10-px" />
          <cds-table-header-cell class="col-310-px">Primary client</cds-table-header-cell>
          <cds-table-header-cell class="col-210-px">Relationship type</cds-table-header-cell>
          <cds-table-header-cell class="col-310-px">Related client</cds-table-header-cell>
          <cds-table-header-cell class="col-120-px">Percentage owned</cds-table-header-cell>
          <cds-table-header-cell class="col-120-px">Signing authority</cds-table-header-cell>
          <cds-table-header-cell v-if="showActionsColumn" class="col-104-px-fixed">
            Actions
          </cds-table-header-cell>
        </cds-table-header-row>
      </cds-table-head>
      <cds-table-body>
        <template v-for="row in sortedData" :key="row">
          <client-relationship-row
            :row
            :client-data="props.clientData"
            :location-index="locationIndex"
            :validations="props.validations"
            :user-roles="props.userRoles"
            :is-printing="props.isPrinting"
          />
        </template>
      </cds-table-body>
    </cds-table>
  </div>
</template>
