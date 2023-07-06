<template>
  <div class="submission-head">
    <SVG alt="Badge pictogram" class="submission-badge"></SVG>
    <div ref="i"></div>
    <div class="submission-text">
      <h1>Application submitted!</h1>
      <p>Your application for a client number has been submitted.</p>
      <p>
        We’ll send the client number to {{ submitterInformation?.email }} once
        we confirm the information you provided.
      </p>
      <bx-btn
        kind="primary"
        iconLayout=""
        class="bx--btn"
        @click.prevent="newFormSubmission()"
        size="field"
      >
        <span>Create another client</span>
      </bx-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import { useRouter } from 'vue-router'
import badgePictogram from '@carbon/pictograms/es/badge'

import type { Submitter } from '@/dto/CommonTypesDto'
import useSvg from '@/composables/useSvg'

const submitterInformation = inject<Submitter>('submitterInformation')

const router = useRouter()

const newFormSubmission = () => {
  router.push({ name: 'form' })
}

const SVG = useSvg(badgePictogram)
</script>
