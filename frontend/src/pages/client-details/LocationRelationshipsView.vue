<script setup lang="ts">
import { computed } from "vue";
import {
  type OtherRelatedClientEntry,
  type ClientLocation,
  type RelatedClientEntry,
  type UserRole,
} from "@/dto/CommonTypesDto";
import {
  booleanToYesNo,
  compareAny,
  formatAddress,
  formatLocation,
  includesAnyOf,
  toTitleCase,
} from "@/services/ForestClientService";

const props = defineProps<{
  data: RelatedClientEntry[];
  location: ClientLocation;
  userRoles: UserRole[];
  isReloading: boolean;
  createMode?: boolean;
}>();

const indexString = props.location.clientLocnCode;

const sortedData = computed<OtherRelatedClientEntry[]>(() => {
  const normalizedData = props.data.map((entry) => {
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
      -compareAny(a.isMainParticipant, b.isMainParticipant) ||
      compareAny(a.otherClient.client.name, b.otherClient.client.name) ||
      compareAny(a.otherClient.location.name, b.otherClient.location.name),
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
      <read-only-component
        label="Mailing address"
        :id="`location-relationships-${indexString}-mailingAddress`"
      >
        <a>
          <span class="body-compact-01 colorless">{{ formatAddress(props.location) }}</span>
        </a>
      </read-only-component>
      <cds-table id="relatioships-table" :class="{ 'view-only': !canEdit }" use-zebra-styles>
        <cds-table-head>
          <cds-table-header-row>
            <cds-table-header-cell class="col-padding-8-px" />
            <cds-table-header-cell class="col-248-px">Relationship type</cds-table-header-cell>
            <cds-table-header-cell class="col-488-px">
              Related client location
            </cds-table-header-cell>
            <cds-table-header-cell class="col-88-px">Percentage owned</cds-table-header-cell>
            <cds-table-header-cell class="col-88-px">Signing authority</cds-table-header-cell>
            <cds-table-header-cell v-if="canEdit" class="col-88-px">
              Actions
            </cds-table-header-cell>
            <cds-table-header-cell class="col-padding-16-px" />
          </cds-table-header-row>
        </cds-table-head>
        <cds-table-body>
          <cds-table-row v-for="row in sortedData" :key="row">
            <cds-table-cell />
            <cds-table-cell>
              <div class="gap-0_5-rem">
                <span>{{ row.relationship.name }}</span>
                <cds-tag type="purple" title="" v-if="row.isMainParticipant">
                  <span>Primary</span>
                </cds-tag>
              </div>
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
                , {{ toTitleCase(row.otherClient.client.name) }}
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
              <span>{{ row.percentageOwnership ? `${row.percentageOwnership}%` : "-" }}</span>
            </cds-table-cell>
            <cds-table-cell>
              <span>{{ booleanToYesNo(row.hasSigningAuthority) || "-" }}</span>
            </cds-table-cell>
            <cds-table-cell v-if="canEdit">
              <!-- TODO: Actions column contents -->
            </cds-table-cell>
            <cds-table-cell />
          </cds-table-row>
        </cds-table-body>
      </cds-table>
    </template>
  </div>
</template>
