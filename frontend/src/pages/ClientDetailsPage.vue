<script setup lang="ts">
import { computed, ref } from "vue";
import { AxiosError } from "axios";

// Carbon
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/tabs/index";
import "@carbon/web-components/es/components/tag/index";
import "@carbon/web-components/es/components/accordion/index";

// Composables
import { useFetchTo } from "@/composables/useFetch";
import { useRouter } from "vue-router";

import Location16 from "@carbon/icons-vue/es/location/16";
import User16 from "@carbon/icons-vue/es/user/16";
import NetworkEnterprise16 from "@carbon/icons-vue/es/network--enterprise/16";
import RecentlyViewed16 from "@carbon/icons-vue/es/recently-viewed/16";
import LocationStar20 from "@carbon/icons-vue/es/location--star/20";
import Location20 from "@carbon/icons-vue/es/location/20";

import { adminEmail, getObfuscatedEmailLink, toTitleCase } from "@/services/ForestClientService";

import type { ClientDetails, ClientLocation } from "@/dto/CommonTypesDto";

// Page components
import SummaryView from "@/pages/client-details/SummaryView.vue";
import LocationView from "@/pages/client-details/LocationView.vue";

//Route related
const router = useRouter();
const clientNumber = router.currentRoute.value.params.id;

const data = ref<ClientDetails>(undefined);

const { error: fetchError } = useFetchTo(`/api/clients/details/${clientNumber}`, data);

const clientFullName = computed(() => {
  if (data.value) {
    const { legalFirstName, legalMiddleName, clientName } = data.value;
    const rawParts = [legalFirstName, legalMiddleName, clientName];
    const populatedParts = [];
    for (const part of rawParts) {
      if (part) {
        populatedParts.push(part);
      }
    }
    const fullClientName = populatedParts.join(" ");
    return fullClientName;
  }
  return "";
});

const formatCount = (count = 0) => {
  return String(count).padStart(2, "0");
};

const formatAddress = (location: ClientLocation) => {
  const { addressOne, city, province, country, postalCode } = location;
  const list = [addressOne, city, province, country, postalCode];
  return list.join(", ");
};
</script>

<template>
  <div id="screen" class="client-details-screen">
    <div class="client-details-content">
      <div class="resource-header">
        <cds-breadcrumb>
          <cds-breadcrumb-item>
            <cds-breadcrumb-link href="/search">Client search</cds-breadcrumb-link>
          </cds-breadcrumb-item>
        </cds-breadcrumb>

        <h1 class="resource-details--title">
          <span>
            {{ toTitleCase(clientFullName) }}
          </span>
        </h1>
        <div>
          <p class="body-02 light-theme-text-text-secondary" data-testid="subtitle">
            Check and manage this client data
          </p>
        </div>
      </div>

      <div class="hide-when-less-than-two-children">
        <!--
          This div is necessary to avoid the div.header-offset below from interfering in the flex flow.
        -->
        <div data-scroll="top-notification" class="header-offset"></div>
        <cds-actionable-notification
          v-if="[AxiosError.ERR_BAD_RESPONSE, AxiosError.ERR_NETWORK].includes(fetchError.code)"
          id="internalServerError"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="Something went wrong:"
        >
          <div>
            We're working to fix a problem with our network. Please try again later. If this error
            persists, please email
            <span v-dompurify-html="getObfuscatedEmailLink(adminEmail)"></span> for help.
          </div>
        </cds-actionable-notification>
        <cds-actionable-notification
          v-else-if="fetchError.code === AxiosError.ERR_BAD_REQUEST"
          id="badRequestError"
          v-shadow="true"
          low-contrast="true"
          hide-close-button="true"
          open="true"
          kind="error"
          title="Something went wrong:"
        >
          <div>
            There seems to be a problem with the information you entered. Please double-check and
            try again.
          </div>
        </cds-actionable-notification>
      </div>

      <div class="grouping-14" v-if="data">
        <div class="grouping-05-short">
          <div>
            <h2 class="mg-tl-2 heading-06">Client summary</h2>

            <div class="grouping-10">
              <summary-view :data="data" />
            </div>
          </div>
        </div>

        <cds-tabs value="locations" type="contained">
          <cds-tab id="tab-locations" target="panel-locations" value="locations">
            <div>
              Client locations
              <Location16 />
            </div>
          </cds-tab>
          <cds-tab id="tab-contacts" target="panel-contacts" value="contacts">
            <div>
              Client contacts
              <User16 />
            </div>
          </cds-tab>
          <cds-tab id="tab-related" target="panel-related" value="related">
            <div>
              Related clients
              <NetworkEnterprise16 />
            </div>
          </cds-tab>
          <cds-tab id="tab-activity" target="panel-activity" value="activity">
            <div>
              Activity log
              <RecentlyViewed16 />
            </div>
          </cds-tab>
        </cds-tabs>
      </div>
    </div>
    <div class="tab-panel" v-if="data">
      <div id="panel-locations" role="tabpanel" aria-labelledby="tab-locations" hidden>
        <h3 class="padding-left-1rem">{{ formatCount(data.addresses?.length) }} locations</h3>
        <cds-accordion v-for="(location, index) in data.addresses" :key="location.clientLocnCode">
          <cds-accordion-item size="lg" class="grouping-13">
            <div slot="title" class="flex-column-0_25rem">
              <span class="label-with-icon">
                <LocationStar20 v-if="index === 0" />
                <Location20 v-else />
                {{ location.clientLocnCode }} - {{ location.clientLocnName }}
                <cds-tag
                  :id="`location-${location.clientLocnCode}-deactivated`"
                  v-if="location.locnExpiredInd === 'Y'"
                  type="purple"
                  title=""
                >
                  <span>Deactivated</span>
                </cds-tag>
              </span>
              <span class="hide-open body-compact-01 padding-left-1_625rem">
                {{ formatAddress(location) }}
              </span>
            </div>
            <location-view :data="location" />
          </cds-accordion-item>
        </cds-accordion>
      </div>
      <div id="panel-contacts" role="tabpanel" aria-labelledby="tab-contacts" hidden></div>
      <div id="panel-related" role="tabpanel" aria-labelledby="tab-related" hidden></div>
      <div id="panel-activity" role="tabpanel" aria-labelledby="tab-activity" hidden></div>
    </div>
  </div>
</template>
