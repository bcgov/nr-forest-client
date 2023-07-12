export const checkEnv = (key: string) =>
  window.localStorage.getItem(key) || import.meta.env[key]

export const backendUrl = checkEnv('VITE_BACKEND_URL')
export const frontendUrl = checkEnv('VITE_FRONTEND_URL')
export const keycloakUrl = checkEnv('VITE_KEYCLOAK_URL')
export const keycloakClientId = checkEnv('VITE_KEYCLOAK_CLIENT_ID')
export const nodeEnv = checkEnv('VITE_NODE_ENV')

// constant
export const maxFileSizePerFile = 1000000 * 20 // 20 mb
export const maxTotalFileSize = 1000000 * 20 * 5 // 100 mb

export const amplifyConfig = {
  aws_cognito_region: checkEnv('VITE_AWS_COGNITO_REGION'),
  aws_user_pools_id: checkEnv('VITE_AWS_USER_POOLS_ID'),
  aws_user_pools_web_client_id: checkEnv('VITE_AWS_USER_POOLS_WEB_CLIENT_ID'),
  aws_mandatory_sign_in: 'enable',
  oauth: {
    domain: `${checkEnv('VITE_AWS_COGNITO_DOMAIN')}.auth.${checkEnv(
      'VITE_AWS_COGNITO_REGION'
    )}.amazoncognito.com`,
    scope: ['openid'],
    redirectSignIn: `${checkEnv('VITE_FRONTEND_URL')}/dashboard`,
    redirectSignOut: checkEnv('VITE_AWS_LOGOUT'),
    responseType: 'code'
  },
  federationTarget: 'COGNITO_USER_POOLS'
}
