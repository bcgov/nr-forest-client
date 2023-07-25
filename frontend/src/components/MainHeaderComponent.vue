

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
    <bx-btn
      v-if="logged"
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
  </header>
</template>

<script setup lang="ts">
import Logout16 from '@carbon/icons-vue/es/logout/16'
import { computedAsync } from '@vueuse/core'
import AmplifyUserSession from '@/helpers/AmplifyUserSession'

const logged = computedAsync(
  async () => await AmplifyUserSession.isLoggedIn(),
  false
)
</script>

