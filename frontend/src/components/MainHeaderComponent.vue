

<template>
  <header>
    <div class="bx--header">
      <a href="https://gov.bc.ca">
        <img
          src="/img/bc_for_logo.png"
          alt="Go to the Government of British Columbia website"
          width="150"
        />
      </a>
      <label class="bx--header-site-name">Ministry of Forests</label>
      <label class="bx--header-env-and-rel" 
             v-if="env !== 'Prod'">
        Env. {{ env }} - Rel. 1.0.22.15
      </label>
      <bx-btn
        v-if="$keycloak"
        kind="tertiary"
        iconLayout=""
        class="bx--btn bx--btn-header"
        @click.prevent="$keycloak.logoutFn"
        size="field"
      >
        <span>Logout</span>
        <Logout16 slot="icon" />
      </bx-btn>
    </div>
  </header>
</template>

<script setup lang="ts">
import Logout16 from '@carbon/icons-vue/es/logout/16';
import { nodeEnv } from '@/CoreConstants';
import { ref } from 'vue';

const envPrefix = "openshift-";
const env = ref(nodeEnv);
env.value = env.value.slice(envPrefix.length);
env.value = env.value.charAt(0).toUpperCase() + env.value.slice(1);
</script>

<style scoped>
.bx--header-env-and-rel {
  padding-left: 2rem; 
  color: white;
}
</style>
