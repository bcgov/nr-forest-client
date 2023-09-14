<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
// Carbon
import '@carbon/web-components/es/components/button/index';
// Types
import { nodeEnv, appVersion } from '@/CoreConstants';
// @ts-ignore
import Logout16 from '@carbon/icons-vue/es/logout/16';

const envPrefix = "openshift-";
const env = ref(nodeEnv);
env.value = env.value.slice(envPrefix.length);
env.value = env.value.charAt(0).toUpperCase() + env.value.slice(1);

const screenWidth = ref(window.outerWidth);
const updateScreenWidth = () => (screenWidth.value = window.outerWidth);

// Use computed property to determine if it's a small screen
const isSmallScreen = computed(() => screenWidth.value <= 320);
const isMediumScreen = computed(() => screenWidth.value <= 671);

onMounted(() => window.addEventListener('resize', updateScreenWidth));
</script>

<template>

  <a href="https://gov.bc.ca" v-if="$session.user?.provider !== 'idir'">
    <img
      src="/img/logo1.svg"
      alt="Go to the Government of British Columbia website"
      v-if="isSmallScreen"
    />
    <img
      src="/img/bc_for_logo.png"
      alt="Go to the Government of British Columbia website"
      v-else
    />
  </a>
  
  <div class="heading">
    <span class="heading-compact-01" v-if="$session?.user?.provider !== 'idir'">Ministry of Forests</span>
    <span class="heading-compact-01" v-else>Client Management System</span>
    <span class="heading-compact-01" v-if="env !== 'Prod'">Env. {{ env }} - Rel. {{appVersion}}</span>
  </div>
  
  <div class="heading-space"></div>

  <div class="heading-buttons">

    <!-- Remember to add profile button here -->
    <cds-button
      v-if="$session?.isLoggedIn() && (isSmallScreen || isMediumScreen)"
      data-id="logout-btn"
      kind="tertiary"
      size="sm"
      @click.prevent="$session?.logOut"
    >
      <Logout16 slot="icon" />
    </cds-button>

    <cds-button
      v-if="$session?.isLoggedIn() && (!isSmallScreen && !isMediumScreen)"
      data-id="logout-btn"
      kind="tertiary"
      @click.prevent="$session?.logOut"
    >
      <span>Logout</span>
      <Logout16 slot="icon" />
    </cds-button>
  </div>
</template>
