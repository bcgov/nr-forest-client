<script setup lang="ts">
// Composables
import useSvg from "@/composables/useSvg";
// @ts-ignore
import badgePictogram from "@carbon/pictograms/es/badge";
import { greenDomain, featureFlags } from "@/CoreConstants";

defineProps<{
  clientNumber: string;
  clientEmail: string;
  notifyClientInd: string;
}>();
const SVG = useSvg(badgePictogram);

const openClientDetails = (clientNumber: string) => {
  if (clientNumber) {
    const url = featureFlags.STAFF_CLIENT_DETAIL
      ? `/clients/details/${clientNumber}`
      : `https://${greenDomain}/int/client/client02MaintenanceAction.do?bean.clientNumber=${clientNumber}`;

    if (featureFlags.STAFF_CLIENT_DETAIL) {
      window.open(url, "_self");
    } else {
      window.open(url, "_blank", "noopener");
    }
  }
};
</script>

<template>
  <div class="frame-03 staff" role="status">
    <SVG alt="Badge pictogram" class="submission-badge" role="presentation"></SVG>
    <div class="form-header form-header-application-submitted">
      <h1 class="fluid-heading-04">New client {{ clientNumber }} has been created!</h1>
      <p class="fluid-paragraph-01" v-if="notifyClientInd === 'Y'">
        We’ll send the client number and details submitted to <strong>{{ clientEmail }}</strong>
      </p>
      <div class="form-group-buttons">
        <cds-button
          id="openClientDtlsBtnId"
          kind="tertiary"
          v-on:click="openClientDetails(clientNumber)"
          size="field">
          <span>View client details</span>
        </cds-button>
        <cds-button
          id="createAnotherClientBtnId"
          kind="primary"
          href="/new-client-staff"
          size="field">
          <span>Create another client</span>
        </cds-button>
      </div>
    </div>
  </div>
</template>
