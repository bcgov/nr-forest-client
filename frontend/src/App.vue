<template>
  <MainHeader />
  <router-view></router-view>
</template>

<script setup lang="ts">
import { inject, provide, ref } from 'vue'
import type { KeycloakInstance } from 'keycloak-js'
import { useRouter } from 'vue-router'

import MainHeader from '@/common/MainHeaderComponent.vue'
import type { Submitter } from '@/core/CommonTypes'

const keycloak: KeycloakInstance | undefined = inject('keycloak')
const router = useRouter()

let submitterInformation = ref<Submitter>({
  firstName: '',
  lastName: '',
  email: '',
  bceidBusinessName: '',
  userId: ''
})

const submitAndRedirect = (page: string) => {
  provide('submitterInformation', submitterInformation.value)
  router.push({ name: 'form' })
}

if (
  keycloak &&
  keycloak.tokenParsed &&
  keycloak.tokenParsed.identity_provider === 'idir'
) {
  submitAndRedirect('internal')
} else {
  submitterInformation.value.bceidBusinessName =
    keycloak && keycloak.tokenParsed
      ? keycloak.tokenParsed.display_name
      : 'Dev Test Client Name'
  submitterInformation.value.userId =
    keycloak && keycloak.tokenParsed ? keycloak.subject : 'testUserId'
  submitterInformation.value.firstName =
    keycloak && keycloak.tokenParsed ? keycloak.tokenParsed.given_name : 'Maria'
  submitterInformation.value.lastName =
    keycloak && keycloak.tokenParsed
      ? keycloak.tokenParsed.family_name
      : 'Martinez'
  submitterInformation.value.email =
    keycloak && keycloak.tokenParsed
      ? keycloak.tokenParsed.email
      : 'maria.martinez@gov.bc.ca'
  submitAndRedirect('form')
}
</script>

<style>
#app {
  /* font-family: Avenir, Helvetica, Arial, sans-serif; */
  font-family: -apple-system, BlinkMacSystemFont, BCSans, Roboto, Verdana, Arial,
    sans-serif;
  font-size: 0.875rem;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* text-align: center; */
  /* color: #2c3e50; */
  color: rgba(0, 0, 0, 0.87);
}

.card-header {
  /*padding: 0.5rem 0.5rem 0.5rem 0.5rem !important;*/
}

.row {
  padding: 0px 0px 0px 0px !important;
  margin: 0px 0px 0px 0px !important;
}

.row > * {
  padding: 0px 0px 0px 0px !important;
  margin: 0px 0px 0px 0px !important;
}

.container,
.container-fluid,
.container-xxl,
.container-xl,
.container-lg,
.container-md,
.container-sm {
  padding: 0px 0px 0px 0px !important;
  margin: 0px 0px 0px 0px !important;
  max-width: 100% !important;
}

.col-3 {
  padding-right: 1rem !important;
}

/* ------------ nav bar ------------------- */
.nav.nav-pills {
  background-color: v-bind(navBlue) !important;
  height: 45px;
}

.nav.nav-pills .nav-item button {
  color: #fff;
  height: 100%;
}

.nav.nav-pills .nav-item button[aria-selected='true'] {
  background-color: v-bind(navSelectBlue);
  border-color: v-bind(navSelectBlue);
}
</style>
