<template>
  <div class="submission-head">
    <badge32 class="submission-badge" />
    <div class="submission-text">
      <h1>Application submitted!</h1>
      <p>Your application for a client number has been submitted.</p>
      <p>We’ll send the client number to {{submitterInformation?.email}} once we confirm the information you provided.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject } from "vue";
import badge32 from "@carbon/icons-vue/es/badge/32";
import type { Submitter } from "@/core/CommonTypes";


const submitterInformation = inject<Submitter>("submitterInformation");

</script>

<style scoped></style>
