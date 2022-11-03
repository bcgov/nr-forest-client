// import config from "../../public/config"; 

// env variables
export const backendUrl =
  config.VITE_BACKEND_URL || import.meta.env.VITE_BACKEND_URL;
export const keycloakUrl =
  config.VITE_KEYCLOAK_URL || import.meta.env.VITE_KEYCLOAK_URL;
export const keycloakClientId =
  config.VITE_KEYCLOAK_CLIENT_ID || import.meta.env.VITE_KEYCLOAK_CLIENT_ID;
export const nodeEnv = config.VITE_NODE_ENV || import.meta.env.VITE_NODE_ENV;