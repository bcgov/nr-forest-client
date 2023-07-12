<template>
  <div>Temporary loading user placeholder</div>
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
    console.log(AmplifyUserSession?.user)
    AmplifyUserSession?.user?.provider === 'idir' //Needs to invert the logic here later
      ? router.push({ name: 'form' })
      : router.push({ name: 'internal' })
  }
}

watch(userLoggedIn, loginRedirect)
</script>
