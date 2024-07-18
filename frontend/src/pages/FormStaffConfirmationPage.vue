<script setup lang="ts">
// Composables
import { useRouter } from "vue-router";
import useSvg from "@/composables/useSvg";

// @ts-ignore
import badgePictogram from "@carbon/pictograms/es/badge";

const router = useRouter();

const createNewClient = () => {
  router.push({ name: "staff-form" });
};

const SVG = useSvg(badgePictogram);
</script>

<template>
  <div class="frame-03" role="status">
    <SVG alt="Badge pictogram" class="submission-badge" role="presentation"></SVG>
    <div class="form-header form-header-application-submitted">
      <h1 class="fluid-heading-04">New client {{ $session.user?.clientNumber }} has been created!</h1>
      <p class="fluid-paragraph-01">
        We’ll send the client number and details submitted to <strong>{{ $session.user?.email }}</strong>
      </p>
      <cds-button
        kind="primary"
        @click.prevent="createNewClient()"
        size="field">
        <span>Create another client</span>
      </cds-button>
    </div>
  </div>
</template>
