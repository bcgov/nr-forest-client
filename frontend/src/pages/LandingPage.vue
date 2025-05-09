<script setup lang="ts">
// Carbon
import "@carbon/web-components/es/components/button/index";
// Composables
import { useRouter } from "vue-router";
// Session
import ForestClientUserSession from "@/helpers/ForestClientUserSession";
// Assets
import landingImagePath from "@/assets/images/pexels-james-wheeler-1544935.jpg";
import logo from "@/assets/images/bc-gov-logo.png";
// @ts-ignore
import login16 from "@carbon/icons-vue/es/login/16";
import { ref } from "vue";
import { backendUrl, nodeEnv } from "@/CoreConstants";

// extract the querystring parameters from the URL
const router = useRouter();
const { query } = router.currentRoute.value;

let hideIdirBtnInd = false;

// check if a querystring parameter called ref exists and if it has a value of external
if (query.ref && query.ref === "external") {
  ForestClientUserSession.logIn('bceidbusiness');
  hideIdirBtnInd = true;
}
if (query.ref && query.ref === "individual") {
  ForestClientUserSession.logIn('bcsc');
  hideIdirBtnInd = true;
}

const stubRoles = [
  "None",
  "CLIENT_VIEWER",
  "CLIENT_EDITOR",
  "CLIENT_SUSPEND",
  "CLIENT_ADMIN",
  "Multiple",
];
const stubRolesCodeName = stubRoles.map((item) => ({
  code: item,
  name: item,
}));

const selectedStubRole = ref("CLIENT_EDITOR");

const logInStubRole = (role: string) => {
  if (nodeEnv === "test") {
    const host = backendUrl.replace("http://", "https://").replace(":8080", ":8081");
    window.location.href = `${host}/login?stubrole=${role}`;
  }
};

if (query.stubrole) {
  document.cookie = `stubrole=${query.stubrole}`;
  ForestClientUserSession.logIn("idir");
}
</script>

<template>
  <div class="login-content">

    <img 
      :src="logo" 
      alt="Government of British Columbia Logo" 
      />

    <h1 id="landing-title" class="landing-title">Forests Client Management System</h1>

    <h2 id="landing-subtitle" class="landing-subtitle">Create and manage client accounts</h2>
  
    <div class="spacing"></div>
  
    <div class="form-group-buttons" v-if="!hideIdirBtnInd">
      <cds-button
        kind="primary"
        iconLayout=""
        class="landing-button"
        @click.prevent="ForestClientUserSession.logIn('idir')"
      >
        <span>Log in with IDIR</span>
        <login16 slot="icon" />
      </cds-button>
      <cds-button
        v-if="$features.BCSC_LOGIN"
        kind="primary"
        iconLayout=""
        class="landing-button"
        @click.prevent="ForestClientUserSession.logIn('bcsc')"
      >
        <span>Log in with BC Services Card</span>
        <login16 slot="icon" />
      </cds-button>
      <cds-button
        v-if="$features.BCEID_LOGIN"
        kind="primary"
        iconLayout=""
        class="landing-button"
        @click.prevent="ForestClientUserSession.logIn('bceidbusiness')"
      >
        <span>Log in with BCeID</span>
        <login16 slot="icon" />
      </cds-button>
      <div v-if="$features.ROLE_LOGIN" style="width: 100%; display: flex; gap: 1rem">
        <dropdown-input-component
          id="role"
          label="Role"
          :initial-value="selectedStubRole"
          required
          required-label
          :model-value="stubRolesCodeName"
          :enabled="true"
          tip=""
          :validations="[]"
          style="width: 18rem; margin-right: 0.5rem"
          @update:selected-value="
            (selectedValue) => {
              selectedStubRole = selectedValue.code;
            }
          "
        />
        <cds-button
          kind="primary"
          iconLayout=""
          class="landing-button"
          @click.prevent="logInStubRole(selectedStubRole)"
        >
          <span>Log in as Selected Role</span>
          <login16 slot="icon" />
        </cds-button>
      </div>
    </div>
    <div v-else>Redirecting...</div>
  </div>

  <div class="login-content">
    <img
      :src="landingImagePath"
      alt="Small green seedling on dirt and water"
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

.form-group-buttons {
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

  .login-content .form-group-buttons {
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

  .login-content .form-group-buttons {
    flex-direction: column;
  }
}
</style>
