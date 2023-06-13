import { createApp } from "vue";
import VueTheMask from "vue-the-mask";
import App from "./App.vue";
import VueKeycloakJs from "@dsb-norge/vue-keycloak-js";
import type { KeycloakInstance } from "keycloak-js";
import type { VueKeycloakInstance } from "@dsb-norge/vue-keycloak-js/dist/types";
import { keycloakUrl, keycloakClientId, nodeEnv } from "./core/CoreConstants";


// Import Bootstrap an BootstrapVue CSS files (order is important)
import "bootstrap/dist/css/bootstrap.css";
import "bootstrap-vue-3/dist/bootstrap-vue-3.css";

//Import Carbon WebComponents
import "@carbon/web-components/es";
import "carbon-components/css/carbon-components.min.css";
import "@/assets/theme/components-overrides.scss";

import "@/assets/global.css";

const app = createApp(App);
app.use(VueTheMask);

if (nodeEnv && nodeEnv == "openshift-dev") {
  // disable the login authentication for the deployment in the openshift dev namespace
  // cause the url in the dev namespace is not stable
  app.mount("#app");
} else {
  app.use(VueKeycloakJs, {
    init: {
      // Use 'login-required' to always require authentication
      // If using 'login-required', there is no need for the router guards in router.js
      onLoad: "login-required",
      pkceMethod: "S256",
      // onLoad: "check-sso",
      // silentCheckSsoRedirectUri: window.location.origin + "/index.html",
    },
    config: {
      url: keycloakUrl,
      realm: "standard",
      clientId: keycloakClientId,
    },
    onReady(keycloak: KeycloakInstance) {
      console.log("Keycloak ready", keycloak);
      // provde global property keycloak to read login information
      app.provide("keycloak", keycloak);
      app.mount("#app");
    },
  });
}

declare module "@vue/runtime-core" {
  interface ComponentCustomProperties {
    $keycloak: VueKeycloakInstance;
  }
}
