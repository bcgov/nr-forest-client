<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
// Carbon
import '@carbon/web-components/es/components/button/index';
// Types
import { nodeEnv, appVersion } from '@/CoreConstants';
// @ts-ignore
import Logout16 from '@carbon/icons-vue/es/logout/16';
import Help16 from '@carbon/icons-vue/es/help/16';

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

const helpModalActive = ref(false);
const logoutModalActive = ref(false);
</script>

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

  <div class="heading-buttons">

    <!-- Remember to add profile button here -->
    
    <cds-button
      id="help-btn"
      data-id="help-btn"
      kind="ghost"
      :size="(isSmallScreen || isMediumScreen) ? 'sm' : 'lg'"
      @click.prevent="helpModalActive = true"
    >
      <span v-if="!isSmallScreen && !isMediumScreen">Help with application</span>
      <Help16 slot="icon" />
    </cds-button>
    <cds-button
      v-if="$session?.isLoggedIn()"
      data-id="logout-btn"
      kind="tertiary"
      :size="(isSmallScreen || isMediumScreen) ? 'sm' : 'lg'"
      @click.prevent="logoutModalActive = true"
    >
      <span v-if="!isSmallScreen && !isMediumScreen">Logout</span>
      <Logout16 slot="icon" />
    </cds-button>
  </div>

  <cds-modal
    id="help-modal"
    size="sm"
    :open="helpModalActive"
    @cds-modal-closed="helpModalActive = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading>
        Help with application
      </cds-modal-heading>
    </cds-modal-header>
    <cds-modal-body>
      <p>
        Can’t proceed with your application? Let us know by emailing your issue to <a href='mailto:forhvap.cliadmin@gov.bc.ca'>forhvap.cliadmin@gov.bc.ca</a> and we’ll get back to you.
      </p>
    </cds-modal-body>
  </cds-modal>

  <cds-modal
    id="logout-modal"
    size="sm"
    :open="logoutModalActive"
    @cds-modal-closed="logoutModalActive = false"
  >
    <cds-modal-header>
      <cds-modal-close-button></cds-modal-close-button>
      <cds-modal-heading>
        Are you sure you want to logout? Your data will not be saved.
      </cds-modal-heading>
    </cds-modal-header>
    <cds-modal-body><p></p></cds-modal-body>

    <cds-modal-footer>
        <cds-modal-footer-button 
          kind="secondary"
          data-modal-close
          class="cds--modal-close-btn">
          Cancel
        </cds-modal-footer-button>
        
        <cds-modal-footer-button 
          kind="danger"
          class="cds--modal-submit-btn"
          v-on:click="$session?.logOut"
        >
          Logout
          <Logout16 slot="icon" />
        </cds-modal-footer-button>

      </cds-modal-footer>
  </cds-modal>
</template>
