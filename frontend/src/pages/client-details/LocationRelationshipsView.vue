<script setup lang="ts">
import { computed } from "vue";
import { type ClientLocation, type RelatedClientEntry, type UserRole } from "@/dto/CommonTypesDto";
import {
  booleanToYesNo,
  compareAny,
  formatAddress,
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
  const result = props.data.toSorted(
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

const formattedAddress = computed(() => {
  return formatAddress(props.location);
});

const encodedAddress = computed(() => {
  return encodeURIComponent(formattedAddress.value);
});
</script>

<template>
  <div class="grouping-12" :class="{ invisible: props.isReloading }">
    <template v-if="!props.createMode">
      <read-only-component
        label="Mailing address"
        :id="`location-relationships-${locationIndex}-mailingAddress`"
      >
        <a
          :href="`https://www.google.com/maps/search/?api=1&query=${encodedAddress}`"
          target="_blank"
          rel="noopener"
        >
          <span class="body-compact-01 colorless">{{ formattedAddress }}</span>
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
          <cds-table-row v-for="(row, rowIndex) in sortedData" :key="row">
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
                  :href="`/clients/details/${row.relatedClient.client.code}`"
                  target="_blank"
                  rel="noopener"
                >
                  {{ row.relatedClient.client.code }}
                </a>
                , {{ toTitleCase(row.relatedClient.client.name) }}
                <a
                  :href="`/clients/details/${row.relatedClient.client.code}`"
                  target="_blank"
                  rel="noopener"
                >
                  {{
                    formatLocation(row.relatedClient.location.code, row.relatedClient.location.name)
                  }}
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
            <cds-table-cell />
          </cds-table-row>
        </cds-table-body>
      </cds-table>
    </template>
  </div>
</template>
