<template>
  <div class="bx--content">
    <div class="waiting">
      <bx-loading type="small"> </bx-loading>
    </div>
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useRouter } from 'vue-router'
import { computedAsync } from '@vueuse/core'

import AmplifyUserSession from '@/helpers/AmplifyUserSession'

const router = useRouter()

const userLoggedIn = computedAsync(
  async () => AmplifyUserSession.isLoggedIn(),
  false
)

const loginRedirect = (userIsLogged: boolean) => {
  if (userIsLogged) {
    AmplifyUserSession?.user?.provider === 'idir' //Needs to invert the logic here later
      ? router.push({ name: 'form' })
      : router.push({ name: 'internal' })
  }
}

watch(userLoggedIn, loginRedirect)
</script>

<style scoped>
.waiting {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.waiting bx-loading {
  height: 5.5rem;
}
</style>
