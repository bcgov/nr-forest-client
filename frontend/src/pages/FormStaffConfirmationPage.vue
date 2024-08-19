<script setup lang="ts">
// Composables
import useSvg from "@/composables/useSvg";
// @ts-ignore
import badgePictogram from "@carbon/pictograms/es/badge";

defineProps<{
  clientNumber: string;
  clientEmail: string;
}>();
const SVG = useSvg(badgePictogram);

</script>

<template>
  <div class="frame-03 staff" role="status">
    <SVG alt="Badge pictogram" class="submission-badge" role="presentation"></SVG>
    <div class="form-header form-header-application-submitted">
      <h1 class="fluid-heading-04">New client {{ clientNumber }} has been created!</h1>
      <p class="fluid-paragraph-01">
        We’ll send the client number and details submitted to <strong>{{ clientEmail }}</strong>
      </p>
      <cds-button
        kind="primary"
        href="/new-client-staff"
        size="field">
        <span>Create another client</span>
      </cds-button>
    </div>
  </div>
</template>
