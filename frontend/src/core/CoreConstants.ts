export const backendUrl =
  config.VITE_BACKEND_URL || import.meta.env.VITE_BACKEND_URL;
export const keycloakUrl =
  config.VITE_KEYCLOAK_URL || import.meta.env.VITE_KEYCLOAK_URL;
export const keycloakClientId =
  config.VITE_KEYCLOAK_CLIENT_ID || import.meta.env.VITE_KEYCLOAK_CLIENT_ID;
export const nodeEnv = config.VITE_NODE_ENV || import.meta.env.VITE_NODE_ENV;

export const maxFileSizePerFile = 1000000 * 20; //20 mb
export const maxTotalFileSize = 1000000 * 20 * 5; //100 mb



// colors
export const headerBlue = "#036";
export const navBlue = "#38598a";
export const navSelectBlue = "rgba(84, 117, 167, 1)";