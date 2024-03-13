<script setup lang="ts">
import { useLocalStorage } from "@vueuse/core";
import { checkEnv } from "@/CoreConstants"

const userProviderInfo = useLocalStorage("userProviderInfo", "");

/**
 * Logs out the user and redirects them to the specified logout URL.
 * 
 * @param {string} userProviderInfo - The user provider information.
 */
const logout = (userProvider: string) => {
  const redirectUrl = checkEnv(`VITE_LOGOUT_${userProvider}_URL`.toUpperCase());
  userProviderInfo.value = "";
  window.location.href = redirectUrl;
};

logout(userProviderInfo.value)


</script>
<template>
  <div>
    <h1>Logging you out</h1>
  </div>
</template>
