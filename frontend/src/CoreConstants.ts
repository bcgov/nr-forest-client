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

const wm_domain = 'localhost:8081'
const cognito_domain = `${checkEnv('VITE_AWS_COGNITO_DOMAIN')}.auth.${checkEnv(
  'VITE_AWS_COGNITO_REGION'
)}.amazoncognito.com`

export const amplifyConfig = {
  aws_cognito_region: checkEnv('VITE_AWS_COGNITO_REGION'),
  aws_user_pools_id: checkEnv('VITE_AWS_USER_POOLS_ID'),
  aws_user_pools_web_client_id: checkEnv('VITE_AWS_USER_POOLS_WEB_CLIENT_ID'),
  aws_mandatory_sign_in: 'enable',
  oauth: {
    domain: cognito_domain,
    scope: ['openid'],
    redirectSignIn: `${checkEnv('VITE_FRONTEND_URL')}/dashboard`,
    redirectSignOut: checkEnv('VITE_AWS_LOGOUT'),
    responseType: 'code'
  },
  federationTarget: 'COGNITO_USER_POOLS'
}
