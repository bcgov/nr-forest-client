const constants: Record<string, any> = {}

export const checkEnv = (key: string) => {
  if (key in constants) return constants[key]
  constants[key] = window.localStorage.getItem(key) || import.meta.env[key]
  return constants[key]
}

export const featureFlags = JSON.parse(checkEnv('VITE_FEATURE_FLAGS') || '{}')
export const backendUrl: string = checkEnv('VITE_BACKEND_URL')
export const frontendUrl: string = checkEnv('VITE_FRONTEND_URL')
export const nodeEnv: string = checkEnv('VITE_NODE_ENV')
export const cognitoEnv: string = checkEnv('VITE_AWS_ENV')

// constant
export const maxFileSizePerFile = 1000000 * 20 // 20 mb
export const maxTotalFileSize = 1000000 * 20 * 5 // 100 mb
