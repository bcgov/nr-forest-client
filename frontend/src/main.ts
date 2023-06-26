import { createApp } from 'vue'
import VueKeycloakJs from '@dsb-norge/vue-keycloak-js'
import type { Keycloak, KeycloakPromise } from 'keycloak-js'
import type { VueKeycloakInstance } from '@dsb-norge/vue-keycloak-js/dist/types'

import App from '@/App.vue'
import { router } from '@/routes'
import { keycloakUrl, keycloakClientId, nodeEnv } from '@/core/CoreConstants'
import { masking } from '@/helpers/CustomDirectives'

// Importing BC typography
import '@bcgov/bc-sans/css/BCSans.css'

// Import Carbon WebComponents
import '@carbon/web-components/es'
import 'carbon-components/css/carbon-components.min.css'
import '@/assets/theme/components-overrides.scss'

import '@/assets/global.css'

const app = createApp(App)

app.use(router)

app.directive('mask', masking('.bx--text-input__field-wrapper input'))

if (nodeEnv && nodeEnv == 'openshift-dev') {
  // disable the login authentication for the deployment in the openshift dev namespace
  // cause the url in the dev namespace is not stable

  const fakeKeycloak = {
    authenticated: true,
    login: () => {},
    logout: () => {},
    register: () => {},
    logoutFn: (options?: any): KeycloakPromise<void, void> | void => {}
  }

  app.provide('keycloak', fakeKeycloak)
  app.mount('#app')
} else {
  app.use(VueKeycloakJs, {
    init: {
      // Use 'login-required' to always require authentication
      // If using 'login-required', there is no need for the router guards in router.js
      onLoad: 'login-required',
      pkceMethod: 'S256'
      // onLoad: "check-sso",
      // silentCheckSsoRedirectUri: window.location.origin + "/index.html",
    },
    config: {
      url: keycloakUrl,
      realm: 'standard',
      clientId: keycloakClientId
    },
    onReady(keycloak: Keycloak) {
      console.log('Keycloak ready', keycloak)
      // provde global property keycloak to read login information
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
