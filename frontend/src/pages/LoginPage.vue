<template>
  <div>FAM Template</div>

  <bx-btn
    kind="tertiary"
    iconLayout=""
    class="bx--btn"
    @click.prevent="$session?.logIn('bceidbusiness')"
    size="field"
  >
    <span>Login with BCeID</span>
    <Logout16 slot="icon" />
  </bx-btn>

  <bx-btn
    kind="primary"
    iconLayout=""
    class="bx--btn"
    @click.prevent="$session?.logIn('idir')"
    size="field"
  >
    <span>LogIn with IDIR</span>
    <Logout16 slot="icon" />
  </bx-btn>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import Logout16 from '@carbon/icons-vue/es/logout/16'
import AmplifyUserSession from '@/helpers/AmplifyUserSession'

// extract the querystring parameters from the URL
const router = useRouter()
const { query } = router.currentRoute.value

// check if a querystring parameter called ref exists and if it has a value of external
if (query.ref && query.ref === 'external') {
  AmplifyUserSession.logIn('bceidbusiness')
}
</script>
