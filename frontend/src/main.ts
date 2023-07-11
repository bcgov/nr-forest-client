import { createApp } from 'vue'
import VueKeyCloak from '@dsb-norge/vue-keycloak-js'
import type Keycloak from 'keycloak-js'
import type { KeycloakPromise } from 'keycloak-js'
import type { VueKeycloakInstance } from '@dsb-norge/vue-keycloak-js/dist/types'

import App from '@/App.vue'
import { router } from '@/routes'
import { keycloakUrl, keycloakClientId, nodeEnv } from '@/CoreConstants'
import { masking } from '@/helpers/CustomDirectives'

// Importing Styles
import '@/styles'

const app = createApp(App)

app.use(router)

app.directive('mask', masking('.bx--text-input__field-wrapper input'))

if (!nodeEnv || nodeEnv === 'openshift-dev') {
  const fakeKeycloak = {
    authenticated: true,
    login: (options?: any): KeycloakPromise<void, void> | void => {
      console.log('Mocked login')
    },
    logout: (options?: any): KeycloakPromise<void, void> | void => {
      console.log('Mocked logout')
      fakeKeycloak.authenticated = false
    },
    register: (options?: any): KeycloakPromise<void, void> | void => {
      console.log('Mocked register')
    },
    logoutFn: (options?: any): KeycloakPromise<void, void> | void => {
      console.log('Mocked logoutFn ', fakeKeycloak.authenticated)
      fakeKeycloak.authenticated = false
    },
    token: 'fake-token',
    tokenParsed: {
      display_name: 'fake-user',
      given_name: 'fake',
      family_name: 'user',
      email: 'fake-user@mail.com',
      identity_provider: 'bceid'
    },
    subject: 'fake-user'
  }

  app.provide('keycloak', fakeKeycloak)
  app.config.globalProperties.$keycloak = fakeKeycloak
  app.mount('#app')
} else {
  app.use(VueKeyCloak, {
    init: {
      onLoad: 'login-required',
      pkceMethod: 'S256'
    },
    config: {
      url: keycloakUrl,
      realm: 'standard',
      clientId: keycloakClientId
    },
    /* eslint-disable typescript:S1874 */
    onReady: (keycloak: Keycloak) => {
      app.provide('keycloak', keycloak)
      app.mount('#app')
    }
  })
}

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $keycloak: VueKeycloakInstance
  }
}
