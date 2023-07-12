import { version } from '../package.json'

export const checkEnv = (key: string) =>
  window.localStorage.getItem(key) || import.meta.env[key]

export const backendUrl = checkEnv('VITE_BACKEND_URL')
export const frontendUrl = checkEnv('VITE_FRONTEND_URL')
export const keycloakUrl = checkEnv('VITE_KEYCLOAK_URL')
export const keycloakClientId = checkEnv('VITE_KEYCLOAK_CLIENT_ID')
export const nodeEnv = checkEnv('VITE_NODE_ENV')

export const appVersion = version

// constant
export const maxFileSizePerFile = 1000000 * 20 // 20 mb
export const maxTotalFileSize = 1000000 * 20 * 5 // 100 mb
