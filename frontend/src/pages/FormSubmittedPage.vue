<script setup lang="ts">
// Composables
import { useRouter } from "vue-router";
import useSvg from "@/composables/useSvg";

// @ts-ignore
import badgePictogram from "@carbon/pictograms/es/badge";

const router = useRouter();

const newFormSubmission = () => {
  router.push({ name: "form" });
};

const SVG = useSvg(badgePictogram);
</script>

<template>
  <div class="frame-03">
    <SVG alt="Badge pictogram" class="submission-badge"></SVG>    
    <div class="form-header form-header-application-submitted">
      <p class="fluid-heading-05">Application submitted!</p>
      <p class="fluid-paragraph-01">Your application for a client number has been submitted.</p>
      <p class="fluid-paragraph-01">&nbsp;</p>
      <p class="fluid-paragraph-01">
        We’ll send the client number to <strong>{{ $session.user?.email }}</strong> once
        we confirm the information you provided.
      </p>
      <cds-button
        v-if="!$features.BCSC_LOGIN"
        kind="primary"
        @click.prevent="newFormSubmission()"
        size="field">
        <span>Create another client</span>
      </cds-button>
    </div>
  </div>
</template>
