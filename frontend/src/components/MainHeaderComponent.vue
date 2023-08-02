<template>
  <header>
    <div class="bx--header">
      <div v-if="$session?.user?.provider !== 'idir'">
        <a href="https://gov.bc.ca">
          <img
            src="/img/bc_for_logo.png"
            alt="Go to the Government of British Columbia website"
            width="150"
          />
        </a>
        <div class="bx--header-external-texts">
          <label class="bx--header-site-name">
            Ministry of Forests
          </label>
          <label class="bx--header-env-and-rel" 
                v-if="env !== 'Prod'">
            Env. {{ env }} - Rel. {{appVersion}}
          </label>
        </div>
        <bx-btn
          v-if="$session?.isLoggedIn()"
          data-id="logout-btn"
          kind="tertiary"
          iconLayout=""
          class="bx--btn bx--btn-header"
         @click.prevent="$session?.logOut"
          size="field"
        >
          <span>Logout</span>
          <Logout16 slot="icon" />
        </bx-btn>
      </div>
      <div v-if="$session?.user?.provider === 'idir'">
        <label class="bx--header-site-name">Client Management System</label>
        <label class="bx--header-env-and-rel" 
              v-if="env !== 'Prod'">
          Env. {{ env }} - Rel. {{appVersion}}
        </label>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import Logout16 from '@carbon/icons-vue/es/logout/16';
import { nodeEnv, appVersion } from '@/CoreConstants';
import { ref } from 'vue';

const envPrefix = "openshift-";
const env = ref(nodeEnv);
env.value = env.value.slice(envPrefix.length);
env.value = env.value.charAt(0).toUpperCase() + env.value.slice(1);
</script>

<style scoped>
.bx--header-external-texts {
  position: absolute;
  top: 40%;
  left: 9rem;
}

.bx--header-site-name {
  font-family: 'BCSans', 'Noto Sans', Verdana, Arial, sans-serif;
  font-size: 0.875rem;
  font-weight: 700;
  color: #F3F3F5;
  padding-left: 2rem;
  display: inline-block;
}

.bx--header-env-and-rel {
  padding-left: 2rem; 
  color: white;
}
</style>
