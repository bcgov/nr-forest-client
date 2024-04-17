<script setup lang="ts">
import { useLocalStorage } from "@vueuse/core";
import { checkEnv, frontendUrl } from "@/CoreConstants";
import landingImagePath from "@/assets/images/pexels-james-wheeler-1544935.jpg";
import logo from "@/assets/images/bc-gov-logo.png";

const userProviderInfo = useLocalStorage("userProviderInfo", "");

/**
 * Logs out the user and redirects them to the specified logout URL.
 * 
 * @param {string} userProviderInfo - The user provider information.
 */
const logout = (userProvider: string) => {
  const redirectUrl = checkEnv(`VITE_LOGOUT_${userProvider}_URL`.toUpperCase());
  if (redirectUrl) {
    userProviderInfo.value = "";
    window.location.href = redirectUrl;
  } 
  else {
    userProviderInfo.value = "";
    window.location.href = frontendUrl;
  }
};

logout(userProviderInfo.value);
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
  
    <div>
      <h1>Logging you out...</h1>
    </div>

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

