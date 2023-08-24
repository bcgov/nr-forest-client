<template>

    <a href="https://gov.bc.ca">
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

    <!-- Remember to add profile button here -->
    <bx-btn
      v-if="$session?.isLoggedIn() && (isSmallScreen || isMediumScreen)"
      data-id="logout-btn"
      kind="tertiary"
      iconLayout=""
      class="bx--btn bx--btn-header bx--btn-reset"
      @click.prevent="$session?.logOut"
      size="field"
    >
      <Logout16 slot="icon" />
    </bx-btn>

    <bx-btn
      v-if="$session?.isLoggedIn() && (!isSmallScreen && !isMediumScreen)"
      data-id="logout-btn"
      kind="tertiary"
      iconLayout=""
      class="bx--btn bx--btn-header bx--btn-reset"
      @click.prevent="$session?.logOut"
      size="field"
    >
      <span>Logout</span>
      <Logout16 slot="icon" />
    </bx-btn>

</template>

<script setup lang="ts">
import Logout16 from '@carbon/icons-vue/es/logout/16';
import { nodeEnv, appVersion } from '@/CoreConstants';
import { ref, computed, onMounted } from 'vue';

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
