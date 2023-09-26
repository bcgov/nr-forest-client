<script setup lang="ts">
import { inject } from 'vue'
// Composed
import { useRouter } from 'vue-router'
import useSvg from '@/composables/useSvg'
// Carbon
import '@carbon/web-components/es/components/button/index';
// Types
import type { Submitter } from "@/dto/CommonTypesDto";
// @ts-ignore
import badgePictogram from "@carbon/pictograms/es/badge";

const submitterInformation = inject<Submitter>("submitterInformation");

const router = useRouter()
const SVG = useSvg(badgePictogram)

const newFormSubmission = () => router.push({ name: 'form' })

</script>

<template>
  <div class="frame-03">
    <SVG alt="Badge pictogram" class="submission-badge"></SVG>    
    <div class="form-header form-header-stretched">
      <p class="fluid-heading-05">Application submitted!</p>
      <p class="fluid-paragraph-01">Your application for a client number has been submitted.</p>
      <p class="fluid-paragraph-01">
        We’ll send the client number to {{ $session.user?.email }} once
        we confirm the information you provided.
      </p>
      <cds-button
            kind="primary"            
            @click.prevent="newFormSubmission()">
          <span>Create another client</span>
        </cds-button>
    </div>
  </div>
</template>