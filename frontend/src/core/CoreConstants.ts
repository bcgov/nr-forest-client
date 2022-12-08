// env vars
export const backendUrl =
  window.localStorage.getItem("VITE_BACKEND_URL") ||
  import.meta.env.VITE_BACKEND_URL;
export const keycloakUrl =
  window.localStorage.getItem("VITE_KEYCLOAK_URL") ||
  import.meta.env.VITE_KEYCLOAK_URL;
export const keycloakClientId =
  window.localStorage.getItem("VITE_KEYCLOAK_CLIENT_ID") ||
  import.meta.env.VITE_KEYCLOAK_CLIENT_ID;
export const nodeEnv =
  window.localStorage.getItem("VITE_NODE_ENV") ||
  import.meta.env.VITE_NODE_ENV;

// constant
export const maxFileSizePerFile = 1000000 * 20; //20 mb
export const maxTotalFileSize = 1000000 * 20 * 5; //100 mb
