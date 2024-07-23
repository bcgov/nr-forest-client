<script setup lang="ts">
// Composables
import useSvg from "@/composables/useSvg";
// @ts-ignore
import badgePictogram from "@carbon/pictograms/es/badge";

defineProps<{  
  submissionId: Number;
}>();
const SVG = useSvg(badgePictogram);

</script>

<template>
  <div class="frame-03" role="status">
    <SVG alt="Badge pictogram" class="submission-badge" role="presentation"></SVG>
    <div class="form-header form-header-application-submitted">
      <h1 class="fluid-heading-04">Submission still being processed!</h1>
      <p class="fluid-paragraph-01">
        We’ll send the client number and details submitted to <strong>{{ clientEmail }}</strong> once the submission is complete.
      </p>
      <cds-button
        kind="secondary"
        :href="`/submissions/${submissionId}`"
        size="field">
        <span>Track submission</span>
      </cds-button>
      <cds-button
        kind="primary"
        href="/new-client-staff"
        size="field">
        <span>Create another client</span>
      </cds-button>
    </div>
  </div>
</template>
