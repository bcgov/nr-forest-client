<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/button/index";
// Composables
import { useRouter } from "vue-router";
// Constants
import { backendUrl } from "@/CoreConstants";
// Assets
import landingImagePath from "@/assets/images/pexels-james-wheeler-1544935.jpg";
import logo from "@/assets/images/bc-gov-logo.png";
// @ts-ignore
import login16 from "@carbon/icons-vue/es/login/16";

// extract the querystring parameters from the URL
const router = useRouter();
const { query } = router.currentRoute.value;
// check if a querystring parameter called ref exists and if it has a value of external
if (query.ref && query.ref === "external") {
  window.location.href = `${backendUrl}/login?code=bceidbusiness`;
}
</script>

<template>  
  <div class="login-content">

    <img 
      :src="logo" 
      alt="Government of British Columbia Logo" 
      />

    <h1 id="landing-title" class="landing-title">Client Management System</h1>
    
    <h2 id="landing-subtitle" class="landing-subtitle">Create and manage client accounts</h2>
  
    <div class="spacing"></div>
  
    <div class="form-footer-group-buttons">
    <cds-button
      kind="primary"
      iconLayout=""
      class="landing-button"
      :href="$backend+'/login?code=idir'"
    >
      <span>Log in with IDIR</span>
      <login16 slot="icon" />
    </cds-button>
    <cds-button
      v-if="$features.BCSC_LOGIN"
      kind="primary"
      iconLayout=""
      class="landing-button"
      :href="$backend+'/login?code=bcsc'"
    >
      <span>Log in with BC Services Card</span>
      <login16 slot="icon" />
    </cds-button>
    <cds-button
      v-if="$features.BCEID_LOGIN"
      kind="primary"
      iconLayout=""
      class="landing-button"
      :href="$backend+'/login?code=bceidbusiness'"
    >
      <span>Log in with BCeID</span>
      <login16 slot="icon" />
    </cds-button>
    </div>
  </div>

  <div class="login-content">
    <img
      :src="landingImagePath"
      alt="Small green seedling on the dirt and watered"
    />
  </div>
</template>

<style lang="scss">
@import '@bcgov-nr/nr-fsa-theme/style-sheets/landing-page-components-overrides.scss';

.login-content:first-child {
  padding: 2.5rem;
  flex-basis: 60%;
  box-sizing: border-box;
}
.login-content:last-child {
  overflow-y: hidden;
  flex-basis: 40%;
}
.login-content:first-child img {
  width: 10.5rem;
}
.login-content:last-child img {
  height: 100%;
  width: 100%;
  object-fit: cover;
  object-position: 50% 0;
}

.form-footer-group-buttons {
  flex-wrap: wrap;
}

/* Small (up to 671px) */
@media screen and (max-width: 671px) {
  .login-content:first-child {
    padding: 1rem;
  }
  .login-content:first-child {
    flex: 0;
  }
  .login-content:last-child {
    flex: auto;
  }

  .login-content .form-footer-group-buttons {
    flex-direction: column;
  }
}

/* Medium (from 672px to 1055px) */
@media screen and (min-width: 672px) and (max-width: 1055px) {
  .login-content:first-child {
    padding: 2rem;
  }
  .login-content:first-child {
    flex: 0;
  }
  .login-content:last-child {
    flex: auto;
  }

  .login-content .form-footer-group-buttons {
    flex-direction: column;
  }
}
</style>
