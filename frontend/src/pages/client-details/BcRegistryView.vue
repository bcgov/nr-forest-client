<script setup lang="ts">
import { ref, computed } from 'vue';
import { useFetchTo } from '@/composables/useFetch';
import ToolBox20 from "@carbon/icons-vue/es/tool-box/20";
import Friendship20 from "@carbon/icons-vue/es/friendship/20";
import {
  toTitleCase,
  formatDate,
} from "@/services/ForestClientService";
import { retrieveLegalTypeDesc } from "@/helpers/DataConverters";
import type { BcRegistryInformation, BcRegistryParty } from '@/dto/CommonTypesDto';

const props = defineProps<{
  registrationNumber: string;
}>();

const data = ref<BcRegistryInformation[] | null>(null);
const { loading, error } = useFetchTo(
  `/api/clients/${props.registrationNumber}/bc-registry-information`,
  data,
  { skipDefaultErrorHandling: true }
);

const bcRegistryInfo = computed<BcRegistryInformation | null>(() => {
  if (!data.value) return null;
  if (Array.isArray(data.value)) return data.value[0] ?? null;
  return data.value;
});

const hasParties = computed(
  () => (bcRegistryInfo.value?.parties?.length ?? 0) > 0
);

const officerName = (party: BcRegistryParty): string => {
  const officer = party.officer;
  if (!officer) return '—';
  const org = officer.organizationName?.trim();
  if (org) return org;
  return [officer.firstName, officer.middleInitial, officer.lastName]
    .filter((p): p is string => !!p && String(p).trim() !== '')
    .map((p) => String(p).trim())
    .join(' ') || '—';
};

const partyAddress = (party: BcRegistryParty): string => {
  const addr = party.mailingAddress;
  if (!addr) return '—';
  const street = addr.streetAddress?.trim() ?? '';
  const line2 = [addr.addressCity, addr.addressRegion, addr.postalCode]
    .filter((p): p is string => !!p && p.trim().length > 0)
    .join(', ');
  return [street, line2].filter(Boolean).join(' ') || '—';
};
</script>

<template>
  <div class="tab-panel tab-panel--empty" v-if="loading">
    <div class="skeleton-group">
      <cds-skeleton-text v-shadow="1" class="heading-03-skeleton" />
      <cds-skeleton-text v-shadow="1" class="heading-03-skeleton" />
      <div></div>
    </div>
  </div>
  
  <div
    class="tab-panel tab-panel--empty"
    v-else-if="error?.message || !bcRegistryInfo"
  >
    <span class="body-compact-01">BC Registry information not available.</span>
  </div>

  <!-- Populated state -->
  <div v-else>
    <div class="tab-header space-between">
      <h3 class="padding-left-1rem">BC Registry information</h3>
    </div>

    <div class="tab-panel tab-panel--populated">

      <!-- Business information accordion -->
      <cds-accordion id="bc-business-information">
        <cds-accordion-item size="lg" class="grouping-13" v-shadow="1">
          <div slot="title" class="flex-column-0_25rem">
            <span class="label-with-icon">
              <ToolBox20 />
              Business information
            </span>
          </div>
          <div id="bc-business-information-general-section" class="grouping-12">
            <div class="flex-column-1_5rem">
              <read-only-component label="Business name" id="businessNameId">
                <span class="body-compact-01">
                  {{ bcRegistryInfo.business?.legalName || bcRegistryInfo.business?.resolvedLegalName || '—' }}
                </span>
              </read-only-component>
              <read-only-component label="Business status" id="businessStatusId">
                <span class="body-compact-01">
                  {{ toTitleCase(bcRegistryInfo.business?.state) || '—' }}
                </span>
              </read-only-component>
              <read-only-component label="Business type" id="businessTypeId">
                <span class="body-compact-01">
                  {{ retrieveLegalTypeDesc(bcRegistryInfo.business?.legalType || '') || '—' }}
                </span>
              </read-only-component>
              <read-only-component label="Registration number" id="registrationNumberId">
                <span class="body-compact-01">
                  {{ bcRegistryInfo.business?.identifier || '—' }}
                </span>
              </read-only-component>
              <read-only-component label="Registration date" id="registrationDateId">
                <span class="body-compact-01">
                  {{ formatDate(bcRegistryInfo.business?.registrationDateTime) || '—' }}
                </span>
              </read-only-component>
              <read-only-component label="Mailing address" id="mailingAddressId">
                <span class="body-compact-01" id="officeStreetAddressId">
                  {{ bcRegistryInfo.offices?.businessOffice?.mailingAddress?.streetAddress || '—' }}
                </span>
                <span class="body-compact-01" id="officeCityId">
                  {{ bcRegistryInfo.offices?.businessOffice?.mailingAddress?.addressCity || '' }}
                  {{ bcRegistryInfo.offices?.businessOffice?.mailingAddress?.addressRegion || '' }}
                  {{ bcRegistryInfo.offices?.businessOffice?.mailingAddress?.postalCode || '' }}
                </span>
                <span class="body-compact-01" id="officeCountryId">
                  {{ bcRegistryInfo.offices?.businessOffice?.mailingAddress?.addressCountry || '' }}
                </span>
              </read-only-component>
              <read-only-component label="Delivery address" id="deliveryAddressId">
                <span class="body-compact-01" id="deliveryStreetAddressId">
                  {{ bcRegistryInfo.offices?.businessOffice?.deliveryAddress?.streetAddress || '—' }}
                </span>
                <span class="body-compact-01" id="deliveryCityId">
                  {{ bcRegistryInfo.offices?.businessOffice?.deliveryAddress?.addressCity || '' }}
                  {{ bcRegistryInfo.offices?.businessOffice?.deliveryAddress?.addressRegion || '' }}
                  {{ bcRegistryInfo.offices?.businessOffice?.deliveryAddress?.postalCode || '' }}
                </span>
                <span class="body-compact-01" id="deliveryCountryId">
                  {{ bcRegistryInfo.offices?.businessOffice?.deliveryAddress?.addressCountry || '' }}
                </span>
              </read-only-component>
            </div>
          </div>
        </cds-accordion-item>
      </cds-accordion>

      <!-- Partner information accordion (only shown when parties exist) -->
      <cds-accordion id="bc-partner-information" v-if="hasParties">
        <cds-accordion-item size="lg" class="grouping-13" v-shadow="1">
          <div slot="title" class="flex-column-0_25rem">
            <span class="label-with-icon">
              <Friendship20 />
              Partner information
            </span>
          </div>
          <div class="grouping-12">
            <cds-table id="partners-table" use-zebra-styles>
              <cds-table-head>
                <cds-table-header-row>
                  <cds-table-header-cell class="col-padding-10-px" />
                  <cds-table-header-cell class="col-280-px">Individual or Company name</cds-table-header-cell>
                  <cds-table-header-cell class="col-500-px">Residential or Registered address</cds-table-header-cell>
                  <cds-table-header-cell class="col-280-px">Identification or Registration Number</cds-table-header-cell>
                  <cds-table-header-cell class="col-280-px">Role type</cds-table-header-cell>
                </cds-table-header-row>
              </cds-table-head>
              <cds-table-body>
                <cds-table-row
                  v-for="(party, idx) in bcRegistryInfo.parties"
                  :key="party.officer?.id ?? idx"
                >
                  <cds-table-cell class="no-padding" />
                  <cds-table-cell>{{ officerName(party) }}</cds-table-cell>
                  <cds-table-cell>{{ partyAddress(party) }}</cds-table-cell>
                  <cds-table-cell>{{ party.officer?.id || '—' }}</cds-table-cell>
                  <cds-table-cell>{{ party.roles?.[0]?.roleType || '—' }}</cds-table-cell>
                </cds-table-row>
              </cds-table-body>
            </cds-table>
          </div>
        </cds-accordion-item>
      </cds-accordion>

    </div>
  </div>
</template>
