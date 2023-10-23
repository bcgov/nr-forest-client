import { version } from "../package.json";
import { useMediaQuery } from "@vueuse/core";

export const checkEnv = (key: string) =>
  window.localStorage.getItem(key) || import.meta.env[key]

export const featureFlags = JSON.parse(checkEnv('VITE_FEATURE_FLAGS') || '{}')
export const backendUrl = checkEnv('VITE_BACKEND_URL')
export const frontendUrl = checkEnv('VITE_FRONTEND_URL')
export const greenDomain = checkEnv('VITE_GREEN_DOMAIN')
export const nodeEnv = checkEnv('VITE_NODE_ENV')

export const appVersion = version

// constant
export const maxFileSizePerFile = 1000000 * 20; // 20 mb
export const maxTotalFileSize = 1000000 * 20 * 5; // 100 mb

export const isSmallScreen = useMediaQuery("(max-width: 671px)");
export const isMediumScreen = useMediaQuery("(min-width: 672px) and (max-width: 1055px)");
