<script setup lang="ts">
import { useRouter } from 'vue-router'
import login16 from '@carbon/icons-vue/es/login/16'
import Seeding from '@/assets/images/seeding.png'
import logo from '@/assets/images/bc-gov-logo.png'
import AmplifyUserSession from '@/helpers/AmplifyUserSession'

// extract the querystring parameters from the URL
const router = useRouter()
const { query } = router.currentRoute.value

// check if a querystring parameter called ref exists and if it has a value of external
if (query.ref && query.ref === 'external') {
  AmplifyUserSession.logIn('bceidbusiness')
}
</script>

<template>
  <div class="full-width">
    <div class="row bx--row landing-grid">
      <div class="bx--col-sm-8 bx--col-md-10 bx--col-lg-10">
        <img :src="logo" alt="BCGov Logo" width="160" class="logo" />
        <h1 id="landing-title" class="landing-title">Welcome to CLIENT</h1>
        <h2 id="landing-subtitle" class="landing-subtitle">
          The Ministry of Forests' client management system
        </h2>

        <p id="landing-desc" class="landing-desc">
          Check and manage client submissions
        </p>

        <bx-btn
          kind="primary"
          iconLayout=""
          class="landing-button"
          @click.prevent="$session?.logIn('idir')"
        >
          <span>Login with IDIR</span>
          <login16 slot="icon" />
        </bx-btn>
      </div>
      <div class="bx--col-sm-8 bx--col-md-6 bx--col-lg-6 landing-img-column">
        <img
          :src="Seeding"
          alt="Small green seedling on the dirt and watered"
          class="landing-img"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss">
@import '@bcgov-nr/nr-fsa-theme/style-sheets/landing-page-components-overrides.scss';
</style>
