<script setup lang="ts">
import { computed, ref } from "vue";

// Carbon
import "@carbon/web-components/es/components/breadcrumb/index";
import "@carbon/web-components/es/components/notification/index";
import "@carbon/web-components/es/components/button/index";
import "@carbon/web-components/es/components/tabs/index";
import "@carbon/web-components/es/components/tag/index";

// Composables
import { useFetchTo } from "@/composables/useFetch";
import { useRouter } from "vue-router";

// @ts-ignore
import Location16 from "@carbon/icons-vue/es/location/16";
// @ts-ignore
import User16 from "@carbon/icons-vue/es/user/16";
// @ts-ignore
import NetworkEnterprise16 from "@carbon/icons-vue/es/network--enterprise/16";
// @ts-ignore
import RecentlyViewed16 from "@carbon/icons-vue/es/recently-viewed/16";
// @ts-ignore
import Check20 from "@carbon/icons-vue/es/checkmark--filled/20";
// @ts-ignore
import Warning20 from "@carbon/icons-vue/es/warning--filled/20";

import {
  adminEmail,
  getObfuscatedEmailLink,
  getTagColorByClientStatus,
  toTitleCase,
} from "@/services/ForestClientService";

import { AxiosError } from "axios";

//Route related
const router = useRouter();
const clientNumber = router.currentRoute.value.params.id;

const data = ref(undefined);

const { error: fetchError } = useFetchTo(`/api/clients/details/${clientNumber}`, data);

const goodStanding = (goodStanding: string): string => {
  if (goodStanding) return goodStanding === "Y" ? "Good standing" : "Not in good standing";
  return "Unknown";
};

const clientFullName = computed(() => {
  if (data.value?.business) {
    const { legalFirstName, legalMiddleName, clientName } = data.value.business;
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

const clientRegistrationNumber = computed(() => {
  const { registryCompanyTypeCode, corpRegnNmbr } = data.value.business;
  if (!registryCompanyTypeCode || !corpRegnNmbr) {
    return undefined;
  }
  return `${registryCompanyTypeCode}${corpRegnNmbr}`;
});
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
              <div class="grouping-10 no-padding">
                <read-only-component label="Client number">
                  <span class="body-compact-01">{{ data.business.clientNumber }}</span>
                </read-only-component>
                <read-only-component label="Acronym" v-if="data.business.clientAcronym">
                  <span class="body-compact-01">{{ data.business.clientAcronym }}</span>
                </read-only-component>
                <read-only-component label="Client type">
                  <span class="body-compact-01">{{ data.business.clientTypeDesc }}</span>
                </read-only-component>
                <read-only-component label="Registration number" v-if="clientRegistrationNumber">
                  <span class="body-compact-01">{{ clientRegistrationNumber }}</span>
                </read-only-component>
                <read-only-component
                  label="BC Registries standing"
                  id="goodStanding"
                  v-if="clientRegistrationNumber"
                >
                  <div class="internal-grouping-01">
                    <span class="body-compact-01 default-typography">{{
                      goodStanding(data.business.goodStandingInd)
                    }}</span>
                    <Check20 v-if="data.business.goodStandingInd === 'Y'" class="good" />
                    <Warning20 v-if="data.business.goodStandingInd !== 'Y'" class="warning" />
                  </div>
                </read-only-component>
                <read-only-component
                  :label="data.business.clientIdTypeDesc"
                  v-if="data.business.clientIdTypeDesc && data.business.clientIdentification"
                >
                  <span class="body-compact-01">{{ data.business.clientIdentification }}</span>
                </read-only-component>
                <read-only-component label="Date of birth" v-if="data.business.birthdate">
                  <span class="body-compact-01">{{ data.business.birthdate }}</span>
                </read-only-component>
                <read-only-component label="Status">
                  <span class="body-compact-01">
                    <cds-tag :type="getTagColorByClientStatus(data.business.clientStatusDesc)">
                      <span>{{ data.business.clientStatusDesc }}</span>
                    </cds-tag>
                  </span>
                </read-only-component>
              </div>
              <div class="grouping-10 no-padding" v-if="data.business.clientComment">
                <read-only-component label="Notes" v-if="data.business.clientComment">
                  <span class="body-compact-01">{{ data.business.clientComment }}</span>
                </read-only-component>
              </div>
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
      <div id="panel-locations" role="tabpanel" aria-labelledby="tab-locations" hidden></div>
      <div id="panel-contacts" role="tabpanel" aria-labelledby="tab-contacts" hidden></div>
      <div id="panel-related" role="tabpanel" aria-labelledby="tab-related" hidden></div>
      <div id="panel-activity" role="tabpanel" aria-labelledby="tab-activity" hidden></div>
    </div>
  </div>
</template>

<style scoped>
.internal-grouping-01 {
  display: flex;
  gap: 0.5rem;
}

.grouping-10 .grouping-11:first-child {
  width: unset;
}
</style>
