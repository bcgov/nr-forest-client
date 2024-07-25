<script setup lang="ts">
// Composables
import useSvg from "@/composables/useSvg";
// @ts-ignore
import badgePictogram from "@carbon/pictograms/es/badge";

defineProps<{  
  submissionId: number;
  clientEmail: string;
}>();
const SVG = useSvg(badgePictogram);

</script>

<template>
  <div class="frame-03" role="status">
    <SVG alt="Badge pictogram" class="submission-badge" role="presentation"></SVG>
    <div class="form-header form-header-application-submitted">
      <h1 class="fluid-heading-04">This submission is being processed</h1>
      <p class="fluid-paragraph-01">
        We’ll send the client number and submission details to <strong>{{ clientEmail }}</strong> when the process is complete.
      </p>
      <div class="form-footer-group-buttons">
        <cds-button
          kind="tertiary"
          :href="`/submissions/${submissionId}`"
          size="field">
          <span>Track this submission</span>
        </cds-button>
        <cds-button
          kind="primary"
          href="/new-client-staff"
          size="field">
          <span>Create another client</span>
        </cds-button>
      </div>
    </div>
  </div>
</template>
