// This file is provided as an option to support runtime loading of configuration parameters in OpenShift
// Development builds and production builds using yarn build will still work using .env files
const config = (() => {
  return {
    // VITE_BACKEND_URL: '',
    // VITE_KEYCLOAK_URL: '',
    // VITE_KEYCLOAK_CLIENT_ID: '',
    // VITE_KEYCLOAK_REALM: ''
  };
})();

export default config;
